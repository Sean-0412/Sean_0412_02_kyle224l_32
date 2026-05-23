package com.spaceinvaders;

import javax.swing.JFrame;

/**
 * Main game window frame.
 */
public class GameFrame extends JFrame {
    private MenuPanel menuPanel;
    private GamePanel gamePanel;
    private Leaderboard leaderboard;
    private int lastGameMode;
    private int lastDifficulty;
    private boolean lastTwoPlayer;
    
    public GameFrame() {
        setTitle("Space Invaders");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        leaderboard = new Leaderboard();
        
        // Show menu first
        menuPanel = new MenuPanel(this);
        add(menuPanel);
        
        // Pack to get the preferred size
        pack();
        
        // Center the window on screen
        setLocationRelativeTo(null);
    }
    
    public void startGameWithSettings(int gameMode, int difficulty, boolean twoPlayer) {
        lastGameMode = gameMode;
        lastDifficulty = difficulty;
        lastTwoPlayer = twoPlayer;
        
        // Remove menu panel
        remove(menuPanel);
        
        // Create and add game panel with game mode, difficulty level, and player count
        gamePanel = new GamePanel(this, gameMode, difficulty, twoPlayer);
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

    public void returnToMainMenu() {
        // Remove game panel
        remove(gamePanel);
        
        // Recreate and show the main menu
        menuPanel = new MenuPanel(this);
        add(menuPanel);
        
        // Refresh
        revalidate();
        repaint();
        
        menuPanel.requestFocusInWindow();
    }

    public Leaderboard getLeaderboard() {
        return leaderboard;
    }
    
    @Deprecated
    public void startGameWithDifficulty(int difficulty) {
        startGameWithSettings(0, difficulty, false);
    }
}
