package com.gxr.instantChat.client.ui;

import com.gxr.instantChat.common.Message;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class HistoryDialog extends JDialog {

    private final JTextArea historyArea = new JTextArea();

    public HistoryDialog(Frame owner, String conversationTitle, List<Message> historyMessages, String username) {
        super(owner, "历史记录 - " + conversationTitle, true);

        setSize(560, 460);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        initView(conversationTitle, historyMessages, username);
    }

    private void initView(String conversationTitle, List<Message> historyMessages, String username) {
        JLabel titleLabel = new JLabel(conversationTitle);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        titleLabel.setBorder(new EmptyBorder(16, 18, 12, 18));
        add(titleLabel, BorderLayout.NORTH);

        historyArea.setEditable(false);
        historyArea.setLineWrap(true);
        historyArea.setWrapStyleWord(true);
        historyArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        historyArea.setBorder(new EmptyBorder(12, 14, 12, 14));
        historyArea.setText(buildHistoryText(historyMessages, username));
        historyArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(historyArea);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(225, 225, 225)));
        add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private String buildHistoryText(List<Message> historyMessages, String username) {
        if (historyMessages == null || historyMessages.isEmpty()) {
            return "暂无历史记录";
        }

        StringBuilder builder = new StringBuilder();
        for (Message message : historyMessages) {
            String sender = username.equals(message.getFrom()) ? "我" : message.getFrom();
            if (message.getTime() != null && !message.getTime().isEmpty()) {
                builder.append("[").append(message.getTime()).append("] ");
            }
            builder.append(sender).append("：").append(getDisplayContent(message)).append("\n\n");
        }
        return builder.toString();
    }

    private String getDisplayContent(Message message) {
        if (message.getFileName() != null && !message.getFileName().isEmpty()) {
            return "[文件] " + message.getFileName() + "（" + formatFileSize(message.getFileSize()) + "）";
        }
        return message.getContent();
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
}
