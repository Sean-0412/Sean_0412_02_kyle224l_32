package com.spaceinvaders;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class GameRenderer {

    private final GamePanel gamePanel;
    private final GameUI gameUI;
    private final EntityManager entityManager;

    public GameRenderer(GamePanel gamePanel, EntityManager entityManager) {
        this.gamePanel = gamePanel;
        this.entityManager = entityManager;
        this.gameUI = new GameUI(gamePanel);
    }

    public void paint(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw game objects
        gamePanel.getPlayer1().draw(g2);
        if (gamePanel.isTwoPlayer()) {
            gamePanel.getPlayer2().draw(g2);
        }
        drawBullets(g2);
        entityManager.draw(g2);

        // Draw UI on top
        gameUI.draw(g2);
    }

    private void drawBullets(Graphics2D g2) {
        // Player 1 bullets
        g2.setColor(java.awt.Color.WHITE);
        for (Bullet bullet : gamePanel.getPlayer1().getBullets()) {
            g2.fillRect(bullet.x, bullet.y, Bullet.WIDTH, Bullet.HEIGHT);
        }
        // Player 2 bullets
        if (gamePanel.isTwoPlayer()) {
            g2.setColor(new java.awt.Color(100, 255, 255));
            for (Bullet bullet : gamePanel.getPlayer2().getBullets()) {
                g2.fillRect(bullet.x, bullet.y, Bullet.WIDTH, Bullet.HEIGHT);
            }
        }
    }
}
