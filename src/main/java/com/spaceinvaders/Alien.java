package com.spaceinvaders;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.Random;

/**
 * Represents an alien enemy in the Space Invaders game.
 */
public class Alien {
    public double x;
    public double y;
    public double dx;
    public int health;
    public boolean boss;
    public static final int WIDTH = 36;
    public static final int HEIGHT = 24;

    public Alien(int x, int y) {
        this(x, y, 1, false);
    }

    public Alien(int x, int y, int health, boolean boss) {
        this.x = x;
        this.y = y;
        this.health = health;
        this.boss = boss;
        this.dx = 1;
    }

    public void move(double dx, double dy) {
        x += dx;
        y += dy;
    }

    public void updateMovement(double speed, Random random, int minX, int maxX, double dropSpeed) {
        if (random.nextInt(100) < 4) {
            dx = random.nextBoolean() ? 1 : -1;
        }

        x += dx * speed;
        y += dropSpeed;

        if (x < minX) {
            x = minX;
            dx = 1;
        } else if (x > maxX) {
            x = maxX;
            dx = -1;
        }
    }

    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect((int) x, (int) y, WIDTH, HEIGHT);
    }

    public Rectangle2D.Double getBounds() {
        return new Rectangle2D.Double(x, y, WIDTH, HEIGHT);
    }
}
