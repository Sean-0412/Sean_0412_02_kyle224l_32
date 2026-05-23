package com.spaceinvaders;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class GameUI {

    private final GamePanel gamePanel;

    public GameUI(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void draw(Graphics2D g2) {
        drawHud(g2);
        if (gamePanel.getGameMode() == GamePanel.MODE_CLASSIC) {
            drawSafetyLine(g2);
        }
        if (gamePanel.isPaused()) {
            drawPauseMenu(g2);
        }
        if (gamePanel.isGameOver()) {
            drawGameOver(g2);
        }
    }

    private void drawHud(Graphics2D g2) {
        g2.setColor(new Color(0, 255, 120));
        g2.setFont(new Font("Consolas", Font.BOLD, 20));

        int x = 20;
        int y = 30;
        int lineGap = 30;

        g2.drawString("Score: " + gamePanel.getScore(), x, y);
        y += lineGap;

        if (gamePanel.isTwoPlayer()) {
            g2.drawString("P1 Lives: " + gamePanel.getPlayer1().getLives(), x, y);
            y += lineGap;
            g2.drawString("P2 Lives: " + gamePanel.getPlayer2().getLives(), x, y);
            y += lineGap;
        } else {
            g2.drawString("Lives: " + gamePanel.getPlayer1().getLives(), x, y);
            y += lineGap;
        }

        String modeText;
        Color modeColor;
        switch (gamePanel.getGameMode()) {
            case GamePanel.MODE_CLASSIC:
                modeText = "CLASSIC";
                modeColor = new Color(100, 150, 255);
                break;
            case GamePanel.MODE_DODGING:
                modeText = "DODGING";
                modeColor = new Color(255, 100, 150);
                break;
            default:
                modeText = "STAGE";
                modeColor = new Color(180, 255, 100);
                break;
        }
        g2.setColor(modeColor);
        g2.drawString("Mode: " + modeText, x, y);
        y += lineGap;

        if (gamePanel.getGameMode() != GamePanel.MODE_CLASSIC) {
            String difficultyText;
            switch (gamePanel.getDifficulty()) {
                case 0: difficultyText = "EASY"; g2.setColor(new Color(100, 255, 100)); break;
                case 1: difficultyText = "NORMAL"; g2.setColor(new Color(255, 200, 0)); break;
                default: difficultyText = "HARD"; g2.setColor(new Color(255, 100, 100)); break;
            }
            g2.drawString("Difficulty: " + difficultyText, GamePanel.WIDTH - 250, 30);
        }

        g2.setColor(new Color(170, 255, 210));
        g2.setFont(new Font("Consolas", Font.PLAIN, 14));
        
        Player p1 = gamePanel.getPlayer1();
        if (p1.getAttackBoostRemaining() > 0) {
            g2.drawString("P1 Attack: " + (p1.getAttackBoostRemaining() / 60) + "s", x, y);
            y += lineGap;
        }
        if (gamePanel.isTwoPlayer()) {
            Player p2 = gamePanel.getPlayer2();
            if (p2.getAttackBoostRemaining() > 0) {
                g2.drawString("P2 Attack: " + (p2.getAttackBoostRemaining() / 60) + "s", x, y);
                y += lineGap;
            }
        }
        if (p1.getShieldRemaining() > 0) {
            g2.drawString("Shield: " + (p1.getShieldRemaining() / 60) + "s", x, y);
            y += lineGap;
        }

        if (!gamePanel.isTwoPlayer()) {
            g2.setColor(new Color(p1.isUltimateActive() ? 255 : 100, 100, 255));
            if (p1.isUltimateActive()) {
                g2.drawString("ULTIMATE ACTIVE: " + (p1.getUltimateDuration() / 60) + "s", x, y);
            } else if (p1.getUltimateCooldown() > 0) {
                g2.drawString("Ultimate Cooldown: " + (p1.getUltimateCooldown() / 60) + "s (Press U)", x, y);
            } else {
                g2.drawString("Ultimate Ready (Press U)", x, y);
            }
            y += lineGap;
        } else {
            g2.setColor(new Color(200, 200, 200));
            g2.drawString("Two Player Mode: No Ultimate", x, y);
            y += lineGap;
        }

        g2.setColor(new Color(170, 255, 210));
        g2.setFont(new Font("Consolas", Font.PLAIN, 14));
        g2.drawString((gamePanel.getGameMode() != GamePanel.MODE_CLASSIC)
                ? "P1: Arrows  Shoot: Space" + (gamePanel.isTwoPlayer() ? " | P2: WASD  Shoot: H" : "")
                : "P1: Left/Right  Shoot: Space" + (gamePanel.isTwoPlayer() ? " | P2: A/D  Shoot: H" : ""), x, y);
        y += lineGap;
        g2.drawString("Pause: P  Menu: Esc  Restart: R", x, y);

        if (gamePanel.getGameMode() == GamePanel.MODE_STAGE) {
            g2.setColor(new Color(255, 200, 0));
            g2.drawString("Stage: " + gamePanel.getCurrentLevel(), GamePanel.WIDTH - 250, 60);
        }
        g2.setColor(new Color(170, 255, 210));
        g2.drawString("Aliens: " + gamePanel.getEntityManager().getAliens().size(), GamePanel.WIDTH - 250, 90);
    }

    private void drawSafetyLine(Graphics2D g2) {
        g2.setColor(new Color(255, 100, 100, 200));
        g2.setStroke(new java.awt.BasicStroke(2, java.awt.BasicStroke.CAP_BUTT, 
                java.awt.BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
        g2.drawLine(0, GamePanel.HEIGHT - 75, GamePanel.WIDTH, GamePanel.HEIGHT - 75);
        
        g2.setColor(new Color(255, 100, 100, 150));
        g2.setFont(new Font("Consolas", Font.BOLD, 12));
        String warning = "DANGER ZONE";
        int tw = g2.getFontMetrics().stringWidth(warning);
        g2.drawString(warning, (GamePanel.WIDTH - tw) / 2, GamePanel.HEIGHT - 85);
    }

    private void drawPauseMenu(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);
        
        g2.setColor(new Color(255, 200, 0));
        g2.setFont(new Font("Consolas", Font.BOLD, 70));
        String title = "PAUSED";
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (GamePanel.WIDTH - tw) / 2, 150);
        
        if (gamePanel.getResumeCountdown() > 0) {
            g2.setColor(new Color(0, 255, 150));
            g2.setFont(new Font("Consolas", Font.BOLD, 50));
            int secondsLeft = (gamePanel.getResumeCountdown() + 59) / 60;
            String countdownText = "Resuming in " + secondsLeft;
            int cw = g2.getFontMetrics().stringWidth(countdownText);
            g2.drawString(countdownText, (GamePanel.WIDTH - cw) / 2, 280);
            return;
        }
        
        String[] options = {"Continue Game", "Return to Menu"};
        for (int i = 0; i < options.length; i++) {
            if (gamePanel.getPausedSelectedOption() == i) {
                g2.setColor(new Color(255, 200, 0));
                g2.setFont(new Font("Consolas", Font.BOLD, 36));
                int ow = g2.getFontMetrics().stringWidth(options[i]);
                g2.fillRect((GamePanel.WIDTH - ow) / 2 - 20, 280 + i * 80 - 35, ow + 40, 50);
                g2.setColor(new Color(255, 200, 0));
                g2.drawString(options[i], (GamePanel.WIDTH - ow) / 2, 280 + i * 80);
            } else {
                g2.setColor(new Color(100, 200, 255));
                g2.setFont(new Font("Consolas", Font.PLAIN, 36));
                int ow = g2.getFontMetrics().stringWidth(options[i]);
                g2.drawString(options[i], (GamePanel.WIDTH - ow) / 2, 280 + i * 80);
            }
        }
        
        g2.setColor(new Color(100, 200, 255));
        g2.setFont(new Font("Consolas", Font.PLAIN, 14));
        String hint = "Use ↑↓ to select, Enter to confirm";
        int hw = g2.getFontMetrics().stringWidth(hint);
        g2.drawString(hint, (GamePanel.WIDTH - hw) / 2, 550);
    }

    private void drawGameOver(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.BOLD, 40));

        String title = gamePanel.isGameWin() ? "YOU WIN" : "GAME OVER";
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (GamePanel.WIDTH - tw) / 2, GamePanel.HEIGHT / 2 - 20);

        g2.setFont(new Font("Consolas", Font.PLAIN, 20));
        String scoreText = "Final Score: " + gamePanel.getScore();
        int sw = g2.getFontMetrics().stringWidth(scoreText);
        g2.drawString(scoreText, (GamePanel.WIDTH - sw) / 2, GamePanel.HEIGHT / 2 + 20);

        String restartText = "Press R to Restart";
        int rw = g2.getFontMetrics().stringWidth(restartText);
        g2.drawString(restartText, (GamePanel.WIDTH - rw) / 2, GamePanel.HEIGHT / 2 + 60);
    }
}
