# Java Swing Space Invaders 專題

這是一個使用 Java Swing 製作的 Space Invaders 專題遊戲，包含主選單、不同遊戲模式、雙人模式、排行榜、音效與可執行 JAR 發布檔。

## 專案簡介

本專題以 Java 11 開發，使用 Swing 建立圖形介面，並透過 `GameFrame`、`MenuPanel`、`GamePanel`、`GameRenderer`、`GameUI` 與 `EntityManager` 分工完成遊戲視窗、選單、主遊戲邏輯與繪圖。

## 遊戲功能

- 主選單、控制說明、關於頁面與排行榜頁面
- 三種遊戲模式：Classic Mode、Dodging Mode、Stage Mode
- 單人與雙人遊玩
- 玩家移動、射擊、Ultimate 技能與道具效果
- 敵人生成、波次控制、Boss 戰與分數累計
- 暫停、重開、回到主選單
- 背景音樂、射擊音效、爆炸音效與結束音效

## 遊戲操作方式

### 單人模式

- `←`、`→`：左右移動
- `↑`、`↓`：Dodging Mode 與 Stage Mode 可上下移動
- `Space`：射擊
- `U`：單人模式可使用 Ultimate

### 雙人模式

- P1：`←`、`→` 移動，`↑`、`↓` 上下移動，`Space` 射擊
- P2：`A`、`D` 移動，`W`、`S` 上下移動，`H` 射擊

### 共通操作

- `P`：暫停 / 繼續
- `R`：遊戲結束後重新開始
- `Esc`：回到主選單
- 選單畫面使用方向鍵選擇，`Enter` 確認

## Demo 影片

- [Demo 影片：Space Invender.mp4](Space%20Invender.mp4)

## 文件索引

- [UML 類別圖](docs/UML.md)
- [GUI 程式碼安排說明](docs/GUI%E8%A8%AD%E8%A8%88%E8%AA%AA%E6%98%8E.md)
- [API 說明](docs/API%E8%AA%AA%E6%98%8E.md)
- [Javadoc API 文件](docs/api/index.html)

## Runnable JAR 與發佈檔

正式發佈檔放在 [dist/SpaceInvadersGame-release.zip](dist/SpaceInvadersGame-release.zip)。

### 下載與執行

1. 下載 `SpaceInvadersGame-release.zip`
2. 解壓縮後，確認 `SpaceInvadersGame.jar` 與 `resouce` 資料夾放在同一層
3. 執行以下指令：

```bash
java -jar SpaceInvadersGame.jar
```

如果 `SpaceInvadersGame.jar` 和 `resouce` 不在同一層，背景音樂與音效可能無法正常載入。

### Maven 打包指令

```bash
mvn clean package
```

## GitHub Release 說明

正式下載請到 GitHub Releases 頁面取得 `SpaceInvadersGame-release.zip`。

## 專案結構

```text
Sean_0412_02_kyle224l_32/
├── README.md
├── pom.xml
├── build.gradle
├── src/main/java/com/spaceinvaders/
├── resouce/
├── docs/
│   ├── UML.md
│   ├── GUI設計說明.md
│   ├── API說明.md
│   └── api/
│       └── index.html
├── demo/
│   ├── gameplay_full.mp4
│   └── code_explanation.mp4
└── dist/
    ├── SpaceInvadersGame.jar
    └── SpaceInvadersGame-release.zip
```
