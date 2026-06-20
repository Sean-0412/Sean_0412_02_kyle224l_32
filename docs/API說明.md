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

## 類別角色簡述

### `GameFrame`

負責主視窗與面板切換，讓使用者可以在主選單、遊戲畫面和難度選單之間切換。

### `MenuPanel`

負責選單、控制說明、關於頁面與排行榜畫面的繪製與鍵盤選取。

### `GamePanel`

負責遊戲主迴圈、玩家輸入、遊戲狀態、分數與結束判定。

### `EntityManager`

負責敵人、子彈、道具與 Boss 的生成、更新與管理。

### `Player`

負責玩家移動、射擊、血量、護盾、攻擊加成與 Ultimate 技能。

### `Leaderboard`

負責讀寫 `leaderboard.txt`，並區分單人與雙人紀錄。

### `SoundPlayer`

負責背景音樂與音效播放。

## 產生的 Javadoc

已生成的 HTML API 文件位於 [docs/api/index.html](docs/api/index.html)。
