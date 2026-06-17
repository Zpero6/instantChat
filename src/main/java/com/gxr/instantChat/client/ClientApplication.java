package com.gxr.instantChat.client;

import com.gxr.instantChat.client.ui.LoginFrame;

import javax.swing.SwingUtilities;

public class ClientApplication {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
