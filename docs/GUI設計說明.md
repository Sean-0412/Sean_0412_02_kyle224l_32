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

## GUI 程式碼安排

### `GameFrame`

`GameFrame` 只負責視窗與畫面切換，不直接處理遊戲細節。當使用者從選單開始遊戲、回到難度選單或回到主選單時，都由這個類別更換目前顯示的面板。

### `MenuPanel`

`MenuPanel` 負責主選單、遊戲模式選擇、玩家數選擇、難度選擇、控制說明、關於頁面與排行榜頁面。它也負責選單畫面的鍵盤事件與繪圖。

### `GamePanel`

`GamePanel` 是遊戲核心畫面，處理遊戲迴圈、鍵盤輸入、暫停、重新開始與遊戲結束判定。它不直接畫所有物件，而是委派給 `GameRenderer` 與 `GameUI`。

### `GameRenderer`

`GameRenderer` 負責把玩家、子彈與敵人畫到畫面上。這樣可以把「遊戲邏輯」和「畫面繪製」分開，避免 `GamePanel` 過長。

### `GameUI`

`GameUI` 負責顯示分數、生命值、模式、暫停畫面與 Game Over 畫面，讓 HUD 的繪圖邏輯和遊戲物件繪圖分離。

### `EntityManager`

`EntityManager` 管理敵人、敵方子彈與道具的生成、更新與碰撞後狀態。遊戲主畫面只需要透過它取得目前的敵人資料即可。

## 畫面切換流程

1. `SpaceInvadersGame.main` 透過 EDT 建立 `GameFrame`。
2. `GameFrame` 預設顯示 `MenuPanel`。
3. 選單操作會回呼到 `GameFrame`，再切換到 `GamePanel`。
4. `GamePanel` 啟動 `Timer`，每 16ms 更新一次遊戲。
5. 畫面繪製交給 `GameRenderer` 和 `GameUI`。

## 目前的 GUI 檔案安排

- [GameFrame.java](../src/main/java/com/spaceinvaders/GameFrame.java)
- [MenuPanel.java](../src/main/java/com/spaceinvaders/MenuPanel.java)
- [GamePanel.java](../src/main/java/com/spaceinvaders/GamePanel.java)
- [GameRenderer.java](../src/main/java/com/spaceinvaders/GameRenderer.java)
- [GameUI.java](../src/main/java/com/spaceinvaders/GameUI.java)
