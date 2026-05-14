package com.spaceinvaders;

import javax.swing.SwingUtilities;

/**
 * Main entry point for the Space Invaders game.
 */
public class SpaceInvadersGame {
    public static void main(String[] args) {
        System.out.println("Starting Space Invaders...");
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                System.out.println("Creating GameFrame on EDT...");
                GameFrame frame = new GameFrame();
                System.out.println("GameFrame created, setting visible...");
                frame.setVisible(true);
                System.out.println("Game window should now be visible.");
            }
        });
        
        // Keep the application running with a loop
        System.out.println("Main thread: Starting event loop...");
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
