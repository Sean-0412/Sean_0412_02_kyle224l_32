package com.spaceinvaders;

import javax.swing.JFrame;

/**
 * Main game window frame.
 */
public class GameFrame extends JFrame {
    private MenuPanel menuPanel;
    private GamePanel gamePanel;
    private int lastGameMode;
    private int lastDifficulty;
    
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
    
    public void startGameWithSettings(int gameMode, int difficulty) {
        lastGameMode = gameMode;
        lastDifficulty = difficulty;
        
        // Remove menu panel
        remove(menuPanel);
        
        // Create and add game panel with game mode and difficulty level
        gamePanel = new GamePanel(this, gameMode, difficulty);
        add(gamePanel);
        
        // Refresh
        revalidate();
        repaint();
        
        // Start the game
        gamePanel.startGame();
    }
    
    public void returnToDifficultyMenu() {
        // Remove game panel
        remove(gamePanel);
        
        // Recreate and show menu at difficulty selection
        menuPanel = new MenuPanel(this);
        menuPanel.setInitialStateToDifficultyMenu(lastGameMode);
        add(menuPanel);
        
        // Refresh
        revalidate();
        repaint();
        
        menuPanel.requestFocusInWindow();
    }
    
    @Deprecated
    public void startGameWithDifficulty(int difficulty) {
        startGameWithSettings(0, difficulty);
    }
}
