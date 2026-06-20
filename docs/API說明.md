# API 說明

本專案的公開 API 主要集中在 `com.spaceinvaders` 套件，對外可用的類別如下：

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

## 主要功能入口

- `SpaceInvadersGame.main(String[] args)`：程式入口。
- `GameFrame.startGameWithSettings(...)`：從選單進入遊戲。
- `GameFrame.returnToMainMenu()`：回到主選單。
- `GamePanel.startGame()`：啟動遊戲迴圈與音樂。
- `Leaderboard.addScore(int score, boolean twoPlayer)`：記錄排行榜分數。

## 產生的 Javadoc

已生成的 HTML API 文件位於 [docs/api/index.html](api/index.html)。
