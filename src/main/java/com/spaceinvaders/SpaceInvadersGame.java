package com.spaceinvaders;

import javax.swing.JFrame;

/**
 * Main entry point for the Space Invaders game.
 */
public class SpaceInvadersGame {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Space Invaders - Java Project");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GamePanel panel = new GamePanel();
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);

        panel.requestFocusInWindow();
    }
}
