package com.gxr.instantChat.client.ui;

import com.gxr.instantChat.client.socket.SocketClient;
import com.gxr.instantChat.common.Message;
import com.gxr.instantChat.common.MessageType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame {

    private static final String GROUP_KEY = "__GROUP__";
    private static final String GROUP_TITLE = "公共聊天室";

    private final String username;
    private final SocketClient socketClient;

    private final DefaultListModel<String> contactModel = new DefaultListModel<>();
    private final JList<String> contactList = new JList<>(contactModel);

    private final JLabel chatTitleLabel = new JLabel(GROUP_TITLE);
    private final JPanel messagePanel = new JPanel();
    private JScrollPane messageScrollPane;

    private final JTextArea inputArea = new JTextArea();
    private final JButton sendButton = new JButton("发送");

    private final Map<String, List<Message>> conversationMessages = new HashMap<>();
    private String currentConversation = GROUP_KEY;

    public MainFrame(String username, SocketClient socketClient) {
        this.username = username;
        this.socketClient = socketClient;

        setTitle("校园即时通信系统 - " + username);
        setSize(920, 640);
        setMinimumSize(new Dimension(760, 520));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initView();
        initEvents();
    }

    private void initView() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(new Color(245, 245, 245));

        rootPanel.add(createContactPanel(), BorderLayout.WEST);
        rootPanel.add(createChatPanel(), BorderLayout.CENTER);

        setContentPane(rootPanel);
        refreshContactList(new ArrayList<>());
    }

    private JPanel createContactPanel() {
        JPanel contactPanel = new JPanel(new BorderLayout());
        contactPanel.setPreferredSize(new Dimension(220, 0));
        contactPanel.setBackground(new Color(238, 238, 238));
        contactPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));

        JLabel titleLabel = new JLabel("联系人");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        titleLabel.setBorder(new EmptyBorder(16, 16, 12, 16));
        contactPanel.add(titleLabel, BorderLayout.NORTH);

        contactList.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
        contactList.setFixedCellHeight(48);
        contactList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactList.setBackground(new Color(248, 248, 248));
        contactList.setBorder(null);

        JScrollPane contactScrollPane = new JScrollPane(contactList);
        contactScrollPane.setBorder(null);
        contactPanel.add(contactScrollPane, BorderLayout.CENTER);

        return contactPanel;
    }

    private JPanel createChatPanel() {
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBackground(new Color(245, 245, 245));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setPreferredSize(new Dimension(0, 56));
        titlePanel.setBackground(new Color(245, 245, 245));
        titlePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 225, 225)));

        chatTitleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        chatTitleLabel.setBorder(new EmptyBorder(0, 20, 0, 20));
        titlePanel.add(chatTitleLabel, BorderLayout.CENTER);

        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setBackground(new Color(245, 245, 245));
        messagePanel.setBorder(new EmptyBorder(12, 0, 12, 0));

        messageScrollPane = new JScrollPane(messagePanel);
        messageScrollPane.setBorder(null);
        messageScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        chatPanel.add(titlePanel, BorderLayout.NORTH);
        chatPanel.add(messageScrollPane, BorderLayout.CENTER);
        chatPanel.add(createInputPanel(), BorderLayout.SOUTH);

        return chatPanel;
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setPreferredSize(new Dimension(0, 150));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(225, 225, 225)));

        inputArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBorder(new EmptyBorder(12, 14, 12, 14));

        JScrollPane inputScrollPane = new JScrollPane(inputArea);
        inputScrollPane.setBorder(null);
        inputPanel.add(inputScrollPane, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 10));
        actionPanel.setBackground(Color.WHITE);

        sendButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        sendButton.setPreferredSize(new Dimension(90, 34));
        actionPanel.add(sendButton);

        inputPanel.add(actionPanel, BorderLayout.SOUTH);
        return inputPanel;
    }

    private void initEvents() {
        contactList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = contactList.getSelectedValue();
                if (selected != null) {
                    currentConversation = GROUP_TITLE.equals(selected) ? GROUP_KEY : selected;
                    chatTitleLabel.setText(selected);
                    renderCurrentConversation();
                }
            }
        });

        sendButton.addActionListener(e -> sendMessage());

        inputArea.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK),
                "sendMessage"
        );
        inputArea.getActionMap().put("sendMessage", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendMessage();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Message message = new Message();
                message.setType(MessageType.LOGOUT);
                message.setFrom(username);
                socketClient.send(message);
                socketClient.close();
            }
        });
    }

    public void handleMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            switch (message.getType()) {
                case MessageType.LOGIN_RESULT:
                case MessageType.ONLINE_USERS:
                    refreshOnlineUsers(message);
                    break;
                case MessageType.PRIVATE_CHAT:
                    addPrivateMessage(message);
                    break;
                case MessageType.GROUP_CHAT:
                    addGroupMessage(message);
                    break;
                case MessageType.ERROR:
                    JOptionPane.showMessageDialog(this, message.getReason());
                    break;
                default:
                    break;
            }
        });
    }

    private void refreshOnlineUsers(Message message) {
        List<String> users = message.getOnlineUsers() == null ? new ArrayList<>() : message.getOnlineUsers();
        refreshContactList(users);
    }

    private void refreshContactList(List<String> onlineUsers) {
        String selectedBefore = getSelectedDisplayName();

        contactModel.clear();
        contactModel.addElement(GROUP_TITLE);

        List<String> users = new ArrayList<>(onlineUsers);
        users.sort(String::compareTo);
        for (String onlineUser : users) {
            if (!username.equals(onlineUser)) {
                contactModel.addElement(onlineUser);
            }
        }

        String targetSelection = selectedBefore;
        if (targetSelection == null || contactModel.indexOf(targetSelection) < 0) {
            targetSelection = GROUP_TITLE;
        }
        contactList.setSelectedValue(targetSelection, true);
    }

    private String getSelectedDisplayName() {
        return GROUP_KEY.equals(currentConversation) ? GROUP_TITLE : currentConversation;
    }

    private void addPrivateMessage(Message message) {
        String conversationKey = username.equals(message.getFrom()) ? message.getTo() : message.getFrom();
        conversationMessages.computeIfAbsent(conversationKey, key -> new ArrayList<>()).add(message);

        if (conversationKey.equals(currentConversation)) {
            appendMessageBubble(message);
            scrollToBottom();
        }
    }

    private void addGroupMessage(Message message) {
        conversationMessages.computeIfAbsent(GROUP_KEY, key -> new ArrayList<>()).add(message);

        if (GROUP_KEY.equals(currentConversation)) {
            appendMessageBubble(message);
            scrollToBottom();
        }
    }

    private void sendMessage() {
        String content = inputArea.getText().trim();
        if (content.isEmpty()) {
            return;
        }

        Message message = new Message();
        message.setFrom(username);
        message.setContent(content);

        if (GROUP_KEY.equals(currentConversation)) {
            message.setType(MessageType.GROUP_CHAT);
            message.setTo("ALL");
        } else {
            message.setType(MessageType.PRIVATE_CHAT);
            message.setTo(currentConversation);
        }

        socketClient.send(message);
        inputArea.setText("");
        inputArea.requestFocusInWindow();
    }

    private void renderCurrentConversation() {
        messagePanel.removeAll();

        List<Message> messages = conversationMessages.get(currentConversation);
        if (messages != null) {
            for (Message message : messages) {
                appendMessageBubble(message);
            }
        }

        messagePanel.revalidate();
        messagePanel.repaint();
        scrollToBottom();
    }

    private void appendMessageBubble(Message message) {
        boolean ownMessage = username.equals(message.getFrom());

        JPanel rowPanel = new JPanel(new FlowLayout(ownMessage ? FlowLayout.RIGHT : FlowLayout.LEFT, 16, 0));
        rowPanel.setOpaque(false);
        rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel bubbleWrapper = new JPanel();
        bubbleWrapper.setOpaque(false);
        bubbleWrapper.setLayout(new BoxLayout(bubbleWrapper, BoxLayout.Y_AXIS));

        JLabel senderLabel = new JLabel(ownMessage ? "我" : message.getFrom());
        senderLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        senderLabel.setForeground(new Color(130, 130, 130));
        senderLabel.setAlignmentX(ownMessage ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);

        JLabel bubbleLabel = new JLabel(toBubbleHtml(message.getContent()));
        bubbleLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
        bubbleLabel.setOpaque(true);
        bubbleLabel.setBackground(ownMessage ? new Color(149, 236, 105) : Color.WHITE);
        bubbleLabel.setBorder(new EmptyBorder(8, 12, 8, 12));
        bubbleLabel.setAlignmentX(ownMessage ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);

        bubbleWrapper.add(senderLabel);
        bubbleWrapper.add(Box.createVerticalStrut(4));
        bubbleWrapper.add(bubbleLabel);

        rowPanel.add(bubbleWrapper);
        messagePanel.add(rowPanel);
        messagePanel.add(Box.createVerticalStrut(8));

        messagePanel.revalidate();
        messagePanel.repaint();
    }

    private String toBubbleHtml(String content) {
        String escaped = escapeHtml(content == null ? "" : content).replace("\n", "<br>");
        return "<html><body style='width: 320px;'>" + escaped + "</body></html>";
    }

    private String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalBar = messageScrollPane.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getMaximum());
        });
    }
}
