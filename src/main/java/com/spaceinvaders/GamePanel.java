package com.spaceinvaders;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    // Game constants
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    public static final int MODE_CLASSIC = 0;
    public static final int MODE_DODGING = 1;
    public static final int MODE_STAGE = 2;
    private static final int START_LIVES = 5;
    private static final int MAX_STAGE_LEVELS = 5;

    // Game state
    private final Timer gameTimer;
    private int score;
    private boolean gameOver;
    private boolean gameWin;
    private boolean isPaused;
    private int resumeCountdown;
    private int pausedSelectedOption;
    private int currentLevel;

    // Game mode specific state
    private int remainingStageAliens;
    private int stageSpawnBatch = 5;
    private int stageNextSpawnCountdown;
    private int remainingDodgingAliens;
    private int dodgingNextSpawnCountdown;
    private int dodgingWaveCount;
    
    // Sound flags
    private boolean battleMusicPlayed;
    private boolean bossMusicPlayed;
    private boolean gameOverSoundPlayed;
    private boolean defeatSoundPlayed;

    // Components
    private final GameFrame gameFrame;
    private final Player player1;
    private Player player2;
    private final EntityManager entityManager;
    private final GameRenderer gameRenderer;

    // Settings
    private final int gameMode;
    private final int difficulty;
    private final boolean twoPlayer;
    private double alienBaseSpeed;
    private double alienSpeed;


    public GamePanel(GameFrame gameFrame, int gameMode, int difficulty, boolean twoPlayer) {
        this.gameFrame = gameFrame;
        this.gameMode = gameMode;
        this.difficulty = difficulty;
        this.twoPlayer = twoPlayer;
        
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        this.entityManager = new EntityManager(this);
        this.player1 = new Player(WIDTH / 2 - 35, HEIGHT - 70, gameMode, true, START_LIVES);
        if (twoPlayer) {
            this.player2 = new Player(WIDTH / 2 - 35, HEIGHT - 110, gameMode, false, START_LIVES);
        }
        this.gameRenderer = new GameRenderer(this, entityManager);

        this.gameTimer = new Timer(16, this);
        restartGame();
    }

    public void startGame() {
        if (!gameTimer.isRunning()) {
            gameTimer.start();
        }
        if (!battleMusicPlayed) {
            battleMusicPlayed = true;
            SoundPlayer.playBattle();
        }
        requestFocusInWindow();
    }

    private void restartGame() {
        SoundPlayer.stopAllSounds();
        score = 0;
        currentLevel = 1;
        gameOver = false;
        gameWin = false;
        isPaused = false;
        resumeCountdown = 0;
        
        battleMusicPlayed = false;
        bossMusicPlayed = false;
        gameOverSoundPlayed = false;
        defeatSoundPlayed = false;

        player1.reset();
        if (twoPlayer) {
            player2.reset();
        }
        entityManager.initAliens(gameMode, currentLevel, difficulty);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateGame();
        repaint();
    }

    private void updateGame() {
        if (gameOver) return;

        if (isPaused) {
            if (resumeCountdown > 0) {
                resumeCountdown--;
                if (resumeCountdown == 0) {
                    isPaused = false;
                }
            }
            return;
        }

        player1.update();
        if (twoPlayer) {
            player2.update();
        }
        
        entityManager.update(player1, player2, twoPlayer);
        entityManager.spawnPowerUps(gameMode, currentLevel);
        entityManager.updateStageSpawning();
        
        setAlienSpeed(difficulty, gameMode, currentLevel);
        checkGameState();
    }

    private void checkGameState() {
        if (entityManager.areAliensEmpty()) {
            if (gameMode == MODE_STAGE) {
                if (remainingStageAliens <= 0) {
                    if (entityManager.isBossSpawnPending()) {
                        return;
                    }
                    if (!entityManager.isBossSpawned()) {
                        entityManager.spawnBoss();
                        return;
                    }
                    advanceToNextStage();
                }
            } else if (gameMode != MODE_DODGING) {
                setGameOver(true);
            }
        }

        if (gameMode == MODE_CLASSIC) {
            for (Alien alien : entityManager.getAliens()) {
                if (alien.y + 24 >= HEIGHT - 70) {
                    setGameOver(false);
                    return;
                }
            }
        }
        
        if (!player1.isAlive() && (!twoPlayer || !player2.isAlive())) {
            setGameOver(false);
        }
    }

    private void advanceToNextStage() {
        if (currentLevel < MAX_STAGE_LEVELS) {
            currentLevel++;
            player1.increaseShotCount();
            if(twoPlayer) player2.increaseShotCount();
            entityManager.initAliens(gameMode, currentLevel, difficulty);
        } else {
            setGameOver(true);
        }
    }

    private void setGameOver(boolean win) {
        if (!gameOver) {
            gameOver = true;
            gameWin = win;
            SoundPlayer.stopBackgroundMusic();
            if (gameFrame != null && gameMode == MODE_STAGE) {
                gameFrame.getLeaderboard().addScore(score, twoPlayer);
            }
            if (!gameOverSoundPlayed) {
                gameOverSoundPlayed = true;
                SoundPlayer.playGameOver();
            }
            if (!win && !defeatSoundPlayed) {
                defeatSoundPlayed = true;
                SoundPlayer.playDefeat();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        gameRenderer.paint((Graphics2D) g);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (isPaused && resumeCountdown == 0) {
            handlePauseMenuInput(code);
            return;
        }
        
        if (code == KeyEvent.VK_P && !gameOver) {
            isPaused = true;
            pausedSelectedOption = 0;
            return;
        }
        if (code == KeyEvent.VK_ESCAPE && !gameOver) {
            gameTimer.stop();
            if (gameFrame != null) gameFrame.returnToMainMenu();
            return;
        }
        if (code == KeyEvent.VK_R && gameOver) {
            restartGame();
            startGame();
            return;
        }

        handlePlayerInput(code, true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        handlePlayerInput(e.getKeyCode(), false);
    }

    private void handlePlayerInput(int code, boolean isPressed) {
        // Player 1
        if (code == KeyEvent.VK_LEFT) player1.setMoveLeft(isPressed);
        else if (code == KeyEvent.VK_RIGHT) player1.setMoveRight(isPressed);
        else if (code == KeyEvent.VK_UP) player1.setMoveUp(isPressed);
        else if (code == KeyEvent.VK_DOWN) player1.setMoveDown(isPressed);
        else if (code == KeyEvent.VK_SPACE) player1.setShootPressed(isPressed);
        else if (code == KeyEvent.VK_U && isPressed && !twoPlayer) player1.activateUltimate();

        // Player 2
        if (twoPlayer) {
            if (code == KeyEvent.VK_A) player2.setMoveLeft(isPressed);
            else if (code == KeyEvent.VK_D) player2.setMoveRight(isPressed);
            else if (code == KeyEvent.VK_W) player2.setMoveUp(isPressed);
            else if (code == KeyEvent.VK_S) player2.setMoveDown(isPressed);
            else if (code == KeyEvent.VK_H) player2.setShootPressed(isPressed);
        }
    }
    
    private void handlePauseMenuInput(int code) {
        if (code == KeyEvent.VK_UP) {
            pausedSelectedOption = (pausedSelectedOption - 1 + 2) % 2;
        } else if (code == KeyEvent.VK_DOWN) {
            pausedSelectedOption = (pausedSelectedOption + 1) % 2;
        } else if (code == KeyEvent.VK_ENTER) {
            if (pausedSelectedOption == 0) { // Continue
                resumeCountdown = 180;
            } else { // Return to Menu
                gameTimer.stop();
                if (gameFrame != null) gameFrame.returnToMainMenu();
            }
        }
    }
    
    public void setAlienSpeed(int difficulty, int gameMode, int currentLevel) {
        double difficultyMultiplier = getDifficultyMultiplier();
        if (gameMode == MODE_CLASSIC) {
            alienBaseSpeed = 1.8;
        } else {
            alienBaseSpeed = 1.0 + (currentLevel - 1) * 0.25;
        }
        double scoreFactor = (gameMode == MODE_CLASSIC) ? 0.22 : 0.15;
        alienSpeed = (alienBaseSpeed + (score / 100.0) * scoreFactor) * difficultyMultiplier;
    }

    public double getDifficultyMultiplier() {
        if (gameMode == MODE_CLASSIC) {
            return 1.0;
        }
        switch (difficulty) {
            case 0: return 1.0;
            case 1: return 2.0;
            default: return 4.0;
        }
    }

    // Getters for other classes
    public int getScore() { return score; }
    public void addScore(int points) { this.score += points; }
    public int getGameMode() { return gameMode; }
    public int getDifficulty() { return difficulty; }
    public int getCurrentLevel() { return currentLevel; }
    public boolean isTwoPlayer() { return twoPlayer; }
    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public boolean isGameOver() { return gameOver; }
    public boolean isGameWin() { return gameWin; }
    public boolean isPaused() { return isPaused; }
    public int getResumeCountdown() { return resumeCountdown; }
    public int getPausedSelectedOption() { return pausedSelectedOption; }
    public EntityManager getEntityManager() { return entityManager; }
    public double getAlienSpeed() { return alienSpeed; }
    public boolean isBossMusicPlayed() { return bossMusicPlayed; }
    public void setBossMusicPlayed(boolean played) { this.bossMusicPlayed = played; }
    
    public int getRemainingStageAliens() { return remainingStageAliens; }
    public void setRemainingStageAliens(int count) { this.remainingStageAliens = count; }
    public int getStageSpawnBatch() { return stageSpawnBatch; }
    public int getStageNextSpawnCountdown() { return stageNextSpawnCountdown; }
    public void setStageNextSpawnCountdown(int count) { this.stageNextSpawnCountdown = count; }
    public void decrementStageNextSpawnCountdown() { this.stageNextSpawnCountdown--; }
    
    public int getRemainingDodgingAliens() { return remainingDodgingAliens; }
    public void setRemainingDodgingAliens(int count) { this.remainingDodgingAliens = count; }
    public int getDodgingNextSpawnCountdown() { return dodgingNextSpawnCountdown; }
    public void setDodgingNextSpawnCountdown(int count) { this.dodgingNextSpawnCountdown = count; }
    public void decrementDodgingNextSpawnCountdown() { this.dodgingNextSpawnCountdown--; }
    public int getDodgingWaveCount() { return dodgingWaveCount; }
    public void incrementDodgingWaveCount() { this.dodgingWaveCount++; }
    public void setDodgingWaveCount(int count) { this.dodgingWaveCount = count; }

    @Override
    public void keyTyped(KeyEvent e) {}
}
