package com.spaceinvaders;

import javax.swing.JFrame;

public class SpaceInvadersGame {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Space Invaders - Java Project");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GamePanel panel = new GamePanel();
        frame.setContentPane(panel);
        frame.pack(); // 自動貼合 GamePanel 的大小
        frame.setLocationRelativeTo(null); // 視窗置中
        frame.setResizable(false);
        frame.setVisible(true);

        panel.requestFocusInWindow(); // 確保鍵盤事件能被接收
    }
}