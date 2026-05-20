package com.spaceinvaders;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class PowerUp {
    public static final int TYPE_HEALTH = 0;
    public static final int TYPE_ATTACK = 1;
    public static final int TYPE_SHIELD = 2;

    public int x;
    public int y;
    public final int type;
    public final int size;
    public final int dy;

    public PowerUp(int x, int y, int type, int size, int dy) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.size = size;
        this.dy = dy;
    }

    public void update() {
        y += dy;
    }

    public void draw(Graphics g) {
        if (type == TYPE_HEALTH) {
            g.setColor(new Color(100, 255, 140));
        } else {
            g.setColor(new Color(255, 180, 80));
        }
        g.fillOval(x, y, size, size);
        g.setColor(Color.WHITE);
        if (type == TYPE_HEALTH) {
            int crossSize = size / 2;
            int centerX = x + size / 2;
            int centerY = y + size / 2;
            g.fillRect(centerX - 2, centerY - crossSize / 2, 4, crossSize);
            g.fillRect(centerX - crossSize / 2, centerY - 2, crossSize, 4);
        } else if (type == TYPE_ATTACK) {
            int cx = x + size / 2;
            int cy = y + size / 2;
            g.fillRect(cx - 2, cy - 6, 4, 12);
            g.fillRect(cx - 6, cy - 2, 12, 4);
        } else if (type == TYPE_SHIELD) {
            int cx = x + size / 2;
            int cy = y + size / 2;
            g.drawOval(cx - size / 3, cy - size / 3, size * 2 / 3, size * 2 / 3);
            g.fillOval(cx - 2, cy - 8, 4, 4);
            g.fillRect(cx - 2, cy - 4, 4, 10);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, size, size);
    }
}
