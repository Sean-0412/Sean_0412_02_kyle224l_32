# Space Invaders Project Overview

## UML Class Diagram

```mermaid
classDiagram
    class SpaceInvadersGame {
        +main(String[])
    }

    class GameFrame {
        -MenuPanel menuPanel
        -GamePanel gamePanel
        -Leaderboard leaderboard
        -int lastGameMode
        +GameFrame()
        +startGameWithSettings(int, int, boolean)
        +returnToDifficultyMenu()
        +returnToMainMenu()
        +getLeaderboard() Leaderboard
    }

    class MenuPanel {
        -GameFrame gameFrame
        -int currentState
        -int selectedOption
        +MenuPanel(GameFrame)
        +setInitialStateToDifficultyMenu(int)
    }

    class GamePanel {
        -GameFrame gameFrame
        -Player player1
        -Player player2
        -EntityManager entityManager
        -GameRenderer gameRenderer
        +GamePanel(GameFrame, int, int, boolean)
        +startGame()
        +setAlienSpeed(int, int, int)
        +getDifficultyMultiplier() double
    }

    class GameRenderer {
        -GamePanel gamePanel
        -GameUI gameUI
        -EntityManager entityManager
        +paint(Graphics2D)
    }

    class GameUI {
        -GamePanel gamePanel
        +draw(Graphics2D)
    }

    class EntityManager {
        -GamePanel gamePanel
        -List~Alien~ aliens
        -List~Bullet~ enemyBullets
        -List~PowerUp~ powerUps
        +update(Player, Player, boolean)
        +draw(Graphics2D)
        +initAliens(int, int, int)
        +spawnPowerUps(int, int)
        +getAliens() List~Alien~
    }

    class Shooter {
        +x
        +y
        +move()
        +draw(Graphics)
        +getBounds() Rectangle
    }

    class Player {
        -List~Bullet~ bullets
        +Player(int, int, int, boolean, int)
        +update()
        +draw(Graphics2D)
        +handleHit()
        +applyPowerUp(int)
        +activateUltimate()
        +reset()
    }

    class Alien {
        +Alien(int, int)
        +updateMovement(double, Random, int, int, double)
        +updateBossMovement(double, Random, int, int, double)
        +updateShieldedMovement()
        +draw(Graphics)
    }

    class Bullet {
        +Bullet(int, int)
        +Bullet(int, int, int, boolean)
        +Bullet(int, int, int, int, boolean)
        +update()
        +draw(Graphics)
    }

    class PowerUp {
        +PowerUp(int, int, int, int, int)
        +update()
        +draw(Graphics)
    }

    class Leaderboard {
        +Leaderboard()
        +addScore(int)
        +addScore(int, boolean)
        +getEntries() List~Entry~
        +getEntries(boolean) List~Entry~
    }

    class SoundPlayer {
        +playMenu()
        +playBattle()
        +playBoss()
        +playShoot()
        +playExplosion()
        +playGameOver()
        +playDefeat()
        +stopBackgroundMusic()
        +stopAllSounds()
    }

    SpaceInvadersGame --> GameFrame
    GameFrame *-- MenuPanel
    GameFrame *-- GamePanel
    GameFrame o-- Leaderboard
    GamePanel *-- Player
    GamePanel *-- EntityManager
    GamePanel *-- GameRenderer
    GameRenderer *-- GameUI
    GameRenderer ..> EntityManager
    GameRenderer ..> Player
    EntityManager *-- Alien
    EntityManager *-- Bullet
    EntityManager *-- PowerUp
    Player ..|> Shooter
    Alien ..|> Shooter
    MenuPanel ..> GameFrame
    GamePanel ..> GameFrame
    GamePanel ..> SoundPlayer
    MenuPanel ..> SoundPlayer
```

## GUI Code Layout

The GUI is split into a few focused Swing classes instead of one large panel.

- `GameFrame` is the top-level `JFrame` and switches between menu and game screens.
- `MenuPanel` owns all menu states, key navigation, and screen drawing for start, controls, about, and leaderboard views.
- `GamePanel` owns the game loop, input handling, state updates, and the `Timer` that drives repainting.
- `GameRenderer` centralizes in-game drawing so `GamePanel.paintComponent` stays small.
- `GameUI` draws HUD elements, pause overlay, and game-over overlay.
- `EntityManager` manages aliens, bullets, power-ups, spawning, and collision-related updates.

Flow summary:

1. `SpaceInvadersGame.main` creates `GameFrame` on the EDT.
2. `GameFrame` starts with `MenuPanel` visible.
3. Menu actions call back into `GameFrame`, which swaps to `GamePanel`.
4. `GamePanel` starts the timer, updates entities, and delegates rendering.
5. Game end returns to the menu and restarts music via `SoundPlayer`.

## API Surface

Public classes exposed by the project:

- `Alien`
- `Bullet`
- `EntityManager`
- `GameFrame`
- `GamePanel`
- `GameRenderer`
- `GameUI`
- `Leaderboard`
- `MenuPanel`
- `Player`
- `PowerUp`
- `Shooter`
- `SoundPlayer`
- `SpaceInvadersGame`

The generated HTML API documentation should be placed under `docs/api/`.

## Build Artifacts

Expected outputs:

- Executable jar: `target/space-invaders-game-1.0.0.jar`
- API docs: `docs/api/`

## Local Build Commands

If Maven is available:

```bash
mvn clean package
mvn javadoc:javadoc
```

If Maven is not available, use the JDK tools directly from `JAVA_HOME`:

```bash
"%JAVA_HOME%\\bin\\javac.exe" -d target/classes src/main/java/com/spaceinvaders/*.java
"%JAVA_HOME%\\bin\\jar.exe" --create --file target/space-invaders-game-1.0.0.jar --main-class com.spaceinvaders.SpaceInvadersGame -C target/classes .
"%JAVA_HOME%\\bin\\javadoc.exe" -d docs/api -sourcepath src/main/java -subpackages com.spaceinvaders
```
