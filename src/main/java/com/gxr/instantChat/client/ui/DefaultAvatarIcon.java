package com.gxr.instantChat.client.ui;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class DefaultAvatarIcon implements Icon {

    private final int size;

    public DefaultAvatarIcon(int size) {
        this.size = size;
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = Math.max(10, size / 4);
        g2.setColor(new Color(22, 184, 78));
        g2.fillRoundRect(x, y, size, size, arc, arc);

        int largeW = (int) (size * 0.48);
        int largeH = (int) (size * 0.36);
        int largeX = x + (int) (size * 0.18);
        int largeY = y + (int) (size * 0.25);

        int smallW = (int) (size * 0.42);
        int smallH = (int) (size * 0.31);
        int smallX = x + (int) (size * 0.42);
        int smallY = y + (int) (size * 0.43);

        g2.setColor(Color.WHITE);
        g2.fillOval(largeX, largeY, largeW, largeH);
        g2.fillOval(smallX, smallY, smallW, smallH);

        g2.setStroke(new BasicStroke(Math.max(1f, size / 36f)));
        g2.setColor(new Color(22, 184, 78));
        int dot = Math.max(2, size / 14);
        g2.fillOval(largeX + largeW / 3 - dot / 2, largeY + largeH / 2 - dot / 2, dot, dot);
        g2.fillOval(largeX + largeW * 2 / 3 - dot / 2, largeY + largeH / 2 - dot / 2, dot, dot);
        g2.fillOval(smallX + smallW / 3 - dot / 2, smallY + smallH / 2 - dot / 2, dot, dot);
        g2.fillOval(smallX + smallW * 2 / 3 - dot / 2, smallY + smallH / 2 - dot / 2, dot, dot);

        g2.dispose();
    }
}
