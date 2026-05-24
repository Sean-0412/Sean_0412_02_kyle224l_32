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
    public boolean shielded;
    public boolean blue;
    public static final int WIDTH = 36;
    public static final int HEIGHT = 24;
    public static final int SHIELDED_WAIT_FRAMES = 180;
    public static final double SHIELDED_DROP_SPEED = 14.0;

    // Green enemies shoot independently. Each green enemy gets one fixed
    // random interval when it is created: 1~15 seconds, using the game's
    // existing 60-frames-per-second timing convention.
    public static final int GREEN_MIN_SHOOT_INTERVAL_FRAMES = 60;
    public static final int GREEN_MAX_SHOOT_INTERVAL_FRAMES = 900;

    private boolean bossUseCos;
    private double bossPhase;
    private double bossFrequency;
    private double bossAmplitude;
    private double bossBaseY;
    private int bossDir = 1;

    public int shieldedWait;
    public int greenShootIntervalFrames;
    public int greenShootTimer;

    public Alien(int x, int y) {
        this(x, y, 1, false);
    }

    public Alien(int x, int y, int health, boolean boss) {
        this(x, y, health, boss, false);
    }

    public Alien(int x, int y, int health, boolean boss, boolean shielded) {
        this(x, y, health, boss, shielded, SHIELDED_WAIT_FRAMES);
    }

    public Alien(int x, int y, int health, boolean boss, boolean shielded, int shieldedWait) {
        this(x, y, health, boss, shielded, shieldedWait, false);
    }

    public Alien(int x, int y, int health, boolean boss, boolean shielded, int shieldedWait, boolean blue) {
        this.x = x;
        this.y = y;
        this.health = health;
        this.boss = boss;
        this.shielded = shielded;
        this.blue = blue;
        this.dx = 1;
        this.shieldedWait = shielded ? shieldedWait : 0;
        this.greenShootIntervalFrames = 0;
        this.greenShootTimer = 0;
    }

    public boolean isGreenShooter() {
        return !boss && !shielded && !blue;
    }

    public void initGreenShooting(Random random) {
        greenShootIntervalFrames = GREEN_MIN_SHOOT_INTERVAL_FRAMES
                + random.nextInt(GREEN_MAX_SHOOT_INTERVAL_FRAMES - GREEN_MIN_SHOOT_INTERVAL_FRAMES + 1);
        greenShootTimer = greenShootIntervalFrames;
    }

    public boolean updateGreenShootTimer() {
        if (!isGreenShooter() || greenShootIntervalFrames <= 0) {
            return false;
        }
        if (greenShootTimer > 0) {
            greenShootTimer--;
            return false;
        }
        greenShootTimer = greenShootIntervalFrames;
        return true;
    }

    public void resetGreenShootTimer() {
        greenShootTimer = greenShootIntervalFrames;
    }

    public void initBossMovement(Random random, double maxAmplitude) {
        bossBaseY = y;
        bossUseCos = random.nextBoolean();
        bossAmplitude = Math.max(20, maxAmplitude * (0.25 + random.nextDouble() * 0.25));
        bossFrequency = 0.015 + random.nextDouble() * 0.02;
        bossPhase = 0.0;
        bossDir = 1;
    }

    public void move(double dx, double dy) {
        x += dx;
        y += dy;
    }

    public void updateMovement(double speed, Random random, int minX, int maxX, double dropSpeed) {
        if (random.nextInt(100) < 4) {
            dx = random.nextBoolean() ? 1 : -1;
        }

        // Move and apply drop
        double horizFactor = 1.0; // keep horizontal unchanged by default
        double vertFactor = boss ? 0.5 : 1.0; // Boss vertical movement (drop) reduced
        double nextX = x + dx * speed * horizFactor;
        double nextY = y + dropSpeed * vertFactor;

        // Clamp horizontally and reverse direction when hitting bounds
        if (nextX < minX) {
            x = minX;
            if (dx < 0) dx = 1;
            if (boss) System.out.println("[Alien] Boss hit left bound: nextX=" + nextX + " clamped to " + x + " dx=" + dx + " speed=" + speed);
        } else if (nextX > maxX) {
            x = maxX;
            if (dx > 0) dx = -1;
            if (boss) System.out.println("[Alien] Boss hit right bound: nextX=" + nextX + " clamped to " + x + " dx=" + dx + " speed=" + speed);
        } else {
            x = nextX;
        }
        y = nextY;
    }

    public void updateBossMovement(double speed, Random random, int minX, int maxX, double maxAmplitude) {
        if (bossAmplitude == 0 && bossFrequency == 0) {
            initBossMovement(random, maxAmplitude);
        }

        x += bossDir * speed;
        bossPhase += bossFrequency;
        double offset = bossUseCos ? Math.cos(bossPhase) : Math.sin(bossPhase);
        y = bossBaseY + bossAmplitude * offset;

        if (x < minX) {
            x = minX;
            bossDir = 1;
        } else if (x > maxX) {
            x = maxX;
            bossDir = -1;
        }

        if (y < 40) {
            y = 40;
            bossBaseY = 40;
        } else if (y > (GamePanel.HEIGHT / 2.0) - HEIGHT) {
            y = (GamePanel.HEIGHT / 2.0) - HEIGHT;
            bossBaseY = y;
        }
    }

    public void updateShieldedMovement() {
        if (!shielded) {
            return;
        }
        if (shieldedWait > 0) {
            shieldedWait--;
            return;
        }
        y += SHIELDED_DROP_SPEED;
    }

    public void draw(Graphics g) {
        if (boss) {
            g.setColor(new Color(255, 110, 80));
            g.fillRoundRect((int) x, (int) y, WIDTH, HEIGHT, 8, 8);

            g.setColor(new Color(180, 40, 40));
            g.fillRoundRect((int) x + 2, (int) y + 3, WIDTH - 4, HEIGHT - 6, 8, 8);

            g.setColor(new Color(255, 220, 120));
            g.fillOval((int) x + 7, (int) y + 7, 6, 6);
            g.fillOval((int) x + WIDTH - 13, (int) y + 7, 6, 6);

            g.setColor(new Color(255, 180, 90));
            g.fillArc((int) x - 5, (int) y + 2, 12, 16, 90, 180);
            g.fillArc((int) x + WIDTH - 7, (int) y + 2, 12, 16, 270, 180);

            g.setColor(new Color(120, 30, 30));
            int[] leftHornX = {(int) x + 8, (int) x + 14, (int) x + 10};
            int[] leftHornY = {(int) y + 2, (int) y - 10, (int) y + 4};
            int[] rightHornX = {(int) x + WIDTH - 8, (int) x + WIDTH - 14, (int) x + WIDTH - 10};
            int[] rightHornY = {(int) y + 2, (int) y - 10, (int) y + 4};
            g.fillPolygon(leftHornX, leftHornY, 3);
            g.fillPolygon(rightHornX, rightHornY, 3);

            g.setColor(new Color(255, 140, 90, 120));
            g.fillArc((int) x - 12, (int) y + 4, 10, 18, 90, 180);
            g.fillArc((int) x + WIDTH + 2, (int) y + 4, 10, 18, 270, 180);
        } else {
            if (shielded) {
                g.setColor(new Color(60, 140, 255));
                g.fillRoundRect((int) x, (int) y, WIDTH, HEIGHT, 8, 8);
                g.setColor(new Color(20, 60, 140));
                g.drawRoundRect((int) x, (int) y, WIDTH, HEIGHT, 8, 8);

                g.setColor(new Color(120, 200, 255, 160));
                g.drawOval((int) x - 6, (int) y - 6, WIDTH + 12, HEIGHT + 12);
                g.setColor(new Color(140, 220, 255, 200));
                g.drawOval((int) x - 2, (int) y - 2, WIDTH + 4, HEIGHT + 4);

                g.setColor(new Color(230, 245, 255));
                g.fillOval((int) x + 9, (int) y + 7, 6, 6);
                g.fillOval((int) x + 21, (int) y + 7, 6, 6);
            } else if (blue) {
                g.setColor(new Color(80, 170, 255));
                g.fillRoundRect((int) x, (int) y, WIDTH, HEIGHT, 6, 6);
                g.setColor(new Color(20, 70, 130));
                g.fillOval((int) x + 8, (int) y + 7, 6, 6);
                g.fillOval((int) x + 22, (int) y + 7, 6, 6);
            } else {
                g.setColor(new Color(150, 255, 80));
                g.fillRoundRect((int) x, (int) y, WIDTH, HEIGHT, 6, 6);
                g.setColor(new Color(30, 80, 20));
                g.fillOval((int) x + 8, (int) y + 7, 6, 6);
                g.fillOval((int) x + 22, (int) y + 7, 6, 6);
            }
        }
    }

    public Rectangle2D.Double getBounds() {
        return new Rectangle2D.Double(x, y, WIDTH, HEIGHT);
    }
}
