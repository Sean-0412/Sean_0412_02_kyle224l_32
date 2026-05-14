package com.spaceinvaders;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
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
 * 
 * Game Modes:
 * - MODE_CLASSIC (0): Only left/right movement, game ends when enemies reach the line
 * - MODE_DODGING (1): Full movement (up/down/left/right), game ends when hit by enemies
 */
public class GamePanel extends JPanel implements ActionListener, KeyListener {
    // Game modes
    private static final int MODE_CLASSIC = 0;
    private static final int MODE_DODGING = 1;
    
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private static final int PLAYER_WIDTH = 70;
    private static final int PLAYER_HEIGHT = 18;
    private static final int PLAYER_SPEED = 7;
    private static final int PLAYER_VERTICAL_SPEED = 5;

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
    private int playerY;

    private boolean moveLeft;
    private boolean moveRight;
    private boolean moveUp;
    private boolean moveDown;
    private boolean shootPressed;

    private int fireCooldown;
    private int score;

    private boolean gameOver;
    private boolean gameWin;
    
    private boolean isPaused;
    private int resumeCountdown; // 3, 2, 1, then resume
    private int pausedSelectedOption; // 0 = Continue, 1 = Return to Difficulty
    private static final int PAUSE_CONTINUE = 0;
    private static final int PAUSE_RETURN = 1;

    private double alienSpeed;
    private int alienDirection;
    private int safeLineY; // For classic mode: the line that enemies shouldn't cross
    
    private int difficulty; // 0 = Easy, 1 = Normal, 2 = Hard
    private int gameMode;   // 0 = Classic, 1 = Dodging
    private GameFrame gameFrame; // Reference to parent frame

    public GamePanel() {
        this(null, MODE_CLASSIC, 1); // Default to Classic mode with Normal difficulty
    }
    
    public GamePanel(int gameMode, int difficulty) {
        this(null, gameMode, difficulty);
    }
    
    public GamePanel(GameFrame gameFrame, int gameMode, int difficulty) {
        System.out.println("GamePanel: Constructor starting... (Mode: " + (gameMode == MODE_CLASSIC ? "Classic" : "Dodging") + ", Difficulty: " + difficulty + ")");
        this.gameFrame = gameFrame;
        this.gameMode = gameMode;
        this.difficulty = difficulty;
        this.safeLineY = HEIGHT - 70;
        
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        playerX = (WIDTH - PLAYER_WIDTH) / 2;
        playerY = HEIGHT - 70;

        System.out.println("GamePanel: Initializing aliens...");
        initAliens();

        System.out.println("GamePanel: Creating timer...");
        gameTimer = new Timer(16, this);
        System.out.println("GamePanel: Constructor finished.");
    }
    
    @Deprecated
    public GamePanel(int difficulty) {
        this(MODE_CLASSIC, difficulty);
    }

    public void startGame() {
        if (!gameTimer.isRunning()) {
            gameTimer.start();
        }
        requestFocusInWindow();
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
        
        // Set initial speed based on difficulty
        switch(difficulty) {
            case 0: // Easy
                alienSpeed = 0.8;
                break;
            case 1: // Normal
                alienSpeed = 1.8;
                break;
            case 2: // Hard
                alienSpeed = 3.0;
                break;
            default:
                alienSpeed = 1.8;
        }
        alienDirection = 1;
    }

    private void restartGame() {
        score = 0;
        bullets.clear();
        playerX = (WIDTH - PLAYER_WIDTH) / 2;
        playerY = HEIGHT - 70;
        gameOver = false;
        gameWin = false;
        fireCooldown = 0;
        moveLeft = false;
        moveRight = false;
        moveUp = false;
        moveDown = false;
        shootPressed = false;
        isPaused = false;
        resumeCountdown = 0;
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
        
        // Handle pause resume countdown
        if (isPaused && resumeCountdown > 0) {
            resumeCountdown--;
            if (resumeCountdown == 0) {
                isPaused = false;
            }
            return;
        }
        
        if (isPaused) {
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
        
        // Only allow vertical movement in dodging mode
        if (gameMode == MODE_DODGING) {
            if (moveUp) {
                playerY -= PLAYER_VERTICAL_SPEED;
            }
            if (moveDown) {
                playerY += PLAYER_VERTICAL_SPEED;
            }
        }

        if (playerX < 0) {
            playerX = 0;
        }
        if (playerX > WIDTH - PLAYER_WIDTH) {
            playerX = WIDTH - PLAYER_WIDTH;
        }
        
        if (gameMode == MODE_DODGING) {
            if (playerY < 50) {
                playerY = 50;
            }
            if (playerY > HEIGHT - PLAYER_HEIGHT - 10) {
                playerY = HEIGHT - PLAYER_HEIGHT - 10;
            }
        } else {
            // Classic mode: keep player at safe line
            playerY = safeLineY;
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
            bullet.update();
            if (bullet.y + Bullet.HEIGHT < 0) {
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
            Rectangle2D bulletRect = bullet.getBounds();

            boolean hit = false;
            Iterator<Alien> alienIterator = aliens.iterator();
            while (alienIterator.hasNext()) {
                Alien alien = alienIterator.next();
                Rectangle2D alienRect = alien.getBounds();
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
        
        // Check for collisions with player (only in dodging mode)
        if (gameMode == MODE_DODGING) {
            for (Alien alien : aliens) {
                if (alien.getBounds().intersects(new Rectangle2D.Double(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT))) {
                    gameOver = true;
                    gameWin = false;
                    SoundPlayer.playGameOver();
                    return;
                }
            }
        }
        
        // Check if aliens crossed the safety line (only in classic mode)
        if (gameMode == MODE_CLASSIC) {
            for (Alien alien : aliens) {
                if (alien.y + ALIEN_HEIGHT >= safeLineY) {
                    gameOver = true;
                    gameWin = false;
                    SoundPlayer.playGameOver();
                    return;
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawHud(g2);
        
        // Draw safety line in classic mode
        if (gameMode == MODE_CLASSIC) {
            drawSafetyLine(g2);
        }
        
        drawPlayer(g2);
        drawBullets(g2);
        drawAliens(g2);
        
        // Draw pause menu if paused
        if (isPaused) {
            drawPauseMenu(g2);
        }

        if (gameOver) {
            drawGameOver(g2);
        }
    }

    private void drawHud(Graphics2D g2) {
        g2.setColor(new Color(0, 255, 120));
        g2.setFont(new Font("Consolas", Font.BOLD, 20));
        g2.drawString("Score: " + score, 20, 30);
        
        // Draw game mode
        String modeText = gameMode == MODE_CLASSIC ? "CLASSIC" : "DODGING";
        g2.setColor(gameMode == MODE_CLASSIC ? new Color(100, 150, 255) : new Color(255, 100, 150));
        g2.drawString("Mode: " + modeText, 20, 55);
        
        // Draw difficulty level
        String difficultyText;
        switch(difficulty) {
            case 0:
                difficultyText = "EASY";
                g2.setColor(new Color(100, 255, 100));
                break;
            case 1:
                difficultyText = "NORMAL";
                g2.setColor(new Color(255, 200, 0));
                break;
            case 2:
                difficultyText = "HARD";
                g2.setColor(new Color(255, 100, 100));
                break;
            default:
                difficultyText = "NORMAL";
                g2.setColor(new Color(255, 200, 0));
        }
        g2.drawString("Difficulty: " + difficultyText, WIDTH - 250, 30);

        g2.setFont(new Font("Consolas", Font.PLAIN, 14));
        g2.setColor(new Color(170, 255, 210));
        if (gameMode == MODE_CLASSIC) {
            g2.drawString("Move: Left/Right  Shoot: Space", 20, 77);
        } else {
            g2.drawString("Move: Arrows  Shoot: Space", 20, 77);
        }
    }

    private void drawSafetyLine(Graphics2D g2) {
        g2.setColor(new Color(255, 100, 100, 200));
        g2.setStroke(new java.awt.BasicStroke(2, java.awt.BasicStroke.CAP_BUTT, 
                java.awt.BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
        g2.drawLine(0, safeLineY - 5, WIDTH, safeLineY - 5);
        
        g2.setColor(new Color(255, 100, 100, 150));
        g2.setFont(new Font("Consolas", Font.BOLD, 12));
        String warning = "DANGER ZONE";
        int tw = g2.getFontMetrics().stringWidth(warning);
        g2.drawString(warning, (WIDTH - tw) / 2, safeLineY - 15);
    }

    private void drawPauseMenu(Graphics2D g2) {
        // Semi-transparent overlay
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Title
        g2.setColor(new Color(255, 200, 0));
        g2.setFont(new Font("Consolas", Font.BOLD, 70));
        String title = "PAUSED";
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (WIDTH - tw) / 2, 150);
        
        // Resume countdown if active
        if (resumeCountdown > 0) {
            g2.setColor(new Color(0, 255, 150));
            g2.setFont(new Font("Consolas", Font.BOLD, 50));
            int secondsLeft = (resumeCountdown + 59) / 60; // Round up to nearest second
            String countdownText = "Resuming in " + secondsLeft;
            int cw = g2.getFontMetrics().stringWidth(countdownText);
            g2.drawString(countdownText, (WIDTH - cw) / 2, 280);
            return;
        }
        
        // Menu options
        int startY = 280;
        String[] options = {"Continue Game", "Return to Difficulty"};
        
        for (int i = 0; i < options.length; i++) {
            if (pausedSelectedOption == i) {
                // Highlighted
                g2.setColor(new Color(255, 200, 0));
                g2.setFont(new Font("Consolas", Font.BOLD, 36));
                int ow = g2.getFontMetrics().stringWidth(options[i]);
                
                // Background box
                int boxX = (WIDTH - ow) / 2 - 20;
                int boxY = startY + i * 80 - 35;
                int boxW = ow + 40;
                int boxH = 50;
                
                g2.setColor(new Color(150, 120, 0, 100));
                g2.fillRect(boxX, boxY, boxW, boxH);
                g2.setColor(new Color(255, 200, 0));
                g2.setStroke(new java.awt.BasicStroke(3));
                g2.drawRect(boxX, boxY, boxW, boxH);
                
                // Text
                g2.setColor(new Color(255, 200, 0));
                g2.drawString(options[i], (WIDTH - ow) / 2, startY + i * 80);
            } else {
                // Normal
                g2.setColor(new Color(100, 200, 255));
                g2.setFont(new Font("Consolas", Font.PLAIN, 36));
                int ow = g2.getFontMetrics().stringWidth(options[i]);
                g2.drawString(options[i], (WIDTH - ow) / 2, startY + i * 80);
            }
        }
        
        // Hint
        g2.setColor(new Color(100, 200, 255));
        g2.setFont(new Font("Consolas", Font.PLAIN, 14));
        String hint = "Use ↑↓ to select, Enter to confirm";
        int hw = g2.getFontMetrics().stringWidth(hint);
        g2.drawString(hint, (WIDTH - hw) / 2, 550);
    }

    private void drawPlayer(Graphics2D g2) {
        int centerX = playerX + PLAYER_WIDTH / 2;
        int centerY = playerY + PLAYER_HEIGHT / 2;

        // 主船體 - 三角形（指向上方）
        int[] hullX = {
            playerX + PLAYER_WIDTH / 2,           // 前端（尖點）
            playerX + 5,                          // 左後端
            playerX + PLAYER_WIDTH - 5             // 右後端
        };
        int[] hullY = {
            playerY,                              // 前端
            playerY + PLAYER_HEIGHT,              // 左後端
            playerY + PLAYER_HEIGHT               // 右後端
        };
        
        Polygon hull = new Polygon(hullX, hullY, 3);
        g2.setColor(new Color(50, 150, 255));
        g2.fillPolygon(hull);
        
        // 船體邊框
        g2.setColor(new Color(100, 180, 255));
        g2.setStroke(new java.awt.BasicStroke(2));
        g2.drawPolygon(hull);
        
        // 駕駛艙（圓形窗口）
        int cockpitX = playerX + PLAYER_WIDTH / 2 - 6;
        int cockpitY = playerY + 4;
        g2.setColor(new Color(150, 255, 150));
        g2.fillOval(cockpitX, cockpitY, 12, 8);
        g2.setColor(new Color(100, 200, 100));
        g2.drawOval(cockpitX, cockpitY, 12, 8);
        
        // 左翼推進器
        g2.setColor(new Color(255, 100, 50));
        g2.fillRect(playerX + 8, playerY + PLAYER_HEIGHT - 4, 6, 4);
        
        // 右翼推進器
        g2.fillRect(playerX + PLAYER_WIDTH - 14, playerY + PLAYER_HEIGHT - 4, 6, 4);
        
        // 中央推進器
        g2.setColor(new Color(255, 200, 100));
        g2.fillRect(playerX + PLAYER_WIDTH / 2 - 2, playerY + PLAYER_HEIGHT - 2, 4, 2);
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
        
        // Handle pause menu navigation
        if (isPaused && resumeCountdown == 0) {
            if (code == KeyEvent.VK_UP) {
                pausedSelectedOption = (pausedSelectedOption - 1 + 2) % 2;
                repaint();
                return;
            } else if (code == KeyEvent.VK_DOWN) {
                pausedSelectedOption = (pausedSelectedOption + 1) % 2;
                repaint();
                return;
            } else if (code == KeyEvent.VK_ENTER) {
                handlePauseMenuChoice();
                repaint();
                return;
            }
        }
        
        // Handle pause key
        if (code == KeyEvent.VK_P && !gameOver && !isPaused) {
            isPaused = true;
            pausedSelectedOption = PAUSE_CONTINUE;
            repaint();
            return;
        }
        
        if (code == KeyEvent.VK_LEFT) {
            moveLeft = true;
        } else if (code == KeyEvent.VK_RIGHT) {
            moveRight = true;
        } else if (code == KeyEvent.VK_UP) {
            moveUp = true;
        } else if (code == KeyEvent.VK_DOWN) {
            moveDown = true;
        } else if (code == KeyEvent.VK_SPACE) {
            shootPressed = true;
        } else if (code == KeyEvent.VK_R && gameOver) {
            restartGame();
        }
    }
    
    private void handlePauseMenuChoice() {
        if (pausedSelectedOption == PAUSE_CONTINUE) {
            // Start countdown to resume (3 seconds = 3000ms / 16ms per frame ≈ 188 frames)
            resumeCountdown = 188;
        } else if (pausedSelectedOption == PAUSE_RETURN) {
            // Return to difficulty selection
            gameTimer.stop();
            if (gameFrame != null) {
                gameFrame.returnToDifficultyMenu();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_LEFT) {
            moveLeft = false;
        } else if (code == KeyEvent.VK_RIGHT) {
            moveRight = false;
        } else if (code == KeyEvent.VK_UP) {
            moveUp = false;
        } else if (code == KeyEvent.VK_DOWN) {
            moveDown = false;
        } else if (code == KeyEvent.VK_SPACE) {
            shootPressed = false;
        }
    }
}
