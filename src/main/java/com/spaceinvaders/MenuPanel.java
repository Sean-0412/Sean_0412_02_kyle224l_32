package com.spaceinvaders;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;

/**
 * Main menu panel for Space Invaders game.
 */
public class MenuPanel extends JPanel implements KeyListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    
    private int selectedOption = 0;
    private static final int NUM_OPTIONS = 3;
    
    private GameFrame gameFrame;
    
    public MenuPanel(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        requestFocusInWindow();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 標題
        g2.setColor(new Color(0, 255, 150));
        g2.setFont(new Font("Consolas", Font.BOLD, 80));
        String title = "SPACE INVADERS";
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (WIDTH - tw) / 2, 100);
        
        // 選項
        drawMenuItem(g2, "EASY", 0, 250);
        drawMenuItem(g2, "NORMAL", 1, 320);
        drawMenuItem(g2, "HARD", 2, 390);
        
        // 提示信息
        g2.setColor(new Color(100, 200, 255));
        g2.setFont(new Font("Consolas", Font.PLAIN, 16));
        String hint = "Use ↑↓ to select, ENTER to confirm";
        int hw = g2.getFontMetrics().stringWidth(hint);
        g2.drawString(hint, (WIDTH - hw) / 2, 500);
        
        // 底部裝飾
        g2.setColor(new Color(50, 150, 100));
        g2.drawLine(0, 550, WIDTH, 550);
    }
    
    private void drawMenuItem(Graphics2D g2, String text, int index, int y) {
        if (selectedOption == index) {
            // 高亮選中的選項
            g2.setColor(new Color(255, 200, 0));
            g2.setFont(new Font("Consolas", Font.BOLD, 40));
            
            // 繪製背景框
            Rectangle2D bounds = g2.getFontMetrics().getStringBounds(text, g2);
            int boxX = (WIDTH - (int)bounds.getWidth()) / 2 - 20;
            int boxY = y - 30;
            int boxWidth = (int)bounds.getWidth() + 40;
            int boxHeight = 50;
            
            g2.setColor(new Color(100, 100, 0, 100));
            g2.fillRect(boxX, boxY, boxWidth, boxHeight);
            g2.setColor(new Color(255, 200, 0));
            g2.setStroke(new java.awt.BasicStroke(3));
            g2.drawRect(boxX, boxY, boxWidth, boxHeight);
            
            // 繪製文字
            g2.setColor(new Color(255, 200, 0));
            int tw = g2.getFontMetrics().stringWidth(text);
            g2.drawString(text, (WIDTH - tw) / 2, y);
        } else {
            // 未選中的選項
            g2.setColor(new Color(100, 200, 255));
            g2.setFont(new Font("Consolas", Font.PLAIN, 40));
            int tw = g2.getFontMetrics().stringWidth(text);
            g2.drawString(text, (WIDTH - tw) / 2, y);
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        
        if (code == KeyEvent.VK_UP) {
            selectedOption = (selectedOption - 1 + NUM_OPTIONS) % NUM_OPTIONS;
            repaint();
        } else if (code == KeyEvent.VK_DOWN) {
            selectedOption = (selectedOption + 1) % NUM_OPTIONS;
            repaint();
        } else if (code == KeyEvent.VK_ENTER) {
            startGame();
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
    }
    
    private void startGame() {
        // 根據選擇的難度啟動遊戲
        gameFrame.startGameWithDifficulty(selectedOption);
    }
}
