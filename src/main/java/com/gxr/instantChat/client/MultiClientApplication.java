package com.gxr.instantChat.client;

import com.gxr.instantChat.client.ui.LoginFrame;

import javax.swing.SwingUtilities;

public class MultiClientApplication {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame firstClient = new LoginFrame();
            firstClient.setLocation(260, 160);
            firstClient.setVisible(true);

            LoginFrame secondClient = new LoginFrame();
            secondClient.setLocation(660, 160);
            secondClient.setVisible(true);
        });
    }
}
