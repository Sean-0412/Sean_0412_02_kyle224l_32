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
import java.util.Random;

/**
 * Main game panel for Space Invaders game.
 * Handles game logic, rendering, and user input.
 * 
 * Game Modes:
 * - MODE_CLASSIC (0): Only left/right movement, game ends when enemies reach the line
 * - MODE_DODGING (1): Full movement (up/down/left/right), game ends when hit by enemies
 * - MODE_STAGE (2): Clear a sequence of stages, enemies get tougher each wave
 */
public class GamePanel extends JPanel implements ActionListener, KeyListener {
    // Game modes
    private static final int MODE_CLASSIC = 0;
    private static final int MODE_DODGING = 1;
    private static final int MODE_STAGE = 2;
    
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private static final int PLAYER_WIDTH = 70;
    private static final int PLAYER_HEIGHT = 18;
    private static final int PLAYER_SPEED = 7;
    private static final int PLAYER_VERTICAL_SPEED = 5;

    private static final int BULLET_WIDTH = 4;
    private static final int BULLET_HEIGHT = 12;
    private static final int BULLET_SPEED = 10;
    private static final int MAX_BULLETS = 12;
    private static final int ENEMY_BULLET_SPEED = 6;
    private static final int MAX_ENEMY_BULLETS = 10;
    private static final int ALIEN_SHOOT_MIN_INTERVAL = 60;
    private static final int ALIEN_SHOOT_MAX_INTERVAL = 120;
    private static final int BOSS_HEALTH = 20;
    private static final double ALIEN_DROP_SPEED = 0.18;

    private static final int MAX_POWERUPS = 2;
    private static final int POWERUP_SIZE = 24;
    private static final int POWERUP_FALL_SPEED = 2;
    private static final int POWERUP_SPAWN_RATE = 1200;
    private static final int ATTACK_BOOST_DURATION = 600;
    private static final int SHIELD_DURATION = 300; // 5 seconds (300 frames at 60fps)

    private static final int ULTIMATE_DURATION = 300; // 5 seconds (300 frames at 60fps)
    private static final int ULTIMATE_COOLDOWN = 600;  // 10 seconds cooldown
    private static final int ULTIMATE_FIRE_RATE = 3;   // Fire every 3 frames

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
    private final List<Bullet> enemyBullets = new ArrayList<Bullet>();
    private final List<PowerUp> powerUps = new ArrayList<PowerUp>();

    private int playerX;
    private int playerY;
    private int alienShootTimer;
    private int attackBoostRemaining;
    private int playerShotCount;
    private int shieldRemaining;
    private boolean bossSpawned;
    
    private boolean ultimateActive;
    private int ultimateDuration;
    private int ultimateCooldown;
    private int ultimateFireCounter;

    private boolean moveLeft;
    private boolean moveRight;
    private boolean moveUp;
    private boolean moveDown;
    private boolean shootPressed;

    private static final int START_LIVES = 5;

    private int fireCooldown;
    private int score;
    private int lives;

    private boolean gameOver;
    private boolean gameWin;
    private boolean gameOverSoundPlayed;
    private boolean battleMusicPlayed;
    private boolean bossMusicPlayed;
    private boolean defeatSoundPlayed;
    
    private boolean isPaused;
    private int resumeCountdown; // 3, 2, 1, then resume
    private int pausedSelectedOption; // 0 = Continue, 1 = Return to Difficulty
    private static final int PAUSE_CONTINUE = 0;
    private static final int PAUSE_RETURN = 1;

    private double alienBaseSpeed;
    private double alienSpeed;
    private int alienDirection;
    private int safeLineY; // For classic and stage modes: the line that enemies shouldn't cross
    
    private int difficulty; // 0 = Easy, 1 = Normal, 2 = Hard
    private int gameMode;   // 0 = Classic, 1 = Dodging, 2 = Stage
    private int currentLevel;
    private static final int MAX_STAGE_LEVELS = 5;
    private int remainingStageAliens;
    private int stageSpawnBatch = 5;
    private int stageNextSpawnCountdown;
    private static final int STAGE_SPAWN_INTERVAL = 120;
    private static final int STAGE_SPAWN_MIN_INTERVAL = 35;
    
    // Dodging mode specific variables
    private int remainingDodgingAliens;
    private int dodgingNextSpawnCountdown;
    private boolean dodgingBossSpawned;
    private int dodgingSpawnBatch = 1;
    private int dodgingWaveCount;
    private static final int DODGING_SPAWN_INTERVAL = 60;
    private static final int DODGING_BOSS_SPAWN_TIME = 6000; // 100 seconds at 60fps
    private static final int DODGING_TOTAL_ALIENS = 50;
    
    private Random random = new Random();
    private GameFrame gameFrame; // Reference to parent frame

    public GamePanel() {
        this(null, MODE_CLASSIC, 1); // Default to Classic mode with Normal difficulty
    }
    
    public GamePanel(int gameMode, int difficulty) {
        this(null, gameMode, difficulty);
    }
    
    public GamePanel(GameFrame gameFrame, int gameMode, int difficulty) {
        String modeName = gameMode == MODE_CLASSIC ? "Classic" : (gameMode == MODE_DODGING ? "Dodging" : "Stage");
        System.out.println("GamePanel: Constructor starting... (Mode: " + modeName + ", Difficulty: " + difficulty + ")");
        this.gameFrame = gameFrame;
        this.gameMode = gameMode;
        this.difficulty = difficulty;
        this.safeLineY = HEIGHT - 70;
        this.currentLevel = 1;
        this.lives = START_LIVES;
        this.alienShootTimer = 0;
        this.attackBoostRemaining = 0;
        this.bossSpawned = false;
        this.ultimateActive = false;
        this.ultimateDuration = 0;
        this.ultimateCooldown = 0;
        this.ultimateFireCounter = 0;
        this.playerShotCount = 1;
        this.shieldRemaining = 0;
        
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
        // Play battle music on game start
        if (!battleMusicPlayed) {
            battleMusicPlayed = true;
            SoundPlayer.playBattle();
        }
        requestFocusInWindow();
    }

    private void initAliens() {
        aliens.clear();
        int rows = Math.min(ALIEN_ROWS + currentLevel - 1, 7);

        if (gameMode == MODE_STAGE) {
            bossSpawned = false;
            remainingStageAliens = 20;
            int initialSpawn = Math.min(stageSpawnBatch, remainingStageAliens);
            spawnStageAliens(initialSpawn);
            remainingStageAliens -= initialSpawn;
            stageNextSpawnCountdown = STAGE_SPAWN_INTERVAL;
        } else if (gameMode == MODE_DODGING) {
            // Dodging mode: no initial aliens, spawn them one by one
            dodgingBossSpawned = false;
            remainingDodgingAliens = DODGING_TOTAL_ALIENS;
            dodgingWaveCount = 0;
            dodgingNextSpawnCountdown = DODGING_SPAWN_INTERVAL;
        } else {
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < ALIEN_COLS; col++) {
                    int x = START_X + col * (ALIEN_WIDTH + ALIEN_H_SPACING);
                    int y = START_Y + row * (ALIEN_HEIGHT + ALIEN_V_SPACING);
                    aliens.add(new Alien(x, y));
                }
            }
        }
        
        // Set initial speed based on difficulty and stage mode
        switch(difficulty) {
            case 0: // Easy
                alienBaseSpeed = (gameMode == MODE_STAGE || gameMode == MODE_DODGING) ? 1.0 + (currentLevel - 1) * 0.25 : 0.8;
                break;
            case 1: // Normal
                alienBaseSpeed = (gameMode == MODE_STAGE || gameMode == MODE_DODGING) ? 1.8 + (currentLevel - 1) * 0.25 : 1.8;
                break;
            case 2: // Hard
                alienBaseSpeed = (gameMode == MODE_STAGE || gameMode == MODE_DODGING) ? 2.6 + (currentLevel - 1) * 0.25 : 3.0;
                break;
            default:
                alienBaseSpeed = 1.8;
        }
        alienSpeed = alienBaseSpeed;
        alienDirection = 1;
    }

    private void restartGame() {
        lives = START_LIVES;
        score = 0;
        currentLevel = 1;
        bullets.clear();
        enemyBullets.clear();
        powerUps.clear();
        attackBoostRemaining = 0;
        shieldRemaining = 0;
        bossSpawned = false;
        dodgingBossSpawned = false;
        ultimateActive = false;
        ultimateDuration = 0;
        ultimateCooldown = 0;
        ultimateFireCounter = 0;
        playerX = (WIDTH - PLAYER_WIDTH) / 2;
        playerY = HEIGHT - 70;
        gameOver = false;
        gameWin = false;
        gameOverSoundPlayed = false;
        battleMusicPlayed = false;
        bossMusicPlayed = false;
        defeatSoundPlayed = false;
        fireCooldown = 0;
        alienShootTimer = 0;
        playerShotCount = 1;
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
        updateEnemyBullets();
        updatePowerUps();
        spawnPowerUps();
        if (attackBoostRemaining > 0) {
            attackBoostRemaining--;
        }
        if (shieldRemaining > 0) {
            shieldRemaining--;
        }
        
        // Update ultimate ability
        if (ultimateActive) {
            ultimateDuration--;
            if (ultimateDuration <= 0) {
                ultimateActive = false;
                ultimateCooldown = ULTIMATE_COOLDOWN;
            }
        }
        
        if (ultimateCooldown > 0) {
            ultimateCooldown--;
        }
        
        updateStageSpawning();
        updateAliens();
        updateAlienShooting();
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
        
        // Allow full movement in dodging and stage modes
        if (gameMode == MODE_DODGING || gameMode == MODE_STAGE) {
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
        
        if (gameMode == MODE_DODGING || gameMode == MODE_STAGE) {
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
            int bulletY = playerY - BULLET_HEIGHT;
            int effectiveShotCount = getEffectiveShotCount();
            int shotsToSpawn = Math.min(effectiveShotCount, MAX_BULLETS - bullets.size());
            int[] offsets = getShotOffsets(shotsToSpawn);
            for (int offset : offsets) {
                bullets.add(new Bullet(playerX + offset, bulletY));
            }
            fireCooldown = 10;
            SoundPlayer.playShoot();
        }
        
        // Ultimate ability continuous fire
        if (ultimateActive) {
            ultimateFireCounter++;
            if (ultimateFireCounter % ULTIMATE_FIRE_RATE == 0) {
                int bulletY = playerY - BULLET_HEIGHT;
                int leftX = playerX + 10;
                int centerX = playerX + (PLAYER_WIDTH / 2) - (BULLET_WIDTH / 2);
                int rightX = playerX + PLAYER_WIDTH - 20;

                if (bullets.size() < MAX_BULLETS) {
                    bullets.add(new Bullet(leftX, bulletY));
                }
                if (bullets.size() < MAX_BULLETS) {
                    bullets.add(new Bullet(centerX, bulletY));
                }
                if (bullets.size() < MAX_BULLETS) {
                    bullets.add(new Bullet(rightX, bulletY));
                }
                SoundPlayer.playShoot();
            }
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

    private int getEffectiveShotCount() {
        int count = playerShotCount;
        if (attackBoostRemaining > 0) {
            count *= 2;
        }
        return Math.max(1, count);
    }

    private int[] getShotOffsets(int shotCount) {
        int[] offsets = new int[shotCount];
        int maxOffset = PLAYER_WIDTH - BULLET_WIDTH;
        if (shotCount == 1) {
            offsets[0] = maxOffset / 2;
            return offsets;
        }
        for (int i = 0; i < shotCount; i++) {
            offsets[i] = (int) Math.round((double) i * maxOffset / (shotCount - 1));
        }
        return offsets;
    }

    private void updateAliens() {
        if (aliens.isEmpty()) {
            return;
        }

        for (Alien alien : aliens) {
            alien.updateMovement(alienSpeed, random, 8, WIDTH - 8 - ALIEN_WIDTH, ALIEN_DROP_SPEED);
        }

        // Remove aliens that move fully off-screen, or touch the bottom edge in stage mode.
        Iterator<Alien> iterator = aliens.iterator();
        while (iterator.hasNext()) {
            Alien alien = iterator.next();
            if (alien.x + ALIEN_WIDTH < 0 || alien.x > WIDTH || alien.y + ALIEN_HEIGHT >= HEIGHT || alien.y + ALIEN_HEIGHT < 0) {
                iterator.remove();
            }
        }

        // Increase difficulty over time through score milestones.
        if (gameMode == MODE_STAGE) {
            alienSpeed = alienBaseSpeed + (currentLevel - 1) * 0.15 + (score / 100.0) * 0.18;
        } else if (gameMode == MODE_DODGING) {
            alienSpeed = alienBaseSpeed + (score / 100.0) * 0.15;
        } else {
            alienSpeed = alienBaseSpeed + (score / 100.0) * 0.22;
        }
    }

    private void updateEnemyBullets() {
        Iterator<Bullet> iterator = enemyBullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            bullet.update();
            if (bullet.y > HEIGHT) {
                iterator.remove();
            }
        }
    }

    private void updatePowerUps() {
        Iterator<PowerUp> iterator = powerUps.iterator();
        Rectangle2D playerRect = new Rectangle2D.Double(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
        while (iterator.hasNext()) {
            PowerUp powerUp = iterator.next();
            powerUp.update();
            if (powerUp.y > HEIGHT) {
                iterator.remove();
                continue;
            }
            if (powerUp.getBounds().intersects(playerRect)) {
                applyPowerUp(powerUp.type);
                iterator.remove();
            }
        }
    }

    private void spawnPowerUps() {
        if (powerUps.size() >= MAX_POWERUPS || random.nextInt(POWERUP_SPAWN_RATE) != 0 || gameOver || isPaused) {
            return;
        }
        int x = 20 + random.nextInt(WIDTH - 40 - POWERUP_SIZE);
        int y = 40;
        int type;
        if (gameMode == MODE_STAGE) {
            int pick = random.nextInt(3);
            if (pick == 0) {
                type = PowerUp.TYPE_HEALTH;
            } else if (pick == 1) {
                type = PowerUp.TYPE_ATTACK;
            } else {
                type = PowerUp.TYPE_SHIELD;
            }
        } else {
            type = random.nextBoolean() ? PowerUp.TYPE_HEALTH : PowerUp.TYPE_ATTACK;
        }
        powerUps.add(new PowerUp(x, y, type, POWERUP_SIZE, POWERUP_FALL_SPEED));
    }

    private void applyPowerUp(int type) {
        if (type == PowerUp.TYPE_HEALTH) {
            lives = Math.min(lives + 1, START_LIVES + 3);
        } else if (type == PowerUp.TYPE_ATTACK) {
            attackBoostRemaining = ATTACK_BOOST_DURATION;
        } else if (type == PowerUp.TYPE_SHIELD) {
            shieldRemaining = SHIELD_DURATION;
        }
    }

    private void updateAlienShooting() {
        if (aliens.isEmpty()) {
            return;
        }

        int enemyBulletCap = Math.min(MAX_ENEMY_BULLETS + currentLevel * 2, 20);
        if (enemyBullets.size() >= enemyBulletCap) {
            alienShootTimer = Math.max(10, ALIEN_SHOOT_MIN_INTERVAL - (currentLevel - 1) * 4);
            return;
        }

        if (alienShootTimer > 0) {
            alienShootTimer--;
            return;
        }

        int shootCount = Math.min(1 + (currentLevel - 1) / 2, 3);
        int availableShots = Math.min(shootCount, enemyBulletCap - enemyBullets.size());
        for (int i = 0; i < availableShots; i++) {
            Alien shooter = aliens.get(random.nextInt(aliens.size()));
            int bulletX = (int) shooter.x + ALIEN_WIDTH / 2 - BULLET_WIDTH / 2;
            int bulletY = (int) shooter.y + ALIEN_HEIGHT;
            enemyBullets.add(new Bullet(bulletX, bulletY, ENEMY_BULLET_SPEED, true));
        }

        int minInterval = Math.max(18, ALIEN_SHOOT_MIN_INTERVAL - (currentLevel - 1) * 4);
        int maxInterval = Math.max(30, ALIEN_SHOOT_MAX_INTERVAL - (currentLevel - 1) * 6);
        alienShootTimer = minInterval + random.nextInt(maxInterval - minInterval + 1);
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
                    bulletIterator.remove();
                    if (alien.boss) {
                        alien.health--;
                        if (alien.health <= 0) {
                            alienIterator.remove();
                            score += 100;
                        }
                    } else {
                        alienIterator.remove();
                        score += 10;
                    }
                    SoundPlayer.playHit();
                    hit = true;
                    break;
                }
            }

            if (hit) {
                continue;
            }
        }

        Rectangle2D playerRect = new Rectangle2D.Double(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
        Iterator<Bullet> enemyIterator = enemyBullets.iterator();
        while (enemyIterator.hasNext()) {
            Bullet enemyBullet = enemyIterator.next();
            if (enemyBullet.getBounds().intersects(playerRect)) {
                enemyIterator.remove();
                if (shieldRemaining > 0) {
                    shieldRemaining = 0;
                    SoundPlayer.playHit();
                } else {
                    handlePlayerHit();
                }
                return;
            }
        }
    }

    private void checkGameState() {
        if (aliens.isEmpty()) {
            if (gameMode == MODE_STAGE) {
                if (remainingStageAliens <= 0) {
                    if (!bossSpawned) {
                        spawnBoss();
                        return;
                    }
                    advanceToNextStage();
                    return;
                }
                return;
            } else if (gameMode == MODE_DODGING) {
                // In dodging mode, continue spawning until boss is defeated
                // The spawning is handled in updateDodgingSpawning()
                return;
            }
            setGameOver(true);
            return;
        }
        
        // Check for collisions with player (only in dodging mode)
        if (gameMode == MODE_DODGING || gameMode == MODE_STAGE) {
            Iterator<Alien> alienIterator = aliens.iterator();
            while (alienIterator.hasNext()) {
                Alien alien = alienIterator.next();
                if (alien.getBounds().intersects(new Rectangle2D.Double(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT))) {
                    alienIterator.remove();
                    if (shieldRemaining > 0) {
                        shieldRemaining = 0;
                        SoundPlayer.playHit();
                    } else {
                        handlePlayerHit();
                    }
                    return;
                }
            }
        }
        
        // Check if aliens crossed the safety line (classic mode only)
        if (gameMode == MODE_CLASSIC) {
            for (Alien alien : aliens) {
                if (alien.y + ALIEN_HEIGHT >= safeLineY) {
                    setGameOver(false);
                    return;
                }
            }
        }
    }

    private void handlePlayerHit() {
        lives--;
        SoundPlayer.playHit();
        if (lives <= 0) {
            setGameOver(false);
            return;
        }

        // Reset player position after taking damage.
        playerX = (WIDTH - PLAYER_WIDTH) / 2;
        playerY = safeLineY;
        fireCooldown = 0;
        bullets.clear();
        moveLeft = false;
        moveRight = false;
        moveUp = false;
        moveDown = false;
        shootPressed = false;
    }

    private void setGameOver(boolean win) {
        if (!gameOver) {
            gameOver = true;
            gameWin = win;
            if (!gameOverSoundPlayed) {
                gameOverSoundPlayed = true;
                SoundPlayer.playGameOver();
            }
            // Play defeat sound if player loses
            if (!win && !defeatSoundPlayed) {
                defeatSoundPlayed = true;
                SoundPlayer.playDefeat();
            }
        } else {
            gameWin = win;
        }
    }

    private void advanceToNextStage() {
        bossSpawned = false;
        if (playerShotCount < 2) {
            playerShotCount = 2;
        }
        if (currentLevel < MAX_STAGE_LEVELS) {
            currentLevel++;
            bullets.clear();
            enemyBullets.clear();
            alienShootTimer = 0;
            playerX = (WIDTH - PLAYER_WIDTH) / 2;
            playerY = safeLineY;
            initAliens();
            return;
        }

        setGameOver(true);
    }

    private void updateStageSpawning() {
        if (gameMode == MODE_STAGE) {
            int spawnBatch = Math.min(stageSpawnBatch + currentLevel - 1, remainingStageAliens);
            int spawnInterval = Math.max(STAGE_SPAWN_MIN_INTERVAL, STAGE_SPAWN_INTERVAL - (currentLevel - 1) * 15);

            if (aliens.isEmpty() && remainingStageAliens > 0) {
                int spawnCount = Math.min(spawnBatch, remainingStageAliens);
                spawnStageAliens(spawnCount);
                remainingStageAliens -= spawnCount;
                stageNextSpawnCountdown = spawnInterval;
                return;
            }

            if (remainingStageAliens > 0) {
                stageNextSpawnCountdown--;
                if (stageNextSpawnCountdown <= 0) {
                    int spawnCount = Math.min(spawnBatch, remainingStageAliens);
                    spawnStageAliens(spawnCount);
                    remainingStageAliens -= spawnCount;
                    stageNextSpawnCountdown = spawnInterval;
                }
            }
        } else if (gameMode == MODE_DODGING) {
            updateDodgingSpawning();
        }
    }
    
    private void updateDodgingSpawning() {
        // Spawn one alien at a time in dodging mode
        if (dodgingBossSpawned && aliens.isEmpty()) {
            // Boss was defeated, continue spawning normal aliens
            dodgingBossSpawned = false;
            remainingDodgingAliens = DODGING_TOTAL_ALIENS;
            dodgingWaveCount = 0;
            dodgingNextSpawnCountdown = DODGING_SPAWN_INTERVAL;
        }
        
        if (remainingDodgingAliens > 0) {
            dodgingNextSpawnCountdown--;
            if (dodgingNextSpawnCountdown <= 0) {
                int x = 20 + random.nextInt(WIDTH - 40 - ALIEN_WIDTH);
                int y = START_Y;
                aliens.add(new Alien(x, y));
                remainingDodgingAliens--;
                dodgingWaveCount++;
                dodgingNextSpawnCountdown = DODGING_SPAWN_INTERVAL;
                
                // Spawn boss after certain wave count
                if (dodgingWaveCount >= (DODGING_BOSS_SPAWN_TIME / DODGING_SPAWN_INTERVAL) && !dodgingBossSpawned) {
                    spawnDodgingBoss();
                }
            }
        }
    }
    
    private void spawnDodgingBoss() {
        dodgingBossSpawned = true;
        remainingDodgingAliens = 0;
        int x = (WIDTH - ALIEN_WIDTH) / 2;
        int y = START_Y;
        aliens.add(new Alien(x, y, BOSS_HEALTH, true));
        alienDirection = 1;
        alienSpeed = alienBaseSpeed;
        // Play boss music on boss spawn
        if (!bossMusicPlayed) {
            bossMusicPlayed = true;
            SoundPlayer.playBoss();
        }
    }

    private void spawnStageAliens(int count) {
        boolean wasEmpty = aliens.isEmpty();
        for (int i = 0; i < count; i++) {
            int x = 20 + random.nextInt(WIDTH - 40 - ALIEN_WIDTH);
            int y = START_Y + random.nextInt(180);
            aliens.add(new Alien(x, y));
        }
        if (wasEmpty) {
            alienDirection = 1;
        }
        if (gameMode == MODE_STAGE) {
            alienSpeed = alienBaseSpeed + (currentLevel - 1) * 0.15 + (score / 100.0) * 0.18;
        }
    }

    private void spawnBoss() {
        bossSpawned = true;
        int x = (WIDTH - ALIEN_WIDTH) / 2;
        int y = START_Y;
        aliens.add(new Alien(x, y, BOSS_HEALTH, true));
        alienDirection = 1;
        alienSpeed = alienBaseSpeed + (currentLevel - 1) * 0.15 + (score / 100.0) * 0.18;
        // Play boss music on boss spawn
        if (!bossMusicPlayed) {
            bossMusicPlayed = true;
            SoundPlayer.playBoss();
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
        drawPowerUps(g2);
        
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
        g2.drawString("Lives: " + lives, 20, 55);
        
        // Draw game mode
        String modeText;
        Color modeColor;
        if (gameMode == MODE_CLASSIC) {
            modeText = "CLASSIC";
            modeColor = new Color(100, 150, 255);
        } else if (gameMode == MODE_DODGING) {
            modeText = "DODGING";
            modeColor = new Color(255, 100, 150);
        } else {
            modeText = "STAGE";
            modeColor = new Color(180, 255, 100);
        }
        g2.setColor(modeColor);
        g2.drawString("Mode: " + modeText, 20, 85);
        
        // Draw difficulty level (only for non-classic modes)
        if (gameMode != MODE_CLASSIC) {
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
        }
        if (gameMode == MODE_STAGE) {
            g2.drawString("Stage: " + currentLevel, WIDTH - 250, 55);
        }
        g2.drawString("Aliens: " + aliens.size(), WIDTH - 250, 80);

        g2.setFont(new Font("Consolas", Font.PLAIN, 14));
        g2.setColor(new Color(170, 255, 210));
        if (attackBoostRemaining > 0) {
            g2.drawString("Attack Powerup: " + (attackBoostRemaining / 60) + "s", 20, 135);
        }
        if (shieldRemaining > 0) {
            g2.drawString("Shield: " + (shieldRemaining / 60) + "s", 20, 155);
        }
        
        // Display ultimate ability status
        if (ultimateActive) {
            g2.setColor(new Color(255, 100, 255));
            g2.drawString("ULTIMATE ACTIVE: " + (ultimateDuration / 60) + "s", 20, 150);
        } else if (ultimateCooldown > 0) {
            g2.setColor(new Color(200, 100, 255));
            g2.drawString("Ultimate Cooldown: " + (ultimateCooldown / 60) + "s (Press U)", 20, 150);
        } else {
            g2.setColor(new Color(100, 255, 100));
            g2.drawString("Ultimate Ready (Press U)", 20, 150);
        }
        
        if (gameMode == MODE_DODGING || gameMode == MODE_STAGE) {
            g2.setColor(new Color(170, 255, 210));
            g2.drawString("Move: Arrows  Shoot: Space", 20, 165);
        } else {
            g2.setColor(new Color(170, 255, 210));
            g2.drawString("Move: Left/Right  Shoot: Space", 20, 165);
        }
        g2.setColor(new Color(170, 255, 210));
        g2.drawString("Ultimate: U  Pause: P  Menu: Esc  Restart: R", 20, 185);
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
        String[] options = {"Continue Game", "Return to Menu"};
        
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

        if (shieldRemaining > 0) {
            g2.setColor(new Color(80, 200, 255, 120));
            g2.setStroke(new java.awt.BasicStroke(4));
            g2.drawOval(playerX - 8, playerY - 8, PLAYER_WIDTH + 16, PLAYER_HEIGHT + 16);
        }
    }

    private void drawBullets(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        for (Bullet bullet : bullets) {
            g2.fillRect(bullet.x, bullet.y, BULLET_WIDTH, BULLET_HEIGHT);
        }
        g2.setColor(new Color(255, 120, 80));
        for (Bullet bullet : enemyBullets) {
            g2.fillRect(bullet.x, bullet.y, BULLET_WIDTH, BULLET_HEIGHT);
        }
    }

    private void drawPowerUps(Graphics2D g2) {
        for (PowerUp powerUp : powerUps) {
            powerUp.draw(g2);
        }
    }

    private void drawAliens(Graphics2D g2) {
        for (Alien alien : aliens) {
            if (alien.boss) {
                g2.setColor(new Color(255, 120, 80));
                g2.fillRoundRect((int) alien.x, (int) alien.y, ALIEN_WIDTH, ALIEN_HEIGHT, 8, 8);
                g2.setColor(new Color(200, 50, 50));
                g2.drawRoundRect((int) alien.x, (int) alien.y, ALIEN_WIDTH, ALIEN_HEIGHT, 8, 8);
                int barWidth = ALIEN_WIDTH;
                int barHeight = 6;
                int barX = (int) alien.x;
                int barY = (int) alien.y - 12;
                g2.setColor(Color.DARK_GRAY);
                g2.fillRect(barX, barY, barWidth, barHeight);
                g2.setColor(new Color(255, 80, 80));
                int healthWidth = Math.max(0, (int) ((alien.health / (double) BOSS_HEALTH) * barWidth));
                g2.fillRect(barX, barY, healthWidth, barHeight);
            } else {
                g2.setColor(new Color(150, 255, 80));
                g2.fillRoundRect((int) alien.x, (int) alien.y, ALIEN_WIDTH, ALIEN_HEIGHT, 6, 6);
                g2.setColor(new Color(30, 80, 20));
                g2.fillOval((int) alien.x + 8, (int) alien.y + 7, 6, 6);
                g2.fillOval((int) alien.x + 22, (int) alien.y + 7, 6, 6);
            }
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

        if (code == KeyEvent.VK_ESCAPE && !gameOver) {
            gameTimer.stop();
            if (gameFrame != null) {
                gameFrame.returnToMainMenu();
            }
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
        } else if (code == KeyEvent.VK_U && !gameOver && !isPaused && ultimateCooldown == 0) {
            // Activate ultimate ability
            ultimateActive = true;
            ultimateDuration = ULTIMATE_DURATION;
            ultimateFireCounter = 0;
            SoundPlayer.playShoot();
        } else if (code == KeyEvent.VK_R && gameOver) {
            restartGame();
        }
    }
    
    private void handlePauseMenuChoice() {
        if (pausedSelectedOption == PAUSE_CONTINUE) {
            // Start countdown to resume (3 seconds = 3000ms / 16ms per frame ≈ 188 frames)
            resumeCountdown = 188;
        } else if (pausedSelectedOption == PAUSE_RETURN) {
            // Return to main menu
            gameTimer.stop();
            if (gameFrame != null) {
                gameFrame.returnToMainMenu();
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
