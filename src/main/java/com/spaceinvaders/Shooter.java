package com.spaceinvaders;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Represents the player's shooter/spaceship.
 */
public class Shooter {
    public int x, y;
    public final int width = 50, height = 20;
    private int dx = 0;
    private int dy = 0;

    public Shooter(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void move() {
        x += dx;
        y += dy;

        if (x < 0) x = 0;
        if (x > 800 - width) x = 800 - width;
        if (y < 0) y = 0;
        if (y > 600 - height) y = 600 - height;
    }

    public void setDirectionX(int dx) {
        this.dx = dx;
    }

    public void setDirectionY(int dy) {
        this.dy = dy;
    }

    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, width, height);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}
