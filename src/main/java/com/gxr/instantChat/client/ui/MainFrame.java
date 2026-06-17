package com.gxr.instantChat.client.ui;

import com.gxr.instantChat.client.socket.SocketClient;
import com.gxr.instantChat.common.Message;
import com.gxr.instantChat.common.MessageType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {

    private final String username;
    private final SocketClient socketClient;

    private final DefaultListModel<String> onlineUserModel = new DefaultListModel<>();
    private final JList<String> onlineUserList = new JList<>(onlineUserModel);

    private final JTextArea chatArea = new JTextArea();
    private final JTextField inputField = new JTextField();

    private final JButton privateSendButton = new JButton("发送私聊");
    private final JButton groupSendButton = new JButton("发送群聊");

    public MainFrame(String username, SocketClient socketClient) {
        this.username = username;
        this.socketClient = socketClient;

        setTitle("校园即时通信系统 - " + username);
        setSize(800, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initView();
        initEvents();
    }

    private void initView() {
        setLayout(new BorderLayout());

        onlineUserList.setBorder(BorderFactory.createTitledBorder("在线用户"));
        onlineUserList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(onlineUserList), BorderLayout.WEST);

        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setBorder(BorderFactory.createTitledBorder("聊天内容"));
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(inputField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(privateSendButton);
        buttonPanel.add(groupSendButton);

        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void initEvents() {
        privateSendButton.addActionListener(e -> sendPrivateMessage());
        groupSendButton.addActionListener(e -> sendGroupMessage());

        inputField.addActionListener(e -> sendPrivateMessage());

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
                    appendPrivateMessage(message);
                    break;
                case MessageType.GROUP_CHAT:
                    appendGroupMessage(message);
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
        onlineUserModel.clear();

        if (message.getOnlineUsers() == null) {
            return;
        }

        for (String onlineUser : message.getOnlineUsers()) {
            if (!username.equals(onlineUser)) {
                onlineUserModel.addElement(onlineUser);
            }
        }
    }

    private void sendPrivateMessage() {
        String to = onlineUserList.getSelectedValue();
        String content = inputField.getText().trim();

        if (to == null) {
            JOptionPane.showMessageDialog(this, "请先选择一个在线用户");
            return;
        }

        if (content.isEmpty()) {
            return;
        }

        Message message = new Message();
        message.setType(MessageType.PRIVATE_CHAT);
        message.setFrom(username);
        message.setTo(to);
        message.setContent(content);

        socketClient.send(message);
        inputField.setText("");
    }

    private void sendGroupMessage() {
        String content = inputField.getText().trim();

        if (content.isEmpty()) {
            return;
        }

        Message message = new Message();
        message.setType(MessageType.GROUP_CHAT);
        message.setFrom(username);
        message.setTo("ALL");
        message.setContent(content);

        socketClient.send(message);
        inputField.setText("");
    }

    private void appendPrivateMessage(Message message) {
        chatArea.append("[私聊] " + message.getFrom() + " -> " + message.getTo() + "：" + message.getContent() + "\n");
    }

    private void appendGroupMessage(Message message) {
        chatArea.append("[群聊] " + message.getFrom() + "：" + message.getContent() + "\n");
    }
}
