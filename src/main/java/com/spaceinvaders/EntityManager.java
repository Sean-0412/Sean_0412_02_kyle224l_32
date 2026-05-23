package com.spaceinvaders;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class EntityManager {
    private final List<Alien> aliens = new ArrayList<>();
    private final List<Bullet> enemyBullets = new ArrayList<>();
    private final List<PowerUp> powerUps = new ArrayList<>();
    private final Random random = new Random();
    private final GamePanel gamePanel;

    private int alienShootTimer;
    private boolean bossSpawned;
    private boolean dodgingBossSpawned;

    public EntityManager(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void update(Player player1, Player player2, boolean twoPlayer) {
        updateAliens();
        updateEnemyBullets();
        updatePowerUps(player1, player2, twoPlayer);
        updateAlienShooting();
        checkCollisions(player1, player2, twoPlayer);
    }

    public void draw(Graphics2D g2) {
        drawAliens(g2);
        drawEnemyBullets(g2);
        drawPowerUps(g2);
    }

    public void reset() {
        aliens.clear();
        enemyBullets.clear();
        powerUps.clear();
        alienShootTimer = 0;
        bossSpawned = false;
        dodgingBossSpawned = false;
    }

    public void initAliens(int gameMode, int currentLevel, int difficulty) {
        reset();
        int rows = Math.min(4 + currentLevel - 1, 7);

        if (gameMode == GamePanel.MODE_STAGE) {
            gamePanel.setRemainingStageAliens(20);
            int initialSpawn = Math.min(gamePanel.getStageSpawnBatch(), gamePanel.getRemainingStageAliens());
            spawnStageAliens(initialSpawn, currentLevel);
            gamePanel.setRemainingStageAliens(gamePanel.getRemainingStageAliens() - initialSpawn);
            gamePanel.setStageNextSpawnCountdown(120);
        } else if (gameMode == GamePanel.MODE_DODGING) {
            gamePanel.setRemainingDodgingAliens(50);
            gamePanel.setDodgingWaveCount(0);
            gamePanel.setDodgingNextSpawnCountdown(60);
        } else {
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < 10; col++) {
                    int x = 80 + col * (36 + 18);
                    int y = 70 + row * (24 + 14);
                    aliens.add(new Alien(x, y));
                }
            }
        }
        gamePanel.setAlienSpeed(difficulty, gameMode, currentLevel);
    }

    private void updateAliens() {
        if (aliens.isEmpty()) return;

        double dropSpeed = 0.18;
        if (gamePanel.getGameMode() != GamePanel.MODE_CLASSIC) {
            dropSpeed *= gamePanel.getDifficultyMultiplier();
        }
        double maxBossAmplitude = (GamePanel.HEIGHT / 2.0) - Alien.HEIGHT - 10;
        for (Alien alien : aliens) {
            if (alien.boss) {
                alien.updateBossMovement(gamePanel.getAlienSpeed(), random, 8, GamePanel.WIDTH - 8 - 36, maxBossAmplitude);
            } else {
                alien.updateMovement(gamePanel.getAlienSpeed(), random, 8, GamePanel.WIDTH - 8 - 36, dropSpeed);
            }
            if (alien.boss) {
                double maxBossY = (GamePanel.HEIGHT / 2.0) - Alien.HEIGHT;
                if (alien.y > maxBossY) {
                    alien.y = maxBossY;
                }
            }
        }

        aliens.removeIf(alien -> alien.x + 36 < 0 || alien.x > GamePanel.WIDTH || alien.y + 24 >= GamePanel.HEIGHT || alien.y + 24 < 0);
    }

    private void updateEnemyBullets() {
        enemyBullets.removeIf(bullet -> {
            bullet.update();
            return bullet.y > GamePanel.HEIGHT;
        });
    }

    private void updatePowerUps(Player player1, Player player2, boolean twoPlayer) {
        Iterator<PowerUp> iterator = powerUps.iterator();
        while (iterator.hasNext()) {
            PowerUp powerUp = iterator.next();
            powerUp.update();
            if (powerUp.y > GamePanel.HEIGHT) {
                iterator.remove();
                continue;
            }
            if (player1.isAlive() && powerUp.getBounds().intersects(player1.getBounds())) {
                player1.applyPowerUp(powerUp.type);
                iterator.remove();
            } else if (twoPlayer && player2.isAlive() && powerUp.getBounds().intersects(player2.getBounds())) {
                player2.applyPowerUp(powerUp.type);
                iterator.remove();
            }
        }
    }

    public void spawnPowerUps(int gameMode, int currentLevel) {
        int spawnRate = 1200;
        if (gameMode == GamePanel.MODE_STAGE) {
            spawnRate = Math.max(200, 1200 - (currentLevel - 1) * 120);
        }
        if (powerUps.size() >= 2 || random.nextInt(spawnRate) != 0) {
            return;
        }
        int x = 20 + random.nextInt(GamePanel.WIDTH - 40 - 24);
        int y = 40;
        int type = (gameMode == GamePanel.MODE_STAGE) ? random.nextInt(3) : (random.nextBoolean() ? 0 : 1);
        powerUps.add(new PowerUp(x, y, type, 24, 2));
    }

    private void updateAlienShooting() {
        if (gamePanel.getGameMode() == GamePanel.MODE_CLASSIC || aliens.isEmpty()) {
            return;
        }

        double rateMultiplier = gamePanel.getDifficultyMultiplier();

        int enemyBulletCap = Math.min(10 + gamePanel.getCurrentLevel() * 2, 20);
        if (enemyBullets.size() >= enemyBulletCap) {
            int baseCooldown = Math.max(10, 60 - (gamePanel.getCurrentLevel() - 1) * 4);
            alienShootTimer = scaleInterval(baseCooldown, rateMultiplier);
            return;
        }

        if (alienShootTimer > 0) {
            alienShootTimer--;
            return;
        }

        int shootCount = Math.min(1 + (gamePanel.getCurrentLevel() - 1) / 2, 3);
        int availableShots = Math.min(shootCount, enemyBulletCap - enemyBullets.size());
        for (int i = 0; i < availableShots; i++) {
            Alien shooter = aliens.get(random.nextInt(aliens.size()));
            enemyBullets.add(new Bullet((int) shooter.x + 16, (int) shooter.y + 24, 6, true));
        }

        int baseMinInterval = Math.max(18, 60 - (gamePanel.getCurrentLevel() - 1) * 4);
        int baseMaxInterval = Math.max(30, 120 - (gamePanel.getCurrentLevel() - 1) * 6);
        int minInterval = scaleInterval(baseMinInterval, rateMultiplier);
        int maxInterval = scaleInterval(baseMaxInterval, rateMultiplier);
        alienShootTimer = minInterval + random.nextInt(maxInterval - minInterval + 1);
    }

    private int scaleInterval(int baseInterval, double multiplier) {
        return Math.max(4, (int) Math.round(baseInterval / multiplier));
    }

    private void checkCollisions(Player player1, Player player2, boolean twoPlayer) {
        checkBulletHitsAliens(player1.getBullets());
        if (twoPlayer) {
            checkBulletHitsAliens(player2.getBullets());
        }

        checkEnemyBulletHitsPlayer(player1);
        if (twoPlayer) {
            checkEnemyBulletHitsPlayer(player2);
        }

        if (gamePanel.getGameMode() == GamePanel.MODE_DODGING || gamePanel.getGameMode() == GamePanel.MODE_STAGE) {
            checkAlienHitsPlayer(player1);
            if (twoPlayer) {
                checkAlienHitsPlayer(player2);
            }
        }
    }

    private void checkBulletHitsAliens(List<Bullet> playerBullets) {
        Iterator<Bullet> bulletIterator = playerBullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            Iterator<Alien> alienIterator = aliens.iterator();
            while (alienIterator.hasNext()) {
                Alien alien = alienIterator.next();
                if (bullet.getBounds().intersects(alien.getBounds())) {
                    bulletIterator.remove();
                    if (alien.boss) {
                        alien.health--;
                        if (alien.health <= 0) {
                            alienIterator.remove();
                            gamePanel.addScore(100);
                            SoundPlayer.stopBackgroundMusic();
                        }
                    } else {
                        alienIterator.remove();
                        gamePanel.addScore(10);
                    }
                    SoundPlayer.playHit();
                    return; 
                }
            }
        }
    }

    private void checkEnemyBulletHitsPlayer(Player player) {
        if (!player.isAlive()) return;
        Iterator<Bullet> iterator = enemyBullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            if (bullet.getBounds().intersects(player.getBounds())) {
                iterator.remove();
                player.handleHit();
                return;
            }
        }
    }

    private void checkAlienHitsPlayer(Player player) {
        if (!player.isAlive()) return;
        Iterator<Alien> iterator = aliens.iterator();
        while (iterator.hasNext()) {
            Alien alien = iterator.next();
            if (alien.getBounds().intersects(player.getBounds())) {
                iterator.remove();
                player.handleHit();
                return;
            }
        }
    }
    
    public void updateStageSpawning() {
        int gameMode = gamePanel.getGameMode();
        if (gameMode == GamePanel.MODE_STAGE) {
            int spawnBatch = Math.min(gamePanel.getStageSpawnBatch() + gamePanel.getCurrentLevel() - 1, gamePanel.getRemainingStageAliens());
            int spawnInterval = Math.max(35, 120 - (gamePanel.getCurrentLevel() - 1) * 15);

            if (aliens.isEmpty() && gamePanel.getRemainingStageAliens() > 0) {
                int spawnCount = Math.min(spawnBatch, gamePanel.getRemainingStageAliens());
                spawnStageAliens(spawnCount, gamePanel.getCurrentLevel());
                gamePanel.setRemainingStageAliens(gamePanel.getRemainingStageAliens() - spawnCount);
                gamePanel.setStageNextSpawnCountdown(spawnInterval);
                return;
            }

            if (gamePanel.getRemainingStageAliens() > 0) {
                gamePanel.decrementStageNextSpawnCountdown();
                if (gamePanel.getStageNextSpawnCountdown() <= 0) {
                    int spawnCount = Math.min(spawnBatch, gamePanel.getRemainingStageAliens());
                    spawnStageAliens(spawnCount, gamePanel.getCurrentLevel());
                    gamePanel.setRemainingStageAliens(gamePanel.getRemainingStageAliens() - spawnCount);
                    gamePanel.setStageNextSpawnCountdown(spawnInterval);
                }
            }
        } else if (gameMode == GamePanel.MODE_DODGING) {
            updateDodgingSpawning();
        }
    }

    private void updateDodgingSpawning() {
        if (dodgingBossSpawned && aliens.isEmpty()) {
            dodgingBossSpawned = false;
            gamePanel.setRemainingDodgingAliens(50);
            gamePanel.setDodgingWaveCount(0);
            gamePanel.setDodgingNextSpawnCountdown(60);
        }
        
        if (gamePanel.getRemainingDodgingAliens() > 0) {
            gamePanel.decrementDodgingNextSpawnCountdown();
            if (gamePanel.getDodgingNextSpawnCountdown() <= 0) {
                aliens.add(new Alien(20 + random.nextInt(GamePanel.WIDTH - 40 - 36), 70));
                gamePanel.setRemainingDodgingAliens(gamePanel.getRemainingDodgingAliens() - 1);
                gamePanel.incrementDodgingWaveCount();
                gamePanel.setDodgingNextSpawnCountdown(60);
                
                if (gamePanel.getDodgingWaveCount() >= (6000 / 60) && !dodgingBossSpawned) {
                    spawnDodgingBoss();
                }
            }
        }
    }

    public void spawnBoss() {
        bossSpawned = true;
        Alien bossAlien = new Alien((GamePanel.WIDTH - 36) / 2, 70, 20, true);
        bossAlien.initBossMovement(random, (GamePanel.HEIGHT / 2.0) - Alien.HEIGHT - 10);
        aliens.add(bossAlien);
        gamePanel.setAlienSpeed(gamePanel.getDifficulty(), gamePanel.getGameMode(), gamePanel.getCurrentLevel());
        if (!gamePanel.isBossMusicPlayed()) {
            gamePanel.setBossMusicPlayed(true);
            SoundPlayer.playBoss();
        }
    }
    
    private void spawnDodgingBoss() {
        dodgingBossSpawned = true;
        gamePanel.setRemainingDodgingAliens(0);
        Alien bossAlien = new Alien((GamePanel.WIDTH - 36) / 2, 70, 20, true);
        bossAlien.initBossMovement(random, (GamePanel.HEIGHT / 2.0) - Alien.HEIGHT - 10);
        aliens.add(bossAlien);
        gamePanel.setAlienSpeed(gamePanel.getDifficulty(), gamePanel.getGameMode(), gamePanel.getCurrentLevel());
        if (!gamePanel.isBossMusicPlayed()) {
            gamePanel.setBossMusicPlayed(true);
            SoundPlayer.playBoss();
        }
    }

    public void spawnStageAliens(int count, int currentLevel) {
        for (int i = 0; i < count; i++) {
            aliens.add(new Alien(20 + random.nextInt(GamePanel.WIDTH - 40 - 36), 70 + random.nextInt(180)));
        }
        gamePanel.setAlienSpeed(gamePanel.getDifficulty(), GamePanel.MODE_STAGE, currentLevel);
    }

    private void drawAliens(Graphics2D g2) {
        for (Alien alien : aliens) {
            if (alien.boss) {
                g2.setColor(new Color(255, 120, 80));
                g2.fillRoundRect((int) alien.x, (int) alien.y, 36, 24, 8, 8);
                g2.setColor(new Color(200, 50, 50));
                g2.drawRoundRect((int) alien.x, (int) alien.y, 36, 24, 8, 8);
                int barWidth = 36;
                int barHeight = 6;
                int barX = (int) alien.x;
                int barY = (int) alien.y - 12;
                g2.setColor(Color.DARK_GRAY);
                g2.fillRect(barX, barY, barWidth, barHeight);
                g2.setColor(new Color(255, 80, 80));
                int healthWidth = Math.max(0, (int) ((alien.health / 20.0) * barWidth));
                g2.fillRect(barX, barY, healthWidth, barHeight);
            } else {
                g2.setColor(new Color(150, 255, 80));
                g2.fillRoundRect((int) alien.x, (int) alien.y, 36, 24, 6, 6);
                g2.setColor(new Color(30, 80, 20));
                g2.fillOval((int) alien.x + 8, (int) alien.y + 7, 6, 6);
                g2.fillOval((int) alien.x + 22, (int) alien.y + 7, 6, 6);
            }
        }
    }

    private void drawEnemyBullets(Graphics2D g2) {
        g2.setColor(new Color(255, 120, 80));
        for (Bullet bullet : enemyBullets) {
            g2.fillRect(bullet.x, bullet.y, 4, 12);
        }
    }

    private void drawPowerUps(Graphics2D g2) {
        for (PowerUp powerUp : powerUps) {
            powerUp.draw(g2);
        }
    }

    public boolean areAliensEmpty() {
        return aliens.isEmpty();
    }

    public boolean isBossSpawned() {
        return bossSpawned;
    }
    
    public List<Alien> getAliens() {
        return aliens;
    }
}
