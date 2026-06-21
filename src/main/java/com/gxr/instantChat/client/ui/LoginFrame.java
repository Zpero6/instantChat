package com.gxr.instantChat.client.ui;

import com.gxr.instantChat.client.socket.SocketClient;
import com.gxr.instantChat.common.Message;
import com.gxr.instantChat.common.MessageType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

    private static final String USERNAME_PATTERN = "^[A-Za-z0-9_]{3,20}$";
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 20;

    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JButton loginButton = new JButton("登录");
    private final JButton registerButton = new JButton("注册账号");

    private SocketClient socketClient;

    public LoginFrame() {
        setTitle("校园即时通信系统");
        setSize(720, 440);
        setMinimumSize(new Dimension(640, 400));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initView();
        connectServer();
        initEvents();
    }

    private void initView() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(Color.WHITE);

        rootPanel.add(createBrandPanel(), BorderLayout.WEST);
        rootPanel.add(createFormPanel(), BorderLayout.CENTER);

        setContentPane(rootPanel);
    }

    private JPanel createBrandPanel() {
        JPanel brandPanel = new JPanel(new GridBagLayout());
        brandPanel.setPreferredSize(new Dimension(260, 0));
        brandPanel.setBackground(new Color(22, 184, 78));

        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JLabel avatarLabel = new JLabel(new DefaultAvatarIcon(82));
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("校园即时通信");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subTitleLabel = new JLabel("聊天 · 文件 · 校园协作");
        subTitleLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        subTitleLabel.setForeground(new Color(230, 255, 238));
        subTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(avatarLabel);
        contentPanel.add(Box.createVerticalStrut(22));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(subTitleLabel);

        brandPanel.add(contentPanel);
        return brandPanel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(34, 52, 34, 52));

        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setPreferredSize(new Dimension(320, 300));

        JLabel titleLabel = new JLabel("欢迎登录");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 26));
        titleLabel.setForeground(new Color(30, 30, 30));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tipLabel = new JLabel("使用账号进入校园局域网聊天");
        tipLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        tipLabel.setForeground(new Color(130, 130, 130));
        tipLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        styleTextField(usernameField, "用户名");
        styleTextField(passwordField, "密码");
        stylePrimaryButton(loginButton);
        styleSecondaryButton(registerButton);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(tipLabel);
        contentPanel.add(Box.createVerticalStrut(28));
        contentPanel.add(usernameField);
        contentPanel.add(Box.createVerticalStrut(14));
        contentPanel.add(passwordField);
        contentPanel.add(Box.createVerticalStrut(22));
        contentPanel.add(loginButton);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(registerButton);

        formPanel.add(contentPanel);
        return formPanel;
    }

    private void styleTextField(JTextField field, String tooltip) {
        field.setToolTipText(tooltip);
        field.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        field.setPreferredSize(new Dimension(320, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                new EmptyBorder(6, 4, 6, 4)
        ));
    }

    private void stylePrimaryButton(JButton button) {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.setFont(new Font("Microsoft YaHei", Font.BOLD, 15));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(22, 184, 78));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
    }

    private void styleSecondaryButton(JButton button) {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        button.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        button.setForeground(new Color(22, 140, 66));
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(22, 184, 78)));
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
        passwordField.addActionListener(e -> sendLogin());
    }

    private void sendLogin() {
        if (!socketClient.isConnected()) {
            JOptionPane.showMessageDialog(this, "尚未连接服务器，请先启动服务端后重新打开客户端");
            return;
        }
        if (!validateUserInput()) {
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
        if (!validateUserInput()) {
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

    private boolean validateUserInput() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名不能为空");
            usernameField.requestFocusInWindow();
            return false;
        }
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "密码不能为空");
            passwordField.requestFocusInWindow();
            return false;
        }
        if (!username.matches(USERNAME_PATTERN)) {
            JOptionPane.showMessageDialog(this, "用户名必须是 3-20 位字母、数字或下划线");
            usernameField.requestFocusInWindow();
            return false;
        }
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            JOptionPane.showMessageDialog(this, "密码长度必须是 6-20 位");
            passwordField.requestFocusInWindow();
            return false;
        }
        if (password.contains(" ")) {
            JOptionPane.showMessageDialog(this, "密码不能包含空格");
            passwordField.requestFocusInWindow();
            return false;
        }

        return true;
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
            socketClient.setMessageConsumer(mainFrame::handleMessage);
            mainFrame.handleMessage(message);
            mainFrame.setVisible(true);
            dispose();
        }
    }
}
