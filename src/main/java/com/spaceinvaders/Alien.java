package com.spaceinvaders;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

/**
 * Represents an alien enemy in the Space Invaders game.
 */
public class Alien {
    public double x;
    public double y;
    public static final int WIDTH = 36;
    public static final int HEIGHT = 24;

    public Alien(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void move(double dx, double dy) {
        x += dx;
        y += dy;
    }

    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect((int) x, (int) y, WIDTH, HEIGHT);
    }

    public Rectangle2D.Double getBounds() {
        return new Rectangle2D.Double(x, y, WIDTH, HEIGHT);
    }
}
