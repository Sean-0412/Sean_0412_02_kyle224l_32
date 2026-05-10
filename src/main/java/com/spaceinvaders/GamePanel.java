package com.spaceinvaders;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private Shooter shooter;
    private ArrayList<Bullet> bullets;
    private ArrayList<Alien> aliens;
    
    private boolean isGameOver = false;
    private boolean isGameWon = false;
    private int score = 0;
    
    private int alienDirectionX = 2; 
    private int alienSpeedY = 15;    

    public GamePanel() {
        this.setPreferredSize(new Dimension(800, 600)); // 設定畫布大小 [cite: 67]
        this.setBackground(Color.BLACK); 
        this.setFocusable(true);
        this.addKeyListener(this);
        
        initGame();

        timer = new Timer(20, this); // 遊戲迴圈定時器 [cite: 130-134]
        timer.start();
    }

    private void initGame() {
        shooter = new Shooter(375, 500);
        bullets = new ArrayList<>();
        aliens = new ArrayList<>();
        score = 0;
        isGameOver = false;
        isGameWon = false;
        alienDirectionX = 2;

        // 產生外星人陣列 [cite: 46]
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 10; col++) {
                aliens.add(new Alien(50 + col * 50, 50 + row * 40));
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (isGameOver) {
            drawGameOver(g, "GAME OVER");
            return;
        }
        if (isGameWon) {
            drawGameOver(g, "YOU WIN!");
            return;
        }

        shooter.draw(g);
        for (Bullet b : bullets) b.draw(g);
        for (Alien a : aliens) a.draw(g);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 20, 30); // 顯示分數 [cite: 18]
    }

    private void drawGameOver(Graphics g, String message) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.drawString(message, 250, 250);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Press 'R' to Restart", 300, 300); // 重新開始提示 [cite: 22]
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isGameOver && !isGameWon) {
            updateGame();
        }
        repaint(); // 更新畫面 [cite: 54]
    }

private void updateGame() {
        shooter.move();

        // 1. 更新子彈位置
        Iterator<Bullet> itBullet = bullets.iterator();
        while (itBullet.hasNext()) {
            Bullet b = itBullet.next();
            b.update();
            // 子彈飛出畫面最上方，移除子彈
            if (b.y < 0) itBullet.remove();
        }

        // 2. 計算外星人是否撞到左右牆壁
        boolean hitWall = false;
        for (Alien a : aliens) {
            if (a.x <= 0 || a.x >= 770) { 
                hitWall = true;
                break;
            }
        }

        // 3. 移動外星人 (撞牆就反轉並往下，否則繼續橫向移動)
        if (hitWall) {
            alienDirectionX = -alienDirectionX; 
            for (Alien a : aliens) {
                a.move(0, alienSpeedY); 
            }
        } else {
            for (Alien a : aliens) {
                a.move(alienDirectionX, 0);
            }
        }

        // 4. 檢查外星人狀態（這是最重要的部分！）
        Rectangle shooterBounds = shooter.getBounds();
        Iterator<Alien> itAlien = aliens.iterator();
        
        while (itAlien.hasNext()) {
            Alien a = itAlien.next();
            
            // 情況 A：外星人直接撞到玩家飛船 -> 遊戲結束
            if (a.getBounds().intersects(shooterBounds)) {
                isGameOver = true; 
                return; // 直接停止更新
            }
            
            // 情況 B：外星人沒被打死，但已經跑出畫面底部 -> 直接移除它
            if (a.y > 600) {
                itAlien.remove();
                continue; // 這隻已經處理完了，跳過後面的子彈檢查
            }
            
            // 情況 C：檢查這隻外星人有沒有被子彈打中
            boolean hitByBullet = false;
            Iterator<Bullet> itBul = bullets.iterator();
            while (itBul.hasNext()) {
                Bullet b = itBul.next();
                if (b.getBounds().intersects(a.getBounds())) {
                    itBul.remove(); // 移除打中目標的子彈
                    hitByBullet = true;
                    score += 10;    // 加分
                    break;
                }
            }
            
            // 如果被子彈打中，移除這隻外星人
            if (hitByBullet) {
                itAlien.remove(); 
            }
        }

        // 5. 只要畫面上沒有外星人（被打死 或 跑出畫面），就馬上生成新的一波！
        if (aliens.isEmpty()) {
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 10; col++) {
                    aliens.add(new Alien(50 + col * 50, 50 + row * 40));
                }
            }
            // 稍微增加難度（加快移動速度）
            if (alienDirectionX > 0) {
                alienDirectionX += 1; 
            } else {
                alienDirectionX -= 1;
            }
        }
    }
    // --- 鍵盤監聽事件 ---
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (key == KeyEvent.VK_LEFT) {
            shooter.setDirectionX(-5);
        } else if (key == KeyEvent.VK_RIGHT) {
            shooter.setDirectionX(5);
        } else if (key == KeyEvent.VK_UP) {
            shooter.setDirectionY(-5);
        } else if (key == KeyEvent.VK_DOWN) {
            shooter.setDirectionY(5);
        } else if (key == KeyEvent.VK_SPACE) {
            if (!isGameOver && !isGameWon) {
                bullets.add(new Bullet(shooter.x + 22, shooter.y)); // 發射子彈 [cite: 12]
            }
        } else if (key == KeyEvent.VK_R) {
            if (isGameOver || isGameWon) {
                initGame(); 
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) {
            shooter.setDirectionX(0);
        } else if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) {
            shooter.setDirectionY(0);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}