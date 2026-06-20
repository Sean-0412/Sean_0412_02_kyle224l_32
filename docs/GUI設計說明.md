# GUI 設計說明

GUI 採用 Swing 拆分成多個職責單純的類別，避免把選單、遊戲迴圈、繪圖與狀態處理全部塞進同一個檔案。

## 分工

- `GameFrame` 是最上層視窗，負責切換主選單和遊戲畫面。
- `MenuPanel` 負責所有選單狀態、鍵盤操作與選單繪圖。
- `GamePanel` 負責遊戲主迴圈、輸入處理、狀態更新與 repaint。
- `GameRenderer` 專門繪製遊戲物件。
- `GameUI` 專門繪製 HUD、暫停遮罩與 Game Over 畫面。
- `EntityManager` 負責敵人、子彈、道具的生成與更新。

## 畫面切換流程

1. `SpaceInvadersGame.main` 透過 EDT 建立 `GameFrame`。
2. `GameFrame` 預設顯示 `MenuPanel`。
3. 選單操作會回呼到 `GameFrame`，再切換到 `GamePanel`。
4. `GamePanel` 啟動 `Timer`，每 16ms 更新一次遊戲。
5. 畫面繪製交給 `GameRenderer` 和 `GameUI`。

## 目前的 GUI 檔案安排

- [src/main/java/com/spaceinvaders/GameFrame.java](../src/main/java/com/spaceinvaders/GameFrame.java)
- [src/main/java/com/spaceinvaders/MenuPanel.java](../src/main/java/com/spaceinvaders/MenuPanel.java)
- [src/main/java/com/spaceinvaders/GamePanel.java](../src/main/java/com/spaceinvaders/GamePanel.java)
- [src/main/java/com/spaceinvaders/GameRenderer.java](../src/main/java/com/spaceinvaders/GameRenderer.java)
- [src/main/java/com/spaceinvaders/GameUI.java](../src/main/java/com/spaceinvaders/GameUI.java)
