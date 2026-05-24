package com.spaceinvaders;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private int x, y;
    private int lives;
    private final int playerWidth;
    private final int playerHeight;
    private final int playerSpeed;
    private final int playerVerticalSpeed;
    private final int maxBullets;
    private final int startLives;
    private final int gameMode;
    private final boolean isPlayer1;

    private boolean moveLeft, moveRight, moveUp, moveDown, shootPressed;
    private int fireCooldown;
    private int attackBoostRemaining;
    private int attackMultiplier;
    private int shotCount;
    private int shieldRemaining;
    private int invincibleRemaining;
    
    private boolean ultimateActive;
    private int ultimateDuration;
    private int ultimateCooldown;
    private int ultimateFireCounter;

    private final List<Bullet> bullets = new ArrayList<>();

    public Player(int startX, int startY, int gameMode, boolean isPlayer1, int startLives) {
        this.x = startX;
        this.y = startY;
        this.gameMode = gameMode;
        this.isPlayer1 = isPlayer1;
        this.startLives = startLives;
        this.lives = startLives;

        this.playerWidth = 70;
        this.playerHeight = 18;
        this.playerSpeed = 7;
        this.playerVerticalSpeed = 5;
        this.maxBullets = 12;
        
        this.attackMultiplier = 1;
        this.shotCount = 1;
    }

    public void update() {
        if (lives <= 0) return;

        // Movement
        if (moveLeft) x -= playerSpeed;
        if (moveRight) x += playerSpeed;
        if (gameMode == GamePanel.MODE_DODGING || gameMode == GamePanel.MODE_STAGE) {
            if (moveUp) y -= playerVerticalSpeed;
            if (moveDown) y += playerVerticalSpeed;
        }

        // Clamp position
        if (x < 0) x = 0;
        if (x > GamePanel.WIDTH - playerWidth) x = GamePanel.WIDTH - playerWidth;
        if (gameMode == GamePanel.MODE_DODGING || gameMode == GamePanel.MODE_STAGE) {
            int minY = GamePanel.HEIGHT / 2;
            if (y < minY) y = minY;
            if (y > GamePanel.HEIGHT - playerHeight - 10) y = GamePanel.HEIGHT - playerHeight - 10;
        } else {
            y = GamePanel.HEIGHT - 70;
        }

        // Shooting
        if (shootPressed && fireCooldown == 0 && bullets.size() < maxBullets) {
            shoot();
        }
        
        // Ultimate
        if (ultimateActive) {
            updateUltimate();
        }

        if (fireCooldown > 0) fireCooldown--;
        if (attackBoostRemaining > 0) attackBoostRemaining--;
        else attackMultiplier = 1;
        if (shieldRemaining > 0) shieldRemaining--;
        if (invincibleRemaining > 0) invincibleRemaining--;
        if (ultimateCooldown > 0) ultimateCooldown--;
        updateBullets();
    }

    private void shoot() {
        int bulletY = y - 12; // BULLET_HEIGHT
        int effectiveShotCount = getEffectiveShotCount();
        int shotsToSpawn = Math.min(effectiveShotCount, maxBullets - bullets.size());
        int[] offsets = getShotOffsets(shotsToSpawn);
        for (int offset : offsets) {
            bullets.add(new Bullet(x + offset, bulletY));
        }
        fireCooldown = 10;
        SoundPlayer.playShoot();
    }
    
    private void updateUltimate() {
        ultimateDuration--;
        if (ultimateDuration <= 0) {
            ultimateActive = false;
            ultimateCooldown = 600; // ULTIMATE_COOLDOWN
        } else {
            ultimateFireCounter++;
            if (ultimateFireCounter % 3 == 0) { // ULTIMATE_FIRE_RATE
                int bulletY = y - 12;
                if (bullets.size() < maxBullets) bullets.add(new Bullet(x + 10, bulletY));
                if (bullets.size() < maxBullets) bullets.add(new Bullet(x + (playerWidth / 2) - 2, bulletY));
                if (bullets.size() < maxBullets) bullets.add(new Bullet(x + playerWidth - 20, bulletY));
                SoundPlayer.playShoot();
            }
        }
    }

    private void updateBullets() {
        bullets.removeIf(bullet -> {
            bullet.update();
            return bullet.y + Bullet.HEIGHT < 0 || bullet.y > GamePanel.HEIGHT || bullet.x + Bullet.WIDTH < 0 || bullet.x > GamePanel.WIDTH;
        });
    }

    public void draw(Graphics2D g2) {
        boolean dead = lives <= 0;
        boolean flicker = invincibleRemaining > 0 && (invincibleRemaining / 6) % 2 == 0;
        if (flicker) {
            return;
        }
        Color hullColor, outlineColor, cockpitFill, cockpitOutline, thrusterColor, thrusterAccent;

        if (isPlayer1) {
            hullColor = dead ? new Color(140, 140, 140) : new Color(50, 150, 255);
            outlineColor = dead ? new Color(180, 180, 180) : new Color(100, 180, 255);
            cockpitFill = dead ? new Color(190, 190, 190) : new Color(150, 255, 150);
            cockpitOutline = dead ? new Color(170, 170, 170) : new Color(100, 200, 100);
            thrusterColor = dead ? new Color(170, 170, 170) : new Color(255, 100, 50);
            thrusterAccent = dead ? new Color(200, 200, 200) : new Color(255, 200, 100);
        } else {
            hullColor = dead ? new Color(140, 140, 140) : new Color(255, 150, 50);
            outlineColor = dead ? new Color(180, 180, 180) : new Color(255, 200, 100);
            cockpitFill = dead ? new Color(200, 200, 200) : new Color(255, 220, 150);
            cockpitOutline = dead ? new Color(170, 170, 170) : new Color(200, 160, 100);
            thrusterColor = dead ? new Color(190, 190, 190) : new Color(255, 180, 100);
            thrusterAccent = thrusterColor;
        }

        int[] hullX = { x + playerWidth / 2, x + 5, x + playerWidth - 5 };
        int[] hullY = { y, y + playerHeight, y + playerHeight };
        Polygon hull = new Polygon(hullX, hullY, 3);
        g2.setColor(hullColor);
        g2.fillPolygon(hull);

        g2.setColor(outlineColor);
        g2.setStroke(new java.awt.BasicStroke(2));
        g2.drawPolygon(hull);

        int cockpitX = x + playerWidth / 2 - 6;
        int cockpitY = y + 4;
        g2.setColor(cockpitFill);
        g2.fillOval(cockpitX, cockpitY, 12, 8);
        g2.setColor(cockpitOutline);
        g2.drawOval(cockpitX, cockpitY, 12, 8);

        g2.setColor(thrusterColor);
        g2.fillRect(x + 8, y + playerHeight - 4, 6, 4);
        g2.fillRect(x + playerWidth - 14, y + playerHeight - 4, 6, 4);
        g2.setColor(thrusterAccent);
        g2.fillRect(x + playerWidth / 2 - 2, y + playerHeight - 2, 4, 2);

        if (shieldRemaining > 0 && !dead) {
            g2.setColor(new Color(80, 200, 255, 120));
            g2.setStroke(new java.awt.BasicStroke(4));
            g2.drawOval(x - 8, y - 8, playerWidth + 16, playerHeight + 16);
        }
    }

    public void handleHit() {
        if (shieldRemaining > 0) {
            shieldRemaining = 0;
            SoundPlayer.playExplosion();
            return;
        }
        if (lives > 0) {
            lives--;
            SoundPlayer.playExplosion();
            if (lives > 0) {
                resetPosition();
                invincibleRemaining = 120;
            }
        }
    }

    public void applyPowerUp(int type) {
        if (type == PowerUp.TYPE_HEALTH) {
            lives = Math.min(lives + 1, startLives + 3);
        } else if (type == PowerUp.TYPE_ATTACK) {
            attackMultiplier *= 2;
            attackBoostRemaining = 600; // ATTACK_BOOST_DURATION
        } else if (type == PowerUp.TYPE_SHIELD) {
            shieldRemaining = 300; // SHIELD_DURATION
        }
    }
    
    public void activateUltimate() {
        if (ultimateCooldown == 0) {
            ultimateActive = true;
            ultimateDuration = 300; // ULTIMATE_DURATION
            ultimateFireCounter = 0;
            SoundPlayer.playShoot();
        }
    }

    public void reset() {
        lives = startLives;
        attackBoostRemaining = 0;
        attackMultiplier = 1;
        shieldRemaining = 0;
        invincibleRemaining = 0;
        ultimateActive = false;
        ultimateDuration = 0;
        ultimateCooldown = 0;
        shotCount = 1;
        bullets.clear();
        resetPosition();
        resetControls();
    }

    private void resetPosition() {
        this.x = (GamePanel.WIDTH - playerWidth) / 2;
        this.y = isPlayer1 ? GamePanel.HEIGHT - 70 : GamePanel.HEIGHT - 110;
    }
    
    public void resetControls() {
        moveLeft = false;
        moveRight = false;
        moveUp = false;
        moveDown = false;
        shootPressed = false;
        fireCooldown = 0;
    }

    private int getEffectiveShotCount() {
        return Math.max(1, shotCount * attackMultiplier);
    }

    private int[] getShotOffsets(int count) {
        int[] offsets = new int[count];
        int maxOffset = playerWidth - 4; // BULLET_WIDTH
        if (count == 1) {
            offsets[0] = maxOffset / 2;
            return offsets;
        }
        for (int i = 0; i < count; i++) {
            offsets[i] = (int) Math.round((double) i * maxOffset / (count - 1));
        }
        return offsets;
    }

    // Getters and Setters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return playerWidth; }
    public int getHeight() { return playerHeight; }
    public int getLives() { return lives; }
    public List<Bullet> getBullets() { return bullets; }
    public Rectangle getBounds() { return new Rectangle(x, y, playerWidth, playerHeight); }
    public boolean isAlive() { return lives > 0; }
    public int getAttackBoostRemaining() { return attackBoostRemaining; }
    public int getShieldRemaining() { return shieldRemaining; }
    public boolean isInvincible() { return invincibleRemaining > 0; }
    public boolean isUltimateActive() { return ultimateActive; }
    public int getUltimateDuration() { return ultimateDuration; }
    public int getUltimateCooldown() { return ultimateCooldown; }
    
    public void setMoveLeft(boolean moveLeft) { this.moveLeft = moveLeft; }
    public void setMoveRight(boolean moveRight) { this.moveRight = moveRight; }
    public void setMoveUp(boolean moveUp) { this.moveUp = moveUp; }
    public void setMoveDown(boolean moveDown) { this.moveDown = moveDown; }
    public void setShootPressed(boolean shootPressed) { this.shootPressed = shootPressed; }
    public void increaseShotCount() { this.shotCount *= 2; }
}
