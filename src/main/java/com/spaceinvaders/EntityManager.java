package com.spaceinvaders;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class EntityManager {
    private static final int SPAWN_DELAY_FRAMES = 180;
    private static final int SHIELDED_SPAWN_INTERVAL_FRAMES = 300;

    private static final int DODGING_WAVE_NONE = -1;
    private static final int DODGING_WAVE_GREEN = 0;
    private static final int DODGING_WAVE_BLUE = 1;
    private static final int DODGING_WAVE_BOSS = 2;
    private static final double DODGING_BLUE_RUSH_SPEED = 5.5;

    private final List<Alien> aliens = new ArrayList<>();
    private final List<Bullet> enemyBullets = new ArrayList<>();
    private final List<PowerUp> powerUps = new ArrayList<>();
    private final Random random = new Random();
    private final GamePanel gamePanel;

    private int spawnDelayTimer;
    private int shieldedSpawnTimer;
    private int pendingClassicRows;
    private int pendingStageSpawnCount;
    private int pendingStageLevel;
    private int pendingDodgingSpawnCount;
    private boolean pendingBossSpawn;
    private boolean pendingDodgingBossSpawn;

    private int alienShootTimer;
    private int bossAttackTimer;
    private int bossSkillTimer;
    private int bossChargeTimer;
    private boolean bossCharging;
    private boolean bossSpawned;
    private boolean dodgingBossSpawned;

    private int dodgingWaveStep;
    private int dodgingCycle;
    private int pendingDodgingWaveType = DODGING_WAVE_NONE;
    private int pendingDodgingWaveCycle;
    private boolean dodgingWaveInProgress;

    public EntityManager(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void update(Player player1, Player player2, boolean twoPlayer) {
        updateSpawnDelay();
        updateShieldedSpawning();
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
        spawnDelayTimer = 0;
        shieldedSpawnTimer = 0;
        pendingClassicRows = 0;
        pendingStageSpawnCount = 0;
        pendingStageLevel = 0;
        pendingDodgingSpawnCount = 0;
        pendingBossSpawn = false;
        pendingDodgingBossSpawn = false;
        alienShootTimer = 0;
        bossAttackTimer = 0;
        bossSkillTimer = 0;
        bossChargeTimer = 0;
        bossCharging = false;
        bossSpawned = false;
        dodgingBossSpawned = false;
        dodgingWaveStep = DODGING_WAVE_GREEN;
        dodgingCycle = 1;
        pendingDodgingWaveType = DODGING_WAVE_NONE;
        pendingDodgingWaveCycle = 1;
        dodgingWaveInProgress = false;
    }

    public void initAliens(int gameMode, int currentLevel, int difficulty) {
        reset();
        int rows = Math.min(4 + currentLevel - 1, 7);

        if (gameMode == GamePanel.MODE_STAGE) {
            gamePanel.setRemainingStageAliens(20);
            pendingStageSpawnCount = Math.min(gamePanel.getStageSpawnBatch(), gamePanel.getRemainingStageAliens());
            pendingStageLevel = currentLevel;
            spawnDelayTimer = SPAWN_DELAY_FRAMES;
            gamePanel.setStageNextSpawnCountdown(SPAWN_DELAY_FRAMES);
        } else if (gameMode == GamePanel.MODE_DODGING) {
            gamePanel.setRemainingDodgingAliens(0);
            gamePanel.setDodgingWaveCount(0);
            dodgingWaveStep = DODGING_WAVE_GREEN;
            dodgingCycle = 1;
            queueNextDodgingWave();
        } else {
            // Classic 模式不能延遲生成，否則 GamePanel 會看到 aliens 為空，
            // 馬上判定遊戲結束。Classic 進場時直接生成第一波敵人。
            spawnClassicWave(rows);
        }

        gamePanel.setAlienSpeed(difficulty, gameMode, currentLevel);
    }

    private void updateSpawnDelay() {
        if (spawnDelayTimer <= 0) {
            return;
        }

        spawnDelayTimer--;
        if (spawnDelayTimer > 0) {
            return;
        }

        if (pendingClassicRows > 0) {
            spawnClassicWave(pendingClassicRows);
            pendingClassicRows = 0;
        }

        if (pendingStageSpawnCount > 0) {
            spawnStageAliens(pendingStageSpawnCount, pendingStageLevel);
            gamePanel.setRemainingStageAliens(gamePanel.getRemainingStageAliens() - pendingStageSpawnCount);
            pendingStageSpawnCount = 0;
            pendingStageLevel = 0;
            gamePanel.setStageNextSpawnCountdown(120);
        }

        if (pendingDodgingWaveType != DODGING_WAVE_NONE) {
            spawnDodgingWaveNow(pendingDodgingWaveType, pendingDodgingWaveCycle);
            pendingDodgingWaveType = DODGING_WAVE_NONE;
            pendingDodgingWaveCycle = 1;
            gamePanel.setDodgingNextSpawnCountdown(60);
        }

        if (pendingBossSpawn) {
            spawnBossNow();
            pendingBossSpawn = false;
        }

        if (pendingDodgingBossSpawn) {
            spawnDodgingBossNow();
            pendingDodgingBossSpawn = false;
        }
    }

    private void spawnClassicWave(int rows) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 10; col++) {
                int x = 80 + col * (Alien.WIDTH + 18);
                int y = 70 + row * (Alien.HEIGHT + 14);
                aliens.add(new Alien(x, y));
            }
        }
    }

    private void updateAliens() {
        if (aliens.isEmpty()) {
            return;
        }

        double dropSpeed = 0.18;
        if (gamePanel.getGameMode() != GamePanel.MODE_CLASSIC) {
            dropSpeed *= gamePanel.getDifficultyMultiplier();
        }

        double maxBossAmplitude = (GamePanel.HEIGHT / 2.0) - Alien.HEIGHT - 10;
        for (Alien alien : aliens) {
            if (alien.boss) {
                alien.updateBossMovement(gamePanel.getAlienSpeed(), random, 8, GamePanel.WIDTH - 8 - Alien.WIDTH, maxBossAmplitude);
                double maxBossY = (GamePanel.HEIGHT / 2.0) - Alien.HEIGHT;
                double minBossY = 40;
                if (alien.y > maxBossY) {
                    alien.y = maxBossY;
                } else if (alien.y < minBossY) {
                    alien.y = minBossY;
                }
            } else if (gamePanel.getGameMode() == GamePanel.MODE_DODGING && alien.blue) {
                // Dodging 模式的藍色敵人：像 Stage 模式的俯衝敵人一樣，從上方往下衝
                alien.move(0, DODGING_BLUE_RUSH_SPEED * gamePanel.getDifficultyMultiplier());
            } else if (alien.shielded) {
                alien.updateShieldedMovement();
            } else {
                alien.updateMovement(gamePanel.getAlienSpeed(), random, 8, GamePanel.WIDTH - 8 - Alien.WIDTH, dropSpeed);
            }
        }

        aliens.removeIf(alien -> !alien.boss && (
                alien.x + Alien.WIDTH < 0
                        || alien.x > GamePanel.WIDTH
                        || alien.y + Alien.HEIGHT >= GamePanel.HEIGHT
                        || (!alien.blue && alien.y + Alien.HEIGHT < 0)
        ));
    }

    private void updateEnemyBullets() {
        enemyBullets.removeIf(bullet -> {
            bullet.update();
            return bullet.y > GamePanel.HEIGHT || bullet.y + Bullet.HEIGHT < 0 || bullet.x + Bullet.WIDTH < 0 || bullet.x > GamePanel.WIDTH;
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
        if (gameMode == GamePanel.MODE_DODGING) {
            return; // Dodging 模式改成清完一波才固定掉 1 個道具
        }

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
        if (gamePanel.getGameMode() == GamePanel.MODE_CLASSIC || aliens.isEmpty() || spawnDelayTimer > 0) {
            return;
        }

        if (hasBossAlien()) {
            updateBossShooting();
            return;
        }

        updateGreenAlienShooting();
    }

    private void updateGreenAlienShooting() {
        int enemyBulletCap = getEnemyBulletCap();

        for (Alien alien : aliens) {
            if (!alien.isGreenShooter()) {
                continue;
            }
            if (alien.greenShootIntervalFrames <= 0) {
                alien.initGreenShooting(random);
            }
            if (alien.updateGreenShootTimer() && enemyBullets.size() < enemyBulletCap) {
                fireGreenAlienShot(alien);
            }
        }
    }

    private int getEnemyBulletCap() {
        if (gamePanel.getGameMode() == GamePanel.MODE_DODGING) {
            return 80;
        }
        return Math.min(20 + gamePanel.getCurrentLevel() * 4, 40);
    }

    private void fireGreenAlienShot(Alien shooter) {
        int startX = (int) shooter.x + Alien.WIDTH / 2 - Bullet.WIDTH / 2;
        int startY = (int) shooter.y + Alien.HEIGHT;
        enemyBullets.add(new Bullet(startX, startY, 0, 6, true));
    }

    private void updateBossShooting() {
        List<Alien> bossAliens = getBossAliens();
        if (bossAliens.isEmpty()) {
            return;
        }

        if (bossSkillTimer > 0) {
            bossSkillTimer--;
        }
        if (bossAttackTimer > 0) {
            bossAttackTimer--;
        }

        if (bossSkillTimer <= 60 && bossSkillTimer > 0) {
            if (!bossCharging) {
                bossCharging = true;
                bossChargeTimer = 30 + random.nextInt(31);
            }
            if (bossChargeTimer > 0) {
                bossChargeTimer--;
            }
            if (bossChargeTimer <= 0) {
                for (Alien bossAlien : bossAliens) {
                    fireBossVolley(bossAlien, getBossTargetPlayer(bossAlien));
                }
                bossSkillTimer = 600;
                bossCharging = false;
            }
        } else {
            bossCharging = false;
        }

        if (bossAttackTimer <= 0) {
            for (Alien bossAlien : bossAliens) {
                fireBossDownShot(bossAlien);
            }
            bossAttackTimer = 16;
        }
    }

    private Player getBossTargetPlayer(Alien bossAlien) {
        Player player1 = gamePanel.getPlayer1();
        Player player2 = gamePanel.isTwoPlayer() ? gamePanel.getPlayer2() : null;

        if (player2 == null || !player2.isAlive()) {
            return player1;
        }
        if (!player1.isAlive()) {
            return player2;
        }

        double bossCenterX = bossAlien.x + Alien.WIDTH / 2.0;
        double player1CenterX = player1.getX() + player1.getWidth() / 2.0;
        double player2CenterX = player2.getX() + player2.getWidth() / 2.0;
        double distance1 = Math.abs(player1CenterX - bossCenterX);
        double distance2 = Math.abs(player2CenterX - bossCenterX);
        return distance1 <= distance2 ? player1 : player2;
    }

    private void fireBossVolley(Alien bossAlien, Player targetPlayer) {
        int[] angleOffsets = {-24, -10, 10, 24};
        for (int angleOffset : angleOffsets) {
            fireBossAimedShotFan(bossAlien, targetPlayer, 8, angleOffset);
        }
    }

    private void fireBossDownShot(Alien bossAlien) {
        int startX = (int) bossAlien.x + Alien.WIDTH / 2 - Bullet.WIDTH / 2;
        int startY = (int) bossAlien.y + Alien.HEIGHT - 2;
        enemyBullets.add(new Bullet(startX, startY, 0, 9, true));
    }

    private void fireBossAimedShotFan(Alien bossAlien, Player targetPlayer, int speed, int angleOffsetDegrees) {
        int startX = (int) bossAlien.x + Alien.WIDTH / 2 - Bullet.WIDTH / 2;
        int startY = (int) bossAlien.y + Alien.HEIGHT - 2;

        double targetX = targetPlayer.getX() + targetPlayer.getWidth() / 2.0;
        double targetY = targetPlayer.getY() + targetPlayer.getHeight() / 2.0;
        double originX = startX + Bullet.WIDTH / 2.0;
        double originY = startY + Bullet.HEIGHT / 2.0;

        double baseDx = targetX - originX;
        double baseDy = targetY - originY;
        double baseLength = Math.sqrt(baseDx * baseDx + baseDy * baseDy);
        if (baseLength == 0) {
            baseLength = 1;
        }

        double normX = baseDx / baseLength;
        double normY = baseDy / baseLength;
        double radians = Math.toRadians(angleOffsetDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        double rotatedX = normX * cos - normY * sin;
        double rotatedY = normX * sin + normY * cos;

        int dx = (int) Math.round(rotatedX * speed);
        int dy = (int) Math.round(rotatedY * speed);
        if (dy <= 0) {
            dy = speed;
        }
        if (dx == 0 && Math.abs(angleOffsetDegrees) > 0) {
            dx = angleOffsetDegrees > 0 ? 1 : -1;
        }

        enemyBullets.add(new Bullet(startX, startY, dx, dy, true));
    }

    private Alien getBossAlien() {
        for (Alien alien : aliens) {
            if (alien.boss) {
                return alien;
            }
        }
        return null;
    }

    private List<Alien> getBossAliens() {
        List<Alien> bossAliens = new ArrayList<>();
        for (Alien alien : aliens) {
            if (alien.boss) {
                bossAliens.add(alien);
            }
        }
        return bossAliens;
    }

    private boolean hasBossAlien() {
        return getBossAlien() != null;
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
                            SoundPlayer.playExplosion();
                            if (!hasBossAlien()) {
                                if (gamePanel.getGameMode() == GamePanel.MODE_STAGE || gamePanel.getGameMode() == GamePanel.MODE_DODGING) {
                                    gamePanel.setBossMusicPlayed(false);
                                    SoundPlayer.playBattle();
                                } else {
                                    SoundPlayer.stopBackgroundMusic();
                                }
                                bossAttackTimer = 0;
                                bossSkillTimer = 0;
                                bossChargeTimer = 0;
                                bossCharging = false;
                            }
                        }
                    } else if (alien.shielded) {
                        if (alien.shieldedWait <= 0) {
                            return;
                        }
                        alien.health--;
                        if (alien.health <= 0) {
                            alienIterator.remove();
                            SoundPlayer.playExplosion();
                        } else {
                            SoundPlayer.playHit();
                        }
                    } else if (alien.blue) {
                        alien.health--;
                        if (alien.health <= 0) {
                            alienIterator.remove();
                            gamePanel.addScore(20);
                            SoundPlayer.playExplosion();
                        } else {
                            SoundPlayer.playHit();
                        }
                    } else {
                        alienIterator.remove();
                        gamePanel.addScore(10);
                        SoundPlayer.playExplosion();
                    }
                    return;
                }
            }
        }
    }

    private void checkEnemyBulletHitsPlayer(Player player) {
        if (!player.isAlive() || player.isInvincible()) {
            return;
        }
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
        if (!player.isAlive() || player.isInvincible()) {
            return;
        }
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
            if (spawnDelayTimer > 0 || pendingStageSpawnCount > 0 || pendingBossSpawn) {
                return;
            }

            int spawnBatch = Math.min(gamePanel.getStageSpawnBatch() + gamePanel.getCurrentLevel() - 1, gamePanel.getRemainingStageAliens());
            int spawnInterval = Math.max(35, 120 - (gamePanel.getCurrentLevel() - 1) * 15);

            if (aliens.isEmpty() && gamePanel.getRemainingStageAliens() > 0) {
                pendingStageSpawnCount = Math.min(spawnBatch, gamePanel.getRemainingStageAliens());
                pendingStageLevel = gamePanel.getCurrentLevel();
                spawnDelayTimer = SPAWN_DELAY_FRAMES;
                gamePanel.setStageNextSpawnCountdown(SPAWN_DELAY_FRAMES);
                return;
            }

            if (gamePanel.getRemainingStageAliens() > 0) {
                gamePanel.decrementStageNextSpawnCountdown();
                if (gamePanel.getStageNextSpawnCountdown() <= 0) {
                    pendingStageSpawnCount = Math.min(spawnBatch, gamePanel.getRemainingStageAliens());
                    pendingStageLevel = gamePanel.getCurrentLevel();
                    spawnDelayTimer = SPAWN_DELAY_FRAMES;
                    gamePanel.setStageNextSpawnCountdown(SPAWN_DELAY_FRAMES);
                }
            }
        } else if (gameMode == GamePanel.MODE_DODGING) {
            updateDodgingSpawning();
        }
    }

    private void updateDodgingSpawning() {
        if (spawnDelayTimer > 0 || pendingDodgingWaveType != DODGING_WAVE_NONE) {
            return;
        }

        if (dodgingWaveInProgress && aliens.isEmpty()) {
            dodgingWaveInProgress = false;
            dropDodgingReward();
            advanceDodgingWave();
            queueNextDodgingWave();
        }
    }

    private void queueNextDodgingWave() {
        pendingDodgingWaveType = dodgingWaveStep;
        pendingDodgingWaveCycle = dodgingCycle;
        spawnDelayTimer = SPAWN_DELAY_FRAMES;
        gamePanel.setDodgingNextSpawnCountdown(SPAWN_DELAY_FRAMES);
    }

    private void advanceDodgingWave() {
        dodgingWaveStep++;
        if (dodgingWaveStep > DODGING_WAVE_BOSS) {
            dodgingWaveStep = DODGING_WAVE_GREEN;
            dodgingCycle++;
        }
    }

    private void spawnDodgingWaveNow(int waveType, int cycle) {
        int count;
        if (waveType == DODGING_WAVE_GREEN) {
            count = 3 * cycle;
            spawnDodgingNormalAliens(count, false);
        } else if (waveType == DODGING_WAVE_BLUE) {
            count = 2 * cycle;
            spawnDodgingNormalAliens(count, true);
        } else {
            count = Math.max(2, cycle);
            spawnDodgingBossWave(count);
        }

        dodgingWaveInProgress = true;
        gamePanel.setRemainingDodgingAliens(count);
        gamePanel.setDodgingWaveCount(gamePanel.getDodgingWaveCount() + 1);
        gamePanel.setAlienSpeed(gamePanel.getDifficulty(), gamePanel.getGameMode(), gamePanel.getCurrentLevel());
    }

    private void spawnDodgingNormalAliens(int count, boolean blue) {
        dodgingBossSpawned = false;
        for (int i = 0; i < count; i++) {
            int x = 20 + random.nextInt(GamePanel.WIDTH - 40 - Alien.WIDTH);
            int y;
            int health;

            if (blue) {
                // 藍色敵人從畫面上方外側出現，依序往下衝進場
                y = -Alien.HEIGHT - random.nextInt(180) - i * 18;
                health = 2;
                Alien blueAlien = new Alien(x, y, health, false, false, 0, true);
                blueAlien.dx = 0;
                aliens.add(blueAlien);
            } else {
                // 綠色敵人維持原本 Dodging 模式的一般移動，並在生成時決定自己的固定射擊週期
                y = 70 + random.nextInt(180);
                health = 1;
                Alien greenAlien = new Alien(x, y, health, false, false, 0, false);
                greenAlien.initGreenShooting(random);
                aliens.add(greenAlien);
            }
        }
    }

    private void spawnDodgingBossWave(int count) {
        dodgingBossSpawned = true;
        bossAttackTimer = 0;
        bossSkillTimer = 600;
        bossChargeTimer = 0;
        bossCharging = false;

        int left = 50;
        int right = GamePanel.WIDTH - 50 - Alien.WIDTH;
        for (int i = 0; i < count; i++) {
            int x = (count == 1)
                    ? (GamePanel.WIDTH - Alien.WIDTH) / 2
                    : left + (right - left) * i / (count - 1);
            int y = 70 + (i % 2) * 40;
            Alien bossAlien = new Alien(x, y, 20, true);
            bossAlien.initBossMovement(random, (GamePanel.HEIGHT / 2.0) - Alien.HEIGHT - 10);
            aliens.add(bossAlien);
        }

        if (!gamePanel.isBossMusicPlayed()) {
            gamePanel.setBossMusicPlayed(true);
            SoundPlayer.playBoss();
        }
    }

    private void dropDodgingReward() {
        int x = 20 + random.nextInt(GamePanel.WIDTH - 40 - 24);
        int y = 80;
        int type = random.nextInt(3);
        powerUps.add(new PowerUp(x, y, type, 24, 2));
    }

    public void spawnBoss() {
        pendingBossSpawn = true;
        spawnDelayTimer = SPAWN_DELAY_FRAMES;
        gamePanel.setStageNextSpawnCountdown(SPAWN_DELAY_FRAMES);
    }

    private void spawnBossNow() {
        bossSpawned = true;
        Alien bossAlien = new Alien((GamePanel.WIDTH - Alien.WIDTH) / 2, 70, 20, true);
        bossAlien.initBossMovement(random, (GamePanel.HEIGHT / 2.0) - Alien.HEIGHT - 10);
        aliens.add(bossAlien);
        bossAttackTimer = 0;
        bossSkillTimer = 600;
        bossChargeTimer = 0;
        bossCharging = false;
        gamePanel.setAlienSpeed(gamePanel.getDifficulty(), gamePanel.getGameMode(), gamePanel.getCurrentLevel());
        if (!gamePanel.isBossMusicPlayed()) {
            gamePanel.setBossMusicPlayed(true);
            SoundPlayer.playBoss();
        }
    }

    private void spawnDodgingBossNow() {
        dodgingBossSpawned = true;
        gamePanel.setRemainingDodgingAliens(0);
        Alien bossAlien = new Alien((GamePanel.WIDTH - Alien.WIDTH) / 2, 70, 20, true);
        bossAlien.initBossMovement(random, (GamePanel.HEIGHT / 2.0) - Alien.HEIGHT - 10);
        aliens.add(bossAlien);
        bossAttackTimer = 0;
        bossSkillTimer = 600;
        bossChargeTimer = 0;
        bossCharging = false;
        gamePanel.setAlienSpeed(gamePanel.getDifficulty(), gamePanel.getGameMode(), gamePanel.getCurrentLevel());
        if (!gamePanel.isBossMusicPlayed()) {
            gamePanel.setBossMusicPlayed(true);
            SoundPlayer.playBoss();
        }
    }

    public void spawnStageAliens(int count, int currentLevel) {
        for (int i = 0; i < count; i++) {
            Alien greenAlien = new Alien(20 + random.nextInt(GamePanel.WIDTH - 40 - Alien.WIDTH), 70 + random.nextInt(180));
            greenAlien.initGreenShooting(random);
            aliens.add(greenAlien);
        }
        gamePanel.setAlienSpeed(gamePanel.getDifficulty(), GamePanel.MODE_STAGE, currentLevel);
    }

    private void updateShieldedSpawning() {
        if (gamePanel.getGameMode() == GamePanel.MODE_DODGING) {
            return; // Dodging 模式只使用固定波次，不再隨機插入護盾敵人
        }
        if (spawnDelayTimer > 0) {
            return;
        }
        if (getBossAlien() != null || pendingBossSpawn || pendingDodgingBossSpawn) {
            return;
        }
        if (gamePanel.getGameMode() == GamePanel.MODE_CLASSIC && aliens.isEmpty()) {
            return;
        }
        if (shieldedSpawnTimer > 0) {
            shieldedSpawnTimer--;
            return;
        }
        int spawnCount = pickShieldedSpawnCount();
        for (int i = 0; i < spawnCount; i++) {
            spawnShieldedAlien();
        }
        shieldedSpawnTimer = SHIELDED_SPAWN_INTERVAL_FRAMES;
    }

    private int pickShieldedSpawnCount() {
        int roll = random.nextInt(3);
        if (roll == 0) {
            return 1;
        }
        if (roll == 1) {
            return 3;
        }
        return 5;
    }

    private void spawnShieldedAlien() {
        int x = 20 + random.nextInt(GamePanel.WIDTH - 40 - Alien.WIDTH);
        int y = 20;
        aliens.add(new Alien(x, y, 2, false, true, getShieldedWaitFrames()));
    }

    private int getShieldedWaitFrames() {
        switch (gamePanel.getDifficulty()) {
            case 0:
                return 360;
            case 1:
                return 240;
            default:
                return 180;
        }
    }

    private void drawAliens(Graphics2D g2) {
        for (Alien alien : aliens) {
            if (alien.boss) {
                g2.setColor(new Color(255, 120, 80));
                g2.fillRoundRect((int) alien.x, (int) alien.y, Alien.WIDTH, Alien.HEIGHT, 8, 8);
                g2.setColor(new Color(200, 50, 50));
                g2.drawRoundRect((int) alien.x, (int) alien.y, Alien.WIDTH, Alien.HEIGHT, 8, 8);

                int barWidth = Alien.WIDTH;
                int barHeight = 6;
                int barX = (int) alien.x;
                int barY = (int) alien.y - 12;
                g2.setColor(Color.DARK_GRAY);
                g2.fillRect(barX, barY, barWidth, barHeight);
                g2.setColor(new Color(255, 80, 80));
                int healthWidth = Math.max(0, (int) ((alien.health / 20.0) * barWidth));
                g2.fillRect(barX, barY, healthWidth, barHeight);

                g2.setColor(new Color(120, 30, 30));
                int[] leftHornX = {(int) alien.x + 8, (int) alien.x + 14, (int) alien.x + 10};
                int[] leftHornY = {(int) alien.y + 2, (int) alien.y - 10, (int) alien.y + 4};
                int[] rightHornX = {(int) alien.x + Alien.WIDTH - 8, (int) alien.x + Alien.WIDTH - 14, (int) alien.x + Alien.WIDTH - 10};
                int[] rightHornY = {(int) alien.y + 2, (int) alien.y - 10, (int) alien.y + 4};
                g2.fillPolygon(leftHornX, leftHornY, 3);
                g2.fillPolygon(rightHornX, rightHornY, 3);

                g2.setColor(new Color(255, 180, 90, 120));
                g2.fillArc((int) alien.x - 12, (int) alien.y + 4, 10, 18, 90, 180);
                g2.fillArc((int) alien.x + Alien.WIDTH + 2, (int) alien.y + 4, 10, 18, 270, 180);

                if (bossCharging) {
                    g2.setColor(new Color(255, 240, 120));
                    g2.setFont(new Font("Consolas", Font.BOLD, 24));
                    g2.drawString("!", (int) alien.x + 15, (int) alien.y - 16);
                }
            } else if (alien.shielded) {
                g2.setColor(new Color(60, 140, 255));
                g2.fillRoundRect((int) alien.x, (int) alien.y, Alien.WIDTH, Alien.HEIGHT, 8, 8);
                g2.setColor(new Color(20, 60, 140));
                g2.drawRoundRect((int) alien.x, (int) alien.y, Alien.WIDTH, Alien.HEIGHT, 8, 8);

                g2.setColor(new Color(120, 200, 255, 160));
                g2.drawOval((int) alien.x - 6, (int) alien.y - 6, Alien.WIDTH + 12, Alien.HEIGHT + 12);
                g2.setColor(new Color(140, 220, 255, 200));
                g2.drawOval((int) alien.x - 2, (int) alien.y - 2, Alien.WIDTH + 4, Alien.HEIGHT + 4);

                g2.setColor(new Color(230, 245, 255));
                g2.fillOval((int) alien.x + 9, (int) alien.y + 7, 6, 6);
                g2.fillOval((int) alien.x + 21, (int) alien.y + 7, 6, 6);
            } else if (alien.blue) {
                g2.setColor(new Color(80, 170, 255));
                g2.fillRoundRect((int) alien.x, (int) alien.y, Alien.WIDTH, Alien.HEIGHT, 6, 6);
                g2.setColor(new Color(20, 70, 130));
                g2.fillOval((int) alien.x + 8, (int) alien.y + 7, 6, 6);
                g2.fillOval((int) alien.x + 22, (int) alien.y + 7, 6, 6);
            } else {
                g2.setColor(new Color(150, 255, 80));
                g2.fillRoundRect((int) alien.x, (int) alien.y, Alien.WIDTH, Alien.HEIGHT, 6, 6);
                g2.setColor(new Color(30, 80, 20));
                g2.fillOval((int) alien.x + 8, (int) alien.y + 7, 6, 6);
                g2.fillOval((int) alien.x + 22, (int) alien.y + 7, 6, 6);
            }
        }
    }

    private void drawEnemyBullets(Graphics2D g2) {
        g2.setColor(new Color(255, 120, 80));
        for (Bullet bullet : enemyBullets) {
            g2.fillRect(bullet.x, bullet.y, Bullet.WIDTH, Bullet.HEIGHT);
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

    public boolean isBossSpawnPending() {
        return pendingBossSpawn || pendingDodgingBossSpawn || spawnDelayTimer > 0;
    }

    public List<Alien> getAliens() {
        return aliens;
    }
}
