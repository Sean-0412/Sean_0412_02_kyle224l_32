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

    private final List<Alien> aliens = new ArrayList<>();
    private final List<Bullet> enemyBullets = new ArrayList<>();
    private final List<PowerUp> powerUps = new ArrayList<>();
    private final Random random = new Random();
    private final GamePanel gamePanel;

    private int spawnDelayTimer;
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

    public EntityManager(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void update(Player player1, Player player2, boolean twoPlayer) {
        updateSpawnDelay();
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
            gamePanel.setRemainingDodgingAliens(50);
            gamePanel.setDodgingWaveCount(0);
            pendingDodgingSpawnCount = 1;
            spawnDelayTimer = SPAWN_DELAY_FRAMES;
            gamePanel.setDodgingNextSpawnCountdown(SPAWN_DELAY_FRAMES);
        } else {
            pendingClassicRows = rows;
            spawnDelayTimer = SPAWN_DELAY_FRAMES;
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

        if (pendingDodgingSpawnCount > 0) {
            spawnDodgingBossNow();
            pendingDodgingSpawnCount = 0;
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
            } else {
                alien.updateMovement(gamePanel.getAlienSpeed(), random, 8, GamePanel.WIDTH - 8 - Alien.WIDTH, dropSpeed);
            }
        }

        aliens.removeIf(alien -> !alien.boss && (alien.x + Alien.WIDTH < 0 || alien.x > GamePanel.WIDTH || alien.y + Alien.HEIGHT >= GamePanel.HEIGHT || alien.y + Alien.HEIGHT < 0));
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

        Alien bossAlien = getBossAlien();
        if (bossAlien != null) {
            updateBossShooting(bossAlien);
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
            enemyBullets.add(new Bullet((int) shooter.x + Alien.WIDTH / 2 - Bullet.WIDTH / 2, (int) shooter.y + Alien.HEIGHT, 0, 6, true));
        }

        int baseMinInterval = Math.max(18, 60 - (gamePanel.getCurrentLevel() - 1) * 4);
        int baseMaxInterval = Math.max(30, 120 - (gamePanel.getCurrentLevel() - 1) * 6);
        int minInterval = scaleInterval(baseMinInterval, rateMultiplier);
        int maxInterval = scaleInterval(baseMaxInterval, rateMultiplier);
        alienShootTimer = minInterval + random.nextInt(maxInterval - minInterval + 1);
    }

    private void updateBossShooting(Alien bossAlien) {
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
                fireBossVolley(bossAlien, getBossTargetPlayer());
                bossSkillTimer = 600;
                bossCharging = false;
            }
        } else {
            bossCharging = false;
        }

        if (bossAttackTimer <= 0) {
            fireBossDownShot(bossAlien);
            bossAttackTimer = 16;
        }
    }

    private Player getBossTargetPlayer() {
        Player player1 = gamePanel.getPlayer1();
        Player player2 = gamePanel.isTwoPlayer() ? gamePanel.getPlayer2() : null;

        if (player2 == null || !player2.isAlive()) {
            return player1;
        }
        if (!player1.isAlive()) {
            return player2;
        }

        Alien bossAlien = getBossAlien();
        if (bossAlien == null) {
            return player1;
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
                            if (gamePanel.getGameMode() == GamePanel.MODE_STAGE) {
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
        if (!player.isAlive()) {
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
        if (!player.isAlive()) {
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
        if (spawnDelayTimer > 0 || pendingDodgingSpawnCount > 0 || pendingDodgingBossSpawn) {
            return;
        }

        if (dodgingBossSpawned && aliens.isEmpty()) {
            dodgingBossSpawned = false;
            gamePanel.setRemainingDodgingAliens(50);
            gamePanel.setDodgingWaveCount(0);
            pendingDodgingSpawnCount = 1;
            spawnDelayTimer = SPAWN_DELAY_FRAMES;
            gamePanel.setDodgingNextSpawnCountdown(SPAWN_DELAY_FRAMES);
            return;
        }

        if (gamePanel.getRemainingDodgingAliens() > 0 && aliens.isEmpty()) {
            pendingDodgingSpawnCount = 1;
            spawnDelayTimer = SPAWN_DELAY_FRAMES;
            gamePanel.setDodgingNextSpawnCountdown(SPAWN_DELAY_FRAMES);
            return;
        }

        if (!dodgingBossSpawned && gamePanel.getDodgingWaveCount() >= (6000 / 60)) {
            pendingDodgingBossSpawn = true;
            spawnDelayTimer = SPAWN_DELAY_FRAMES;
            gamePanel.setDodgingNextSpawnCountdown(SPAWN_DELAY_FRAMES);
        }
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
            aliens.add(new Alien(20 + random.nextInt(GamePanel.WIDTH - 40 - Alien.WIDTH), 70 + random.nextInt(180)));
        }
        gamePanel.setAlienSpeed(gamePanel.getDifficulty(), GamePanel.MODE_STAGE, currentLevel);
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
