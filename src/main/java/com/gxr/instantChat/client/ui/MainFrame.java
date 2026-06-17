package com.gxr.instantChat.client.ui;


import com.gxr.instantChat.client.socket.SocketClient;

import javax.swing.*;

public class MainFrame extends JFrame {

    public MainFrame(String username, SocketClient socketClient) {
        setTitle("校园即时通信系统 - " + username);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        add(new JLabel("登录成功：" + username, SwingConstants.CENTER));
    }
}
