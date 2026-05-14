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
    private static final String[] MAIN_MENU_OPTIONS = {"Start Game", "Controls", "About", "Exit"};
    private static final String[] DIFFICULTY_OPTIONS = {"Easy", "Normal", "Hard"};
    
    private GameFrame gameFrame;
    
    // Menu states
    private static final int STATE_MAIN_MENU = 0;
    private static final int STATE_DIFFICULTY = 1;
    private static final int STATE_CONTROLS = 2;
    private static final int STATE_ABOUT = 3;
    
    private int currentState = STATE_MAIN_MENU;
    
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
        
        switch(currentState) {
            case STATE_MAIN_MENU:
                drawMainMenu(g2);
                break;
            case STATE_DIFFICULTY:
                drawDifficultyMenu(g2);
                break;
            case STATE_CONTROLS:
                drawControlsScreen(g2);
                break;
            case STATE_ABOUT:
                drawAboutScreen(g2);
                break;
        }
    }
    
    private void drawMainMenu(Graphics2D g2) {
        // Title
        g2.setColor(new Color(0, 255, 150));
        g2.setFont(new Font("Consolas", Font.BOLD, 70));
        String title = "SPACE INVADERS";
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (WIDTH - tw) / 2, 100);
        
        // Subtitle
        g2.setColor(new Color(100, 200, 255));
        g2.setFont(new Font("Consolas", Font.PLAIN, 20));
        String subtitle = "Classic Space Shooting Game";
        int sw = g2.getFontMetrics().stringWidth(subtitle);
        g2.drawString(subtitle, (WIDTH - sw) / 2, 140);
        
        // Menu options
        int startY = 220;
        for (int i = 0; i < MAIN_MENU_OPTIONS.length; i++) {
            drawMenuItem(g2, MAIN_MENU_OPTIONS[i], i, startY + i * 80);
        }
        
        // Hint
        g2.setColor(new Color(100, 200, 255));
        g2.setFont(new Font("Consolas", Font.PLAIN, 14));
        String hint = "Use ↑↓ to select, Enter to confirm";
        int hw = g2.getFontMetrics().stringWidth(hint);
        g2.drawString(hint, (WIDTH - hw) / 2, 550);
    }
    
    private void drawDifficultyMenu(Graphics2D g2) {
        // Title
        g2.setColor(new Color(0, 255, 150));
        g2.setFont(new Font("Consolas", Font.BOLD, 60));
        String title = "Select Difficulty";
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (WIDTH - tw) / 2, 100);
        
        // Difficulty options
        int startY = 250;
        for (int i = 0; i < DIFFICULTY_OPTIONS.length; i++) {
            drawMenuItem(g2, DIFFICULTY_OPTIONS[i], i, startY + i * 100);
        }
        
        // Hint
        g2.setColor(new Color(100, 200, 255));
        g2.setFont(new Font("Consolas", Font.PLAIN, 14));
        String hint = "Esc to return to menu";
        int hw = g2.getFontMetrics().stringWidth(hint);
        g2.drawString(hint, (WIDTH - hw) / 2, 550);
    }
    
    private void drawControlsScreen(Graphics2D g2) {
        // Title
        g2.setColor(new Color(0, 255, 150));
        g2.setFont(new Font("Consolas", Font.BOLD, 50));
        String title = "Controls";
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (WIDTH - tw) / 2, 80);
        
        // Control instructions
        g2.setColor(new Color(100, 200, 255));
        g2.setFont(new Font("Consolas", Font.PLAIN, 24));
        
        String[] controls = {
            "← → : Move spaceship",
            "Space : Shoot",
            "R : Restart game",
            "Esc : Return to menu"
        };
        
        int startY = 180;
        for (int i = 0; i < controls.length; i++) {
            g2.drawString(controls[i], 150, startY + i * 80);
        }
        
        // Return hint
        g2.setColor(new Color(150, 255, 150));
        g2.setFont(new Font("Consolas", Font.PLAIN, 14));
        g2.drawString("Press Enter or Esc to return to menu", 250, 550);
    }
    
    private void drawAboutScreen(Graphics2D g2) {
        // Title
        g2.setColor(new Color(0, 255, 150));
        g2.setFont(new Font("Consolas", Font.BOLD, 50));
        String title = "About Game";
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (WIDTH - tw) / 2, 80);
        
        // About info
        g2.setColor(new Color(100, 200, 255));
        g2.setFont(new Font("Consolas", Font.PLAIN, 24));
        
        String[] aboutInfo = {
            "SPACE INVADERS",
            "Classic Space Shooting Game",
            "",
            "Developer: Game Development Team",
            "Version: 1.0",
            "Year: 2026",
            "",
            "Thank you for playing!"
        };
        
        int startY = 150;
        for (int i = 0; i < aboutInfo.length; i++) {
            if (aboutInfo[i].isEmpty()) {
                startY += 20;
            } else {
                int textWidth = g2.getFontMetrics().stringWidth(aboutInfo[i]);
                g2.drawString(aboutInfo[i], (WIDTH - textWidth) / 2, startY);
                startY += 50;
            }
        }
        
        // Return hint
        g2.setColor(new Color(150, 255, 150));
        g2.setFont(new Font("Consolas", Font.PLAIN, 14));
        g2.drawString("Press Enter or Esc to return to menu", 250, 550);
    }
    
    private void drawMenuItem(Graphics2D g2, String text, int index, int y) {
        if (selectedOption == index) {
            // 高亮選中的選項
            g2.setColor(new Color(255, 200, 0));
            g2.setFont(new Font("Consolas", Font.BOLD, 36));
            
            // 繪製背景框
            Rectangle2D bounds = g2.getFontMetrics().getStringBounds(text, g2);
            int boxX = (WIDTH - (int)bounds.getWidth()) / 2 - 30;
            int boxY = y - 35;
            int boxWidth = (int)bounds.getWidth() + 60;
            int boxHeight = 55;
            
            g2.setColor(new Color(150, 120, 0, 100));
            g2.fillRect(boxX, boxY, boxWidth, boxHeight);
            g2.setColor(new Color(255, 200, 0));
            g2.setStroke(new java.awt.BasicStroke(3));
            g2.drawRect(boxX, boxY, boxWidth, boxHeight);
            
            // 繪製文字
            g2.setColor(new Color(255, 200, 0));
            int tw = g2.getFontMetrics().stringWidth(text);
            g2.drawString(text, (WIDTH - tw) / 2, y);
            
            // 繪製箭頭
            g2.drawString("◆", boxX - 50, y);
            g2.drawString("◆", boxX + boxWidth + 30, y);
        } else {
            // 未選中的選項
            g2.setColor(new Color(100, 200, 255));
            g2.setFont(new Font("Consolas", Font.PLAIN, 36));
            int tw = g2.getFontMetrics().stringWidth(text);
            g2.drawString(text, (WIDTH - tw) / 2, y);
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        
        switch(currentState) {
            case STATE_MAIN_MENU:
                handleMainMenuInput(code);
                break;
            case STATE_DIFFICULTY:
                handleDifficultyInput(code);
                break;
            case STATE_CONTROLS:
                handleControlsInput(code);
                break;
            case STATE_ABOUT:
                handleAboutInput(code);
                break;
        }
    }
    
    private void handleMainMenuInput(int code) {
        if (code == KeyEvent.VK_UP) {
            selectedOption = (selectedOption - 1 + MAIN_MENU_OPTIONS.length) % MAIN_MENU_OPTIONS.length;
            repaint();
        } else if (code == KeyEvent.VK_DOWN) {
            selectedOption = (selectedOption + 1) % MAIN_MENU_OPTIONS.length;
            repaint();
        } else if (code == KeyEvent.VK_ENTER) {
            switch(selectedOption) {
                case 0: // Start Game
                    currentState = STATE_DIFFICULTY;
                    selectedOption = 1; // Default to Normal
                    repaint();
                    break;
                case 1: // Controls
                    currentState = STATE_CONTROLS;
                    repaint();
                    break;
                case 2: // About
                    currentState = STATE_ABOUT;
                    repaint();
                    break;
                case 3: // Exit
                    System.exit(0);
                    break;
            }
        }
    }
    
    private void handleDifficultyInput(int code) {
        if (code == KeyEvent.VK_UP) {
            selectedOption = (selectedOption - 1 + DIFFICULTY_OPTIONS.length) % DIFFICULTY_OPTIONS.length;
            repaint();
        } else if (code == KeyEvent.VK_DOWN) {
            selectedOption = (selectedOption + 1) % DIFFICULTY_OPTIONS.length;
            repaint();
        } else if (code == KeyEvent.VK_ENTER) {
            startGame();
        } else if (code == KeyEvent.VK_ESCAPE) {
            currentState = STATE_MAIN_MENU;
            selectedOption = 0;
            repaint();
        }
    }
    
    private void handleControlsInput(int code) {
        if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_ESCAPE) {
            currentState = STATE_MAIN_MENU;
            selectedOption = 0;
            repaint();
        }
    }
    
    private void handleAboutInput(int code) {
        if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_ESCAPE) {
            currentState = STATE_MAIN_MENU;
            selectedOption = 0;
            repaint();
        }
    }
    
    private void startGame() {
        gameFrame.startGameWithDifficulty(selectedOption);
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
    }
}
