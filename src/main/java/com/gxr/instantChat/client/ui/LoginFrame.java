package com.gxr.instantChat.client.ui;


import com.gxr.instantChat.client.socket.SocketClient;
import com.gxr.instantChat.common.Message;
import com.gxr.instantChat.common.MessageType;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JButton loginButton = new JButton("登录");
    private final JButton registerButton = new JButton("注册");

    private SocketClient socketClient;

    public LoginFrame() {
        setTitle("校园即时通信系统 - 登录");
        setSize(360, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initView();
        connectServer();
        initEvents();
    }

    private void initView() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        panel.add(new JLabel("用户名："));
        panel.add(usernameField);
        panel.add(new JLabel("密码："));
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(registerButton);

        add(panel);
    }

    private void connectServer() {
        socketClient = new SocketClient();

        try {
            socketClient.connect("127.0.0.1", 9000, this::handleServerMessage);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "连接服务器失败，请先启动服务端");
            loginButton.setEnabled(false);
            registerButton.setEnabled(false);
        }
    }

    private void initEvents() {
        loginButton.addActionListener(e -> sendLogin());
        registerButton.addActionListener(e -> sendRegister());
    }

    private void sendLogin() {
        if (!socketClient.isConnected()) {
            JOptionPane.showMessageDialog(this, "尚未连接服务器，请先启动服务端后重新打开客户端");
            return;
        }
        Message message = buildUserMessage(MessageType.LOGIN);
        socketClient.send(message);
    }

    private void sendRegister() {
        if (!socketClient.isConnected()) {
            JOptionPane.showMessageDialog(this, "尚未连接服务器，请先启动服务端后重新打开客户端");
            return;
        }
        Message message = buildUserMessage(MessageType.REGISTER);
        socketClient.send(message);
    }

    private Message buildUserMessage(String type) {
        Message message = new Message();
        message.setType(type);
        message.setFrom(usernameField.getText().trim());
        message.setContent(new String(passwordField.getPassword()));
        return message;
    }

    private void handleServerMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            if (MessageType.LOGIN_RESULT.equals(message.getType())) {
                handleLoginResult(message);
            } else if (MessageType.REGISTER_RESULT.equals(message.getType())) {
                JOptionPane.showMessageDialog(this, message.getReason());
            }
        });
    }

    private void handleLoginResult(Message message) {
        JOptionPane.showMessageDialog(this, message.getReason());

        if (Boolean.TRUE.equals(message.getSuccess())) {
            String username = usernameField.getText().trim();
            MainFrame mainFrame = new MainFrame(username, socketClient);
            mainFrame.setVisible(true);
            dispose();
        }
    }
}
