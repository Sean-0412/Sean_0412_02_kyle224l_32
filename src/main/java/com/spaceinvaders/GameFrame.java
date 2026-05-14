package com.spaceinvaders;

import javax.swing.JFrame;

/**
 * Main game window frame.
 */
public class GameFrame extends JFrame {
    private MenuPanel menuPanel;
    private GamePanel gamePanel;
    
    public GameFrame() {
        setTitle("Space Invaders");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // Show menu first
        menuPanel = new MenuPanel(this);
        add(menuPanel);
        
        // Pack to get the preferred size
        pack();
        
        // Center the window on screen
        setLocationRelativeTo(null);
    }
    
    public void startGameWithDifficulty(int difficulty) {
        // Remove menu panel
        remove(menuPanel);
        
        // Create and add game panel with difficulty level
        gamePanel = new GamePanel(difficulty);
        add(gamePanel);
        
        // Refresh
        revalidate();
        repaint();
        
        // Start the game
        gamePanel.startGame();
    }
}
