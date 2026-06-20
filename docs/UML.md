# UML 類別圖

本專題使用 Java Swing 製作 Space Invaders 遊戲。主要類別可分為啟動入口、視窗與 GUI、遊戲邏輯、遊戲物件、音效與排行榜等部分。

```mermaid
classDiagram
    class SpaceInvadersGame {
        +main(String[] args) void
    }

    class GameFrame {
        -MenuPanel menuPanel
        -GamePanel gamePanel
        -Leaderboard leaderboard
        -int lastGameMode
        +GameFrame()
        +startGameWithSettings(int gameMode, int difficulty, boolean twoPlayer) void
        +returnToDifficultyMenu() void
        +returnToMainMenu() void
        +getLeaderboard() Leaderboard
    }

    class MenuPanel {
        +MenuPanel(GameFrame frame)
        +setInitialStateToDifficultyMenu(int gameMode) void
    }

    class GamePanel {
        +WIDTH int
        +HEIGHT int
        +MODE_CLASSIC int
        +MODE_DODGING int
        +MODE_STAGE int
        -GameFrame gameFrame
        -Player player1
        -Player player2
        -EntityManager entityManager
        -GameRenderer gameRenderer
        +GamePanel(GameFrame gameFrame, int gameMode, int difficulty, boolean twoPlayer)
        +startGame() void
        +actionPerformed(ActionEvent e) void
        +keyPressed(KeyEvent e) void
        +keyReleased(KeyEvent e) void
        +addScore(int points) void
        +getScore() int
        +getPlayer1() Player
        +getPlayer2() Player
        +getEntityManager() EntityManager
    }

    class GameRenderer {
        -GamePanel gamePanel
        -GameUI gameUI
        -EntityManager entityManager
        +GameRenderer(GamePanel gamePanel, EntityManager entityManager)
        +paint(Graphics2D g2) void
    }

    class GameUI {
        -GamePanel gamePanel
        +GameUI(GamePanel gamePanel)
        +draw(Graphics2D g2) void
    }

    class EntityManager {
        +initAliens(int gameMode, int currentLevel, int difficulty) void
        +update(Player player1, Player player2, boolean twoPlayer) void
        +draw(Graphics2D g2) void
        +spawnBoss() void
        +getAliens() List~Alien~
    }

    class Player {
        -int x
        -int y
        -int lives
        -List~Bullet~ bullets
        +Player(int startX, int startY, int gameMode, boolean isPlayer1, int startLives)
        +update() void
        +draw(Graphics2D g2) void
        +handleHit() void
        +applyPowerUp(int type) void
        +activateUltimate() void
        +reset() void
        +getBullets() List~Bullet~
        +getBounds() Rectangle
    }

    class Alien {
        +double x
        +double y
        +int health
        +boolean boss
        +boolean shielded
        +boolean blue
        +updateMovement(double speed, Random random, int minX, int maxX, double dropSpeed) void
        +updateBossMovement(double speed, Random random, int minX, int maxX, double maxAmplitude) void
        +draw(Graphics g) void
        +getBounds() Rectangle2D.Double
    }

    class Bullet {
        +int x
        +int y
        +int dx
        +int dy
        +boolean enemy
        +update() void
        +draw(Graphics g) void
        +getBounds() Rectangle
    }

    class PowerUp {
        +draw(Graphics2D g2) void
        +getBounds() Rectangle
    }

    class Leaderboard {
        +addScore(int score, boolean twoPlayer) void
    }

    class SoundPlayer {
        +playShoot() void
        +playHit() void
        +playExplosion() void
        +playGameOver() void
        +playBattle() void
        +playBoss() void
        +playMenu() void
        +playDefeat() void
        +stopAllSounds() void
        +stopBackgroundMusic() void
    }

    SpaceInvadersGame --> GameFrame
    GameFrame *-- MenuPanel
    GameFrame *-- GamePanel
    GameFrame *-- Leaderboard
    MenuPanel --> GameFrame
    GamePanel *-- Player
    GamePanel *-- EntityManager
    GamePanel *-- GameRenderer
    GameRenderer *-- GameUI
    GameRenderer --> EntityManager
    EntityManager *-- Alien
    EntityManager *-- PowerUp
    EntityManager --> Bullet
    Player *-- Bullet
    Player --> SoundPlayer
    EntityManager --> SoundPlayer
    GameFrame --> SoundPlayer