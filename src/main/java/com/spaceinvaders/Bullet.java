package com.spaceinvaders;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Represents a bullet fired by the player.
 */
public class Bullet {
    public int x;
    public int y;
    public int dx;
    public int dy;
    public boolean enemy;
    public static final int SPEED = 10;
    public static final int WIDTH = 5;
    public static final int HEIGHT = 10;

    public Bullet(int x, int y) {
        this(x, y, 0, -SPEED, false);
    }

    public Bullet(int x, int y, int dy, boolean enemy) {
        this(x, y, 0, dy, enemy);
    }

    public Bullet(int x, int y, int dx, int dy, boolean enemy) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.enemy = enemy;
    }

    public void update() {
        x += dx;
        y += dy;
    }

    public void draw(Graphics g) {
        g.setColor(enemy ? new Color(255, 120, 80) : Color.WHITE);
        g.fillRect(x, y, WIDTH, HEIGHT);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, WIDTH, HEIGHT);
    }
}
