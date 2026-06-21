package com.gxr.instantChat.client.ui;

import com.gxr.instantChat.client.socket.SocketClient;
import com.gxr.instantChat.common.Message;
import com.gxr.instantChat.common.MessageType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;

public class MainFrame extends JFrame {

    private static final String GROUP_KEY = "__GROUP__";
    private static final String GROUP_TITLE = "公共聊天室";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final int TEXT_BUBBLE_MIN_TEXT_WIDTH = 32;
    private static final int TEXT_BUBBLE_HORIZONTAL_PADDING = 24;
    private static final int TEXT_BUBBLE_VERTICAL_PADDING = 16;

    private final String username;
    private final SocketClient socketClient;

    private final DefaultListModel<String> contactModel = new DefaultListModel<>();
    private final JList<String> contactList = new JList<>(contactModel);

    private final JLabel chatTitleLabel = new JLabel(GROUP_TITLE);
    private final JTextField searchField = new JTextField();
    private final JButton searchButton = new JButton("搜索");
    private final JButton clearSearchButton = new JButton("清空");
    private final JPanel messagePanel = new JPanel();
    private JScrollPane messageScrollPane;

    private final JTextArea inputArea = new JTextArea();
    private final JButton fileButton = new JButton("发送文件");
    private final JButton sendButton = new JButton("发送");

    private final Map<String, List<Message>> conversationMessages = new HashMap<>();
    private final Map<String, Integer> unreadCounts = new HashMap<>();
    private final Set<String> loadedHistoryConversations = new HashSet<>();
    private final Set<String> loadingHistoryConversations = new HashSet<>();
    private String currentConversation = GROUP_KEY;
    private boolean showingSearchResult = false;
    private String searchConversation = GROUP_KEY;

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
        contactList.setCellRenderer(new ContactCellRenderer());

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

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 11));
        searchPanel.setOpaque(false);

        searchField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(170, 32));
        searchPanel.add(searchField);

        searchButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        searchButton.setPreferredSize(new Dimension(64, 32));
        searchPanel.add(searchButton);

        clearSearchButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        clearSearchButton.setPreferredSize(new Dimension(64, 32));
        searchPanel.add(clearSearchButton);

        titlePanel.add(searchPanel, BorderLayout.EAST);

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

        fileButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        fileButton.setPreferredSize(new Dimension(100, 34));
        actionPanel.add(fileButton);

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
                    clearSearchState();
                    markConversationReadAndNotify(currentConversation);
                    chatTitleLabel.setText(selected);
                    requestConversationHistory(currentConversation);
                    renderCurrentConversation();
                }
            }
        });

        sendButton.addActionListener(e -> sendMessage());
        fileButton.addActionListener(e -> sendFile());
        searchButton.addActionListener(e -> searchMessages());
        clearSearchButton.addActionListener(e -> clearSearchResults());
        searchField.addActionListener(e -> searchMessages());

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
                case MessageType.HISTORY_RESULT:
                    loadHistoryMessages(message);
                    break;
                case MessageType.SEARCH_RESULT:
                    loadSearchMessages(message);
                    break;
                case MessageType.READ_RECEIPT:
                    handleReadReceipt(message);
                    break;
                case MessageType.FILE:
                    handleFileMessage(message);
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
        requestConversationHistory(currentConversation);
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

    private String toConversationKey(String displayName) {
        return GROUP_TITLE.equals(displayName) ? GROUP_KEY : displayName;
    }

    private void addPrivateMessage(Message message) {
        String conversationKey = username.equals(message.getFrom()) ? message.getTo() : message.getFrom();
        conversationMessages.computeIfAbsent(conversationKey, key -> new ArrayList<>()).add(message);
        boolean ownMessage = username.equals(message.getFrom());

        if (conversationKey.equals(currentConversation)) {
            markConversationRead(conversationKey);
            if (!ownMessage) {
                sendReadReceipt(conversationKey);
            }
            if (!isShowingSearchForCurrentConversation()) {
                appendMessageBubble(message);
                scrollToBottom();
            }
        } else if (!ownMessage) {
            increaseUnread(conversationKey);
        }
    }

    private void addGroupMessage(Message message) {
        conversationMessages.computeIfAbsent(GROUP_KEY, key -> new ArrayList<>()).add(message);
        boolean ownMessage = username.equals(message.getFrom());

        if (GROUP_KEY.equals(currentConversation)) {
            markConversationRead(GROUP_KEY);
            if (!isShowingSearchForCurrentConversation()) {
                appendMessageBubble(message);
                scrollToBottom();
            }
        } else if (!ownMessage) {
            increaseUnread(GROUP_KEY);
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
            message.setRead(false);
        }

        socketClient.send(message);
        inputArea.setText("");
        inputArea.requestFocusInWindow();
    }

    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = fileChooser.getSelectedFile();
        if (file == null || !file.isFile()) {
            return;
        }

        if (file.length() > MAX_FILE_SIZE) {
            JOptionPane.showMessageDialog(this, "文件不能超过 10MB");
            return;
        }

        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());

            Message message = new Message();
            message.setType(MessageType.FILE);
            message.setFrom(username);
            message.setTo(GROUP_KEY.equals(currentConversation) ? "ALL" : currentConversation);
            if (!"ALL".equals(message.getTo())) {
                message.setRead(false);
            }
            message.setFileName(file.getName());
            message.setFileSize(file.length());
            message.setContent(Base64.getEncoder().encodeToString(fileBytes));

            socketClient.send(message);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "读取文件失败：" + ex.getMessage());
        }
    }

    private void searchMessages() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            clearSearchResults();
            return;
        }

        searchConversation = currentConversation;
        showingSearchResult = true;
        chatTitleLabel.setText(getSelectedDisplayName() + " - 搜索：" + keyword);

        Message message = new Message();
        message.setType(MessageType.SEARCH_REQUEST);
        message.setFrom(username);
        message.setTo(GROUP_KEY.equals(currentConversation) ? "ALL" : currentConversation);
        message.setKeyword(keyword);

        socketClient.send(message);
    }

    private void clearSearchResults() {
        clearSearchState();
        chatTitleLabel.setText(getSelectedDisplayName());
        renderCurrentConversation();
    }

    private void clearSearchState() {
        showingSearchResult = false;
        searchConversation = currentConversation;
        searchField.setText("");
    }

    private void handleFileMessage(Message message) {
        if ("ALL".equals(message.getTo())) {
            addGroupMessage(message);
        } else {
            addPrivateMessage(message);
        }
    }

    private void saveReceivedFile(Message message) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(message.getFileName()));
        int result = fileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            byte[] fileBytes = Base64.getDecoder().decode(message.getContent());
            Files.write(fileChooser.getSelectedFile().toPath(), fileBytes);
            JOptionPane.showMessageDialog(this, "文件保存成功");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "保存文件失败：" + ex.getMessage());
        }
    }

    private void requestConversationHistory(String conversationKey) {
        if (loadedHistoryConversations.contains(conversationKey) || loadingHistoryConversations.contains(conversationKey)) {
            return;
        }

        loadingHistoryConversations.add(conversationKey);

        Message message = new Message();
        message.setType(MessageType.HISTORY_REQUEST);
        message.setFrom(username);
        message.setTo(GROUP_KEY.equals(conversationKey) ? "ALL" : conversationKey);

        socketClient.send(message);
    }

    private void loadHistoryMessages(Message message) {
        String conversationKey = "ALL".equals(message.getTo()) ? GROUP_KEY : message.getTo();
        loadingHistoryConversations.remove(conversationKey);
        loadedHistoryConversations.add(conversationKey);

        List<Message> historyMessages = message.getHistoryMessages() == null ? new ArrayList<>() : message.getHistoryMessages();
        List<Message> currentMessages = conversationMessages.get(conversationKey);
        List<Message> mergedMessages = mergeHistoryWithCurrent(historyMessages, currentMessages);
        conversationMessages.put(conversationKey, mergedMessages);

        if (conversationKey.equals(currentConversation)) {
            renderCurrentConversation(true);
        }
    }

    private void loadSearchMessages(Message message) {
        String conversationKey = "ALL".equals(message.getTo()) ? GROUP_KEY : message.getTo();
        if (!conversationKey.equals(currentConversation)) {
            return;
        }

        showingSearchResult = true;
        searchConversation = conversationKey;
        chatTitleLabel.setText(getSelectedDisplayName() + " - 搜索：" + message.getKeyword());

        messagePanel.removeAll();
        List<Message> results = message.getHistoryMessages() == null ? new ArrayList<>() : message.getHistoryMessages();
        if (results.isEmpty()) {
            addEmptyHint("没有找到相关聊天记录");
        } else {
            for (Message result : results) {
                appendMessageBubble(result);
            }
        }

        messagePanel.revalidate();
        messagePanel.repaint();
        scrollToBottom();
    }

    private void renderCurrentConversation() {
        renderCurrentConversation(true);
    }

    private void renderCurrentConversation(boolean scrollToBottomAfterRender) {
        messagePanel.removeAll();

        List<Message> messages = conversationMessages.get(currentConversation);
        if (messages != null) {
            for (Message message : messages) {
                appendMessageBubble(message);
            }
        }

        messagePanel.revalidate();
        messagePanel.repaint();
        if (scrollToBottomAfterRender) {
            scrollToBottom();
        }
    }

    private void addEmptyHint(String text) {
        JLabel hintLabel = new JLabel(text);
        hintLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        hintLabel.setForeground(new Color(140, 140, 140));
        hintLabel.setBorder(new EmptyBorder(30, 0, 0, 0));
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        messagePanel.add(hintLabel);
    }

    private boolean isShowingSearchForCurrentConversation() {
        return showingSearchResult && currentConversation.equals(searchConversation);
    }

    private void increaseUnread(String conversationKey) {
        unreadCounts.merge(conversationKey, 1, Integer::sum);
        contactList.repaint();
    }

    private void markConversationReadAndNotify(String conversationKey) {
        markConversationRead(conversationKey);
        sendReadReceipt(conversationKey);
    }

    private void markConversationRead(String conversationKey) {
        if (unreadCounts.remove(conversationKey) != null) {
            contactList.repaint();
        }
    }

    private void sendReadReceipt(String conversationKey) {
        if (GROUP_KEY.equals(conversationKey) || conversationKey == null || conversationKey.equals(username)) {
            return;
        }

        Message receipt = new Message();
        receipt.setType(MessageType.READ_RECEIPT);
        receipt.setFrom(username);
        receipt.setTo(conversationKey);
        receipt.setRead(true);

        socketClient.send(receipt);
    }

    private void handleReadReceipt(Message receipt) {
        String conversationKey = receipt.getFrom();
        List<Message> messages = conversationMessages.get(conversationKey);
        if (messages == null) {
            return;
        }

        boolean changed = false;
        for (Message message : messages) {
            if (isOwnPrivateMessageTo(message, conversationKey) && Boolean.FALSE.equals(message.getRead())) {
                message.setRead(true);
                changed = true;
            }
        }

        if (changed && conversationKey.equals(currentConversation) && !isShowingSearchForCurrentConversation()) {
            renderCurrentConversation(false);
        }
    }

    private boolean isOwnPrivateMessageTo(Message message, String receiver) {
        boolean privateText = MessageType.PRIVATE_CHAT.equals(message.getType());
        boolean privateFile = MessageType.FILE.equals(message.getType()) && !"ALL".equals(message.getTo());
        return (privateText || privateFile)
                && username.equals(message.getFrom())
                && receiver.equals(message.getTo())
                && message.getRead() != null;
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

        JComponent bubbleContent = createBubbleContent(message, ownMessage);
        bubbleContent.setAlignmentX(ownMessage ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);

        bubbleWrapper.add(senderLabel);
        bubbleWrapper.add(Box.createVerticalStrut(4));
        bubbleWrapper.add(bubbleContent);

        JLabel readStatusLabel = createReadStatusLabel(message, ownMessage);
        if (readStatusLabel != null) {
            bubbleWrapper.add(Box.createVerticalStrut(3));
            bubbleWrapper.add(readStatusLabel);
        }

        JLabel avatarLabel = new JLabel(new DefaultAvatarIcon(36));
        avatarLabel.setBorder(new EmptyBorder(16, 0, 0, 0));

        if (ownMessage) {
            rowPanel.add(bubbleWrapper);
            rowPanel.add(avatarLabel);
        } else {
            rowPanel.add(avatarLabel);
            rowPanel.add(bubbleWrapper);
        }
        messagePanel.add(rowPanel);
        messagePanel.add(Box.createVerticalStrut(8));

        messagePanel.revalidate();
        messagePanel.repaint();
    }

    private JLabel createReadStatusLabel(Message message, boolean ownMessage) {
        if (!ownMessage || message.getRead() == null) {
            return null;
        }

        boolean privateText = MessageType.PRIVATE_CHAT.equals(message.getType());
        boolean privateFile = MessageType.FILE.equals(message.getType()) && !"ALL".equals(message.getTo());
        if (!privateText && !privateFile) {
            return null;
        }

        JLabel label = new JLabel(Boolean.TRUE.equals(message.getRead()) ? "已读" : "未读");
        label.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        label.setForeground(Boolean.TRUE.equals(message.getRead()) ? new Color(90, 150, 80) : new Color(150, 150, 150));
        label.setAlignmentX(Component.RIGHT_ALIGNMENT);
        return label;
    }

    private JComponent createBubbleContent(Message message, boolean ownMessage) {
        if (MessageType.FILE.equals(message.getType())) {
            return createFileBubble(message, ownMessage);
        }

        return new TextBubblePanel(message.getContent(), ownMessage);
    }

    private JComponent createFileBubble(Message message, boolean ownMessage) {
        JPanel bubblePanel = new RoundedBubblePanel(new BorderLayout(10, 0), ownMessage);
        bubblePanel.setBorder(new EmptyBorder(8, 10, 8, 10));
        bindFilePreviewActions(bubblePanel, message);

        if (isImageFile(message.getFileName())) {
            ImageIcon thumbnailIcon = createThumbnailIcon(message.getContent(), 190, 130);
            if (thumbnailIcon != null) {
                JPanel imagePanel = new JPanel(new BorderLayout(0, 6));
                imagePanel.setOpaque(false);

                JLabel imageLabel = new JLabel(thumbnailIcon);
                imageLabel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
                imagePanel.add(imageLabel, BorderLayout.CENTER);

                JLabel nameLabel = new JLabel(getFileSummary(message));
                nameLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
                nameLabel.setForeground(new Color(90, 90, 90));
                imagePanel.add(nameLabel, BorderLayout.SOUTH);

                bubblePanel.add(imagePanel, BorderLayout.CENTER);
                return bubblePanel;
            }
        }

        JLabel iconLabel = new JLabel(createGenericFileIcon());
        bubblePanel.add(iconLabel, BorderLayout.WEST);

        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(shortenFileName(message.getFileName()));
        nameLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        nameLabel.setForeground(new Color(35, 35, 35));

        JLabel sizeLabel = new JLabel(formatFileSize(message.getFileSize()));
        sizeLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        sizeLabel.setForeground(new Color(110, 110, 110));

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(sizeLabel);
        bubblePanel.add(infoPanel, BorderLayout.CENTER);

        return bubblePanel;
    }

    private void bindFilePreviewActions(JComponent component, Message message) {
        component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        component.setToolTipText("单击预览，右键保存");
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    showFileActionMenu(component, message, e.getX(), e.getY());
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    previewFile(message);
                }
            }
        });
    }

    private void showFileActionMenu(Component parent, Message message, int x, int y) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem previewItem = new JMenuItem("预览");
        previewItem.addActionListener(e -> previewFile(message));
        menu.add(previewItem);

        JMenuItem saveItem = new JMenuItem("另存为");
        saveItem.addActionListener(e -> saveReceivedFile(message));
        menu.add(saveItem);

        menu.show(parent, x, y);
    }

    private void previewFile(Message message) {
        if (message.getContent() == null || message.getContent().isEmpty()) {
            JOptionPane.showMessageDialog(this, "当前消息没有文件内容，无法预览");
            return;
        }

        try {
            byte[] fileBytes = Base64.getDecoder().decode(message.getContent());
            if (isImageFile(message.getFileName())) {
                previewImage(message, fileBytes);
            } else if (isTextFile(message.getFileName())) {
                previewText(message, fileBytes);
            } else {
                previewWithSystemApplication(message, fileBytes);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "预览失败：" + ex.getMessage());
        }
    }

    private void previewImage(Message message, byte[] fileBytes) throws Exception {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(fileBytes));
        if (image == null) {
            JOptionPane.showMessageDialog(this, "图片格式无法预览");
            return;
        }

        ImageIcon icon = createPreviewImageIcon(image, 760, 520);
        JLabel imageLabel = new JLabel(icon);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane scrollPane = new JScrollPane(imageLabel);
        scrollPane.setBorder(null);

        JDialog dialog = new JDialog(this, "图片预览 - " + message.getFileName(), false);
        dialog.setSize(820, 620);
        dialog.setLocationRelativeTo(this);
        dialog.add(scrollPane);
        dialog.setVisible(true);
    }

    private ImageIcon createPreviewImageIcon(BufferedImage image, int maxWidth, int maxHeight) {
        double scale = Math.min(maxWidth / (double) image.getWidth(), maxHeight / (double) image.getHeight());
        scale = Math.min(scale, 1.0);
        int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(image.getHeight() * scale));
        Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    private void previewText(Message message, byte[] fileBytes) {
        JTextArea textArea = new JTextArea(new String(fileBytes, StandardCharsets.UTF_8));
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        textArea.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);

        JDialog dialog = new JDialog(this, "文本预览 - " + message.getFileName(), false);
        dialog.setSize(720, 540);
        dialog.setLocationRelativeTo(this);
        dialog.add(scrollPane);
        dialog.setVisible(true);
    }

    private void previewWithSystemApplication(Message message, byte[] fileBytes) throws Exception {
        if (!Desktop.isDesktopSupported()) {
            JOptionPane.showMessageDialog(this, "当前系统不支持直接打开预览");
            return;
        }

        Path tempFile = Files.createTempFile("instant-chat-preview-", getFileSuffix(message.getFileName()));
        Files.write(tempFile, fileBytes);
        tempFile.toFile().deleteOnExit();

        Desktop.getDesktop().open(tempFile.toFile());
    }

    private boolean isTextFile(String fileName) {
        if (fileName == null) {
            return false;
        }
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".txt")
                || lowerName.endsWith(".md")
                || lowerName.endsWith(".java")
                || lowerName.endsWith(".json")
                || lowerName.endsWith(".xml")
                || lowerName.endsWith(".yml")
                || lowerName.endsWith(".yaml")
                || lowerName.endsWith(".csv")
                || lowerName.endsWith(".log")
                || lowerName.endsWith(".sql")
                || lowerName.endsWith(".properties")
                || lowerName.endsWith(".html")
                || lowerName.endsWith(".css")
                || lowerName.endsWith(".js");
    }

    private String getFileSuffix(String fileName) {
        if (fileName == null) {
            return ".tmp";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return ".tmp";
        }
        return fileName.substring(dotIndex);
    }

    private Color getBubbleBackground(boolean ownMessage) {
        return ownMessage ? new Color(149, 236, 105) : Color.WHITE;
    }

    private boolean isImageFile(String fileName) {
        if (fileName == null) {
            return false;
        }
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".png")
                || lowerName.endsWith(".jpg")
                || lowerName.endsWith(".jpeg")
                || lowerName.endsWith(".gif")
                || lowerName.endsWith(".bmp")
                || lowerName.endsWith(".webp");
    }

    private ImageIcon createThumbnailIcon(String base64Content, int maxWidth, int maxHeight) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64Content);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                return null;
            }

            double scale = Math.min(maxWidth / (double) image.getWidth(), maxHeight / (double) image.getHeight());
            scale = Math.min(scale, 1.0);
            int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
            int height = Math.max(1, (int) Math.round(image.getHeight() * scale));

            Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
        } catch (Exception e) {
            return null;
        }
    }

    private Icon createGenericFileIcon() {
        return new Icon() {
            @Override
            public int getIconWidth() {
                return 42;
            }

            @Override
            public int getIconHeight() {
                return 46;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(245, 248, 252));
                g2.fillRoundRect(x + 5, y + 2, 30, 40, 5, 5);

                g2.setColor(new Color(87, 132, 186));
                g2.setStroke(new BasicStroke(1.4f));
                g2.drawRoundRect(x + 5, y + 2, 30, 40, 5, 5);

                Polygon fold = new Polygon();
                fold.addPoint(x + 26, y + 2);
                fold.addPoint(x + 35, y + 11);
                fold.addPoint(x + 26, y + 11);
                g2.setColor(new Color(218, 231, 247));
                g2.fillPolygon(fold);
                g2.setColor(new Color(87, 132, 186));
                g2.drawPolygon(fold);

                g2.setStroke(new BasicStroke(1.2f));
                g2.drawLine(x + 11, y + 20, x + 29, y + 20);
                g2.drawLine(x + 11, y + 27, x + 29, y + 27);
                g2.drawLine(x + 11, y + 34, x + 24, y + 34);

                g2.dispose();
            }
        };
    }

    private String getFileSummary(Message message) {
        return shortenFileName(message.getFileName()) + "（" + formatFileSize(message.getFileSize()) + "）";
    }

    private String shortenFileName(String fileName) {
        if (fileName == null || fileName.length() <= 26) {
            return fileName == null ? "未知文件" : fileName;
        }
        int dotIndex = fileName.lastIndexOf('.');
        String suffix = dotIndex >= 0 ? fileName.substring(dotIndex) : "";
        int prefixLength = Math.max(8, 22 - suffix.length());
        return fileName.substring(0, Math.min(prefixLength, fileName.length())) + "..." + suffix;
    }

    private String formatFileSize(Long fileSize) {
        if (fileSize == null) {
            return "未知大小";
        }
        if (fileSize < 1024) {
            return fileSize + "B";
        }
        if (fileSize < 1024 * 1024) {
            return String.format("%.1fKB", fileSize / 1024.0);
        }
        return String.format("%.1fMB", fileSize / 1024.0 / 1024.0);
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalBar = messageScrollPane.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getMaximum());
        });
    }

    private List<Message> mergeHistoryWithCurrent(List<Message> historyMessages, List<Message> currentMessages) {
        List<Message> mergedMessages = new ArrayList<>();
        Set<String> messageKeys = new HashSet<>();

        for (Message message : historyMessages) {
            mergedMessages.add(message);
            messageKeys.add(buildMessageKey(message));
        }

        if (currentMessages != null) {
            for (Message message : currentMessages) {
                String key = buildMessageKey(message);
                if (!messageKeys.contains(key)) {
                    mergedMessages.add(message);
                    messageKeys.add(key);
                }
            }
        }

        return mergedMessages;
    }

    private String buildMessageKey(Message message) {
        return nullToEmpty(message.getType())
                + "|" + nullToEmpty(message.getFrom())
                + "|" + nullToEmpty(message.getTo())
                + "|" + nullToEmpty(message.getContent())
                + "|" + nullToEmpty(message.getFileName())
                + "|" + nullToEmpty(message.getTime());
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private class TextBubblePanel extends RoundedBubblePanel {

        TextBubblePanel(String content, boolean ownMessage) {
            super(new BorderLayout(), ownMessage);
            setBorder(new EmptyBorder(8, 12, 8, 12));

            String text = content == null ? "" : content;
            JTextArea textArea = new JTextArea(text);
            textArea.setEditable(false);
            textArea.setFocusable(false);
            textArea.setOpaque(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(false);
            textArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
            textArea.setForeground(new Color(25, 25, 25));

            FontMetrics metrics = textArea.getFontMetrics(textArea.getFont());
            int textWidth = calculateTextWidth(text, metrics);
            textArea.setSize(new Dimension(textWidth, Short.MAX_VALUE));
            Dimension textSize = textArea.getPreferredSize();
            textArea.setPreferredSize(new Dimension(textWidth, textSize.height));

            int bubbleWidth = textWidth + TEXT_BUBBLE_HORIZONTAL_PADDING;
            int bubbleHeight = textSize.height + TEXT_BUBBLE_VERTICAL_PADDING;
            setPreferredSize(new Dimension(bubbleWidth, bubbleHeight));
            setMaximumSize(new Dimension(bubbleWidth, bubbleHeight));
            add(textArea, BorderLayout.CENTER);
        }

        private int calculateTextWidth(String text, FontMetrics metrics) {
            int maxTextWidth = calculateMaxTextWidth();
            int longestLineWidth = 0;
            String[] lines = text.split("\\R", -1);
            for (String line : lines) {
                longestLineWidth = Math.max(longestLineWidth, metrics.stringWidth(line));
            }
            int contentWidth = Math.max(TEXT_BUBBLE_MIN_TEXT_WIDTH, longestLineWidth + 4);
            return Math.min(maxTextWidth, contentWidth);
        }

        private int calculateMaxTextWidth() {
            int viewportWidth = 0;
            if (messageScrollPane != null && messageScrollPane.getViewport() != null) {
                viewportWidth = messageScrollPane.getViewport().getWidth();
            }
            if (viewportWidth <= 0) {
                viewportWidth = Math.max(520, MainFrame.this.getWidth() - 240);
            }

            int maxBubbleWidth = (int) (viewportWidth * 0.72);
            return Math.max(160, maxBubbleWidth - TEXT_BUBBLE_HORIZONTAL_PADDING);
        }
    }

    private class RoundedBubblePanel extends JPanel {

        private final boolean ownMessage;

        RoundedBubblePanel(LayoutManager layout, boolean ownMessage) {
            super(layout);
            this.ownMessage = ownMessage;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBubbleBackground(ownMessage));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class ContactCellRenderer extends JPanel implements ListCellRenderer<String> {

        private final JLabel avatarLabel = new JLabel(new DefaultAvatarIcon(36));
        private final JLabel nameLabel = new JLabel();
        private final JLabel unreadLabel = new UnreadBadgeLabel();

        ContactCellRenderer() {
            setLayout(new BorderLayout(12, 0));
            setBorder(new EmptyBorder(6, 14, 6, 12));
            setOpaque(true);

            nameLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
            nameLabel.setForeground(new Color(35, 35, 35));

            unreadLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 9));
            unreadLabel.setForeground(Color.WHITE);
            unreadLabel.setHorizontalAlignment(SwingConstants.CENTER);
            unreadLabel.setPreferredSize(new Dimension(18, 18));
            unreadLabel.setOpaque(false);

            add(avatarLabel, BorderLayout.WEST);
            add(nameLabel, BorderLayout.CENTER);
            add(unreadLabel, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends String> list,
                String value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            nameLabel.setText(value);
            int unreadCount = unreadCounts.getOrDefault(toConversationKey(value), 0);
            unreadLabel.setText(unreadCount > 9 ? "9+" : String.valueOf(unreadCount));
            unreadLabel.setVisible(unreadCount > 0);
            setBackground(isSelected ? new Color(225, 225, 225) : new Color(248, 248, 248));
            return this;
        }
    }

    private static class UnreadBadgeLabel extends JLabel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(245, 81, 77));
            int size = Math.min(getWidth(), getHeight());
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            g2.fillOval(x, y, size, size);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
