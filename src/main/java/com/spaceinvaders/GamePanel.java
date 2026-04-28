package com.spaceinvaders;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Main game panel for Space Invaders game.
 * Handles game logic, rendering, and user input.
 */
public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private static final int PLAYER_WIDTH = 70;
    private static final int PLAYER_HEIGHT = 18;
    private static final int PLAYER_SPEED = 7;

    private static final int BULLET_WIDTH = 4;
    private static final int BULLET_HEIGHT = 12;
    private static final int BULLET_SPEED = 10;
    private static final int MAX_BULLETS = 5;

    private static final int ALIEN_WIDTH = 36;
    private static final int ALIEN_HEIGHT = 24;
    private static final int ALIEN_COLS = 10;
    private static final int ALIEN_ROWS = 4;
    private static final int ALIEN_H_SPACING = 18;
    private static final int ALIEN_V_SPACING = 14;

    private static final int START_X = 80;
    private static final int START_Y = 70;

    private final Timer gameTimer;
    private final List<Alien> aliens = new ArrayList<Alien>();
    private final List<Bullet> bullets = new ArrayList<Bullet>();

    private int playerX;
    private final int playerY;

    private boolean moveLeft;
    private boolean moveRight;
    private boolean shootPressed;

    private int fireCooldown;
    private int score;

    private boolean gameOver;
    private boolean gameWin;

    private double alienSpeed;
    private int alienDirection;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        playerX = (WIDTH - PLAYER_WIDTH) / 2;
        playerY = HEIGHT - 70;

        initAliens();

        gameTimer = new Timer(16, this);
        gameTimer.start();
    }

    private void initAliens() {
        aliens.clear();
        for (int row = 0; row < ALIEN_ROWS; row++) {
            for (int col = 0; col < ALIEN_COLS; col++) {
                int x = START_X + col * (ALIEN_WIDTH + ALIEN_H_SPACING);
                int y = START_Y + row * (ALIEN_HEIGHT + ALIEN_V_SPACING);
                aliens.add(new Alien(x, y));
            }
        }
        alienSpeed = 1.8;
        alienDirection = 1;
    }

    private void restartGame() {
        score = 0;
        bullets.clear();
        playerX = (WIDTH - PLAYER_WIDTH) / 2;
        gameOver = false;
        gameWin = false;
        fireCooldown = 0;
        moveLeft = false;
        moveRight = false;
        shootPressed = false;
        initAliens();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateGame();
        repaint();
    }

    private void updateGame() {
        if (gameOver) {
            return;
        }

        updatePlayer();
        updateBullets();
        updateAliens();
        checkCollisions();
        checkGameState();

        if (fireCooldown > 0) {
            fireCooldown--;
        }
    }

    private void updatePlayer() {
        if (moveLeft) {
            playerX -= PLAYER_SPEED;
        }
        if (moveRight) {
            playerX += PLAYER_SPEED;
        }

        if (playerX < 0) {
            playerX = 0;
        }
        if (playerX > WIDTH - PLAYER_WIDTH) {
            playerX = WIDTH - PLAYER_WIDTH;
        }

        if (shootPressed && fireCooldown == 0 && bullets.size() < MAX_BULLETS) {
            int bulletX = playerX + (PLAYER_WIDTH / 2) - (BULLET_WIDTH / 2);
            int bulletY = playerY - BULLET_HEIGHT;
            bullets.add(new Bullet(bulletX, bulletY));
            fireCooldown = 10;
            SoundPlayer.playShoot();
        }
    }

    private void updateBullets() {
        Iterator<Bullet> iterator = bullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            bullet.y -= BULLET_SPEED;
            if (bullet.y + BULLET_HEIGHT < 0) {
                iterator.remove();
            }
        }
    }

    private void updateAliens() {
        if (aliens.isEmpty()) {
            return;
        }

        boolean hitEdge = false;
        for (Alien alien : aliens) {
            alien.x += alienDirection * alienSpeed;
            if (alien.x <= 8 || alien.x + ALIEN_WIDTH >= WIDTH - 8) {
                hitEdge = true;
            }
        }

        if (hitEdge) {
            alienDirection *= -1;
            for (Alien alien : aliens) {
                alien.y += 18;
            }
        }

        // Increase difficulty over time through score milestones.
        alienSpeed = 1.8 + (score / 100.0) * 0.22;
    }

    private void checkCollisions() {
        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            Rectangle2D bulletRect = new Rectangle2D.Double(bullet.x, bullet.y, BULLET_WIDTH, BULLET_HEIGHT);

            boolean hit = false;
            Iterator<Alien> alienIterator = aliens.iterator();
            while (alienIterator.hasNext()) {
                Alien alien = alienIterator.next();
                Rectangle2D alienRect = new Rectangle2D.Double(alien.x, alien.y, ALIEN_WIDTH, ALIEN_HEIGHT);
                if (bulletRect.intersects(alienRect)) {
                    alienIterator.remove();
                    bulletIterator.remove();
                    score += 10;
                    SoundPlayer.playHit();
                    hit = true;
                    break;
                }
            }

            if (hit) {
                continue;
            }
        }
    }

    private void checkGameState() {
        if (aliens.isEmpty()) {
            gameOver = true;
            gameWin = true;
            return;
        }

        for (Alien alien : aliens) {
            if (alien.y + ALIEN_HEIGHT >= playerY) {
                gameOver = true;
                gameWin = false;
                SoundPlayer.playGameOver();
                return;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawHud(g2);
        drawPlayer(g2);
        drawBullets(g2);
        drawAliens(g2);
        drawBottomLine(g2);

        if (gameOver) {
            drawGameOver(g2);
        }
    }

    private void drawHud(Graphics2D g2) {
        g2.setColor(new Color(0, 255, 120));
        g2.setFont(new Font("Consolas", Font.BOLD, 20));
        g2.drawString("Score: " + score, 20, 30);

        g2.setFont(new Font("Consolas", Font.PLAIN, 14));
        g2.setColor(new Color(170, 255, 210));
        g2.drawString("Move: Left/Right  Shoot: Space", 20, 52);
    }

    private void drawPlayer(Graphics2D g2) {
        g2.setColor(new Color(70, 150, 255));
        g2.fillRoundRect(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT, 8, 8);
    }

    private void drawBullets(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        for (Bullet bullet : bullets) {
            g2.fillRect(bullet.x, bullet.y, BULLET_WIDTH, BULLET_HEIGHT);
        }
    }

    private void drawAliens(Graphics2D g2) {
        g2.setColor(new Color(150, 255, 80));
        for (Alien alien : aliens) {
            g2.fillRoundRect((int) alien.x, (int) alien.y, ALIEN_WIDTH, ALIEN_HEIGHT, 6, 6);
            g2.setColor(new Color(30, 80, 20));
            g2.fillOval((int) alien.x + 8, (int) alien.y + 7, 6, 6);
            g2.fillOval((int) alien.x + 22, (int) alien.y + 7, 6, 6);
            g2.setColor(new Color(150, 255, 80));
        }
    }

    private void drawBottomLine(Graphics2D g2) {
        g2.setColor(new Color(60, 60, 60));
        g2.drawLine(0, playerY + PLAYER_HEIGHT + 4, WIDTH, playerY + PLAYER_HEIGHT + 4);
    }

    private void drawGameOver(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.BOLD, 40));

        String title = gameWin ? "YOU WIN" : "GAME OVER";
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (WIDTH - tw) / 2, HEIGHT / 2 - 20);

        g2.setFont(new Font("Consolas", Font.PLAIN, 20));
        String scoreText = "Final Score: " + score;
        int sw = g2.getFontMetrics().stringWidth(scoreText);
        g2.drawString(scoreText, (WIDTH - sw) / 2, HEIGHT / 2 + 20);

        String restartText = "Press R to Restart";
        int rw = g2.getFontMetrics().stringWidth(restartText);
        g2.drawString(restartText, (WIDTH - rw) / 2, HEIGHT / 2 + 60);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used.
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_LEFT) {
            moveLeft = true;
        } else if (code == KeyEvent.VK_RIGHT) {
            moveRight = true;
        } else if (code == KeyEvent.VK_SPACE) {
            shootPressed = true;
        } else if (code == KeyEvent.VK_R && gameOver) {
            restartGame();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_LEFT) {
            moveLeft = false;
        } else if (code == KeyEvent.VK_RIGHT) {
            moveRight = false;
        } else if (code == KeyEvent.VK_SPACE) {
            shootPressed = false;
        }
    }
}
