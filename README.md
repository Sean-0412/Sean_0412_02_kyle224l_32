# Space Invaders Game - Java Swing

A classic Space Invaders game implementation in Java using Swing GUI framework.

## Project Structure

```
space-invaders/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/spaceinvaders/
│   │   │       ├── SpaceInvadersGame.java    # Main entry point
│   │   │       ├── GamePanel.java            # Game logic & rendering
│   │   │       ├── Alien.java                # Enemy entity
│   │   │       ├── Bullet.java               # Projectile entity
│   │   │       └── SoundPlayer.java          # Sound effects utility
│   │   └── resources/
│   └── test/
│       └── java/com/spaceinvaders/
├── pom.xml                                   # Maven configuration
└── README.md
```

## Features

- **Player Movement**: Use Left/Right arrow keys to move
- **Shooting**: Press Space to fire bullets
- **Alien Waves**: Multiple rows of enemies moving left and right
- **Collision Detection**: Bullets destroy aliens, aliens reaching bottom end the game
- **Scoring System**: Gain 10 points per alien destroyed
- **Difficulty Scaling**: Game speed increases with score
- **Sound Effects**: Audio feedback for shooting, hits, and game over
- **Game States**: Win condition (destroy all aliens), lose condition (aliens reach bottom)
- **Restart**: Press R after game over to restart

## Build & Run

### Using Maven

```bash
# Compile
mvn clean compile

# Package into JAR
mvn package

# Run
java -cp target/space-invaders-game-1.0.0.jar com.spaceinvaders.SpaceInvadersGame
```

### Using Gradle (Alternative)

Create a `build.gradle` file in the project root and use:
```bash
gradle build
gradle run
```

### Direct Compilation

```bash
# Compile all Java files
javac -d target/classes src/main/java/com/spaceinvaders/*.java

# Run
java -cp target/classes com.spaceinvaders.SpaceInvadersGame
```

## Game Controls

| Key | Action |
|-----|--------|
| ← | Move player left |
| → | Move player right |
| Space | Fire bullet |
| R | Restart (after game over) |

## System Requirements

- Java 11 or higher
- Operating System: Windows, macOS, or Linux

## Author

Space Invaders Game - Java Project

## License

Open Source
