package com.spaceinvaders;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;

/**
 * Main menu panel for Space Invaders game.
 */
public class MenuPanel extends JPanel implements KeyListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    
    private int selectedOption = 0;
    private static final String[] MAIN_MENU_OPTIONS = {"Start Game", "Leaderboard", "Controls", "About", "Exit"};
    private static final String[] GAME_MODE_OPTIONS = {"Classic Mode", "Dodging Mode", "Stage Mode"};
    private static final String[] PLAYER_COUNT_OPTIONS = {"Single Player", "Two Player"};
    private static final String[] DIFFICULTY_OPTIONS = {"Easy", "Normal", "Hard"};
    private static final String[] LEADERBOARD_MODE_OPTIONS = {"Single Player", "Two Player"};
    
    private GameFrame gameFrame;
    
    // Menu states
    private static final int STATE_MAIN_MENU = 0;
    private static final int STATE_GAME_MODE = 1;
    private static final int STATE_PLAYER_COUNT = 2;
    private static final int STATE_DIFFICULTY = 3;
    private static final int STATE_CONTROLS = 4;
    private static final int STATE_ABOUT = 5;
    private static final int STATE_LEADERBOARD = 6;
    
    private int currentState = STATE_MAIN_MENU;
    private int selectedGameMode = 0; // 0 = Classic, 1 = Dodging, 2 = Stage
    private int selectedPlayerCount = 0; // 0 = Single Player, 1 = Two Player
    private int selectedLeaderboardMode = 0; // 0 = Single Player, 1 = Two Player
    
    public MenuPanel(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        requestFocusInWindow();
    }
    
    public void setInitialStateToDifficultyMenu(int gameMode) {
        this.currentState = STATE_DIFFICULTY;
        this.selectedGameMode = gameMode;
        this.selectedOption = 1; // Default to Normal
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        switch(currentState) {
            case STATE_MAIN_MENU:
                drawMainMenu(g2);
                break;
            case STATE_GAME_MODE:
                drawGameModeMenu(g2);
                break;
            case STATE_PLAYER_COUNT:
                drawPlayerCountMenu(g2);
                break;
            case STATE_DIFFICULTY:
                drawDifficultyMenu(g2);
                break;
            case STATE_CONTROLS:
                drawControlsScreen(g2);
                break;
            case STATE_ABOUT:
                drawAboutScreen(g2);
                break;
            case STATE_LEADERBOARD:
                drawLeaderboardScreen(g2);
                break;
        }
    }
    
    private void drawGameModeMenu(Graphics2D g2) {
        // Title
        g2.setColor(new Color(0, 255, 150));
        g2.setFont(new Font("Consolas", Font.BOLD, 60));
        String title = "Select Game Mode";
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (WIDTH - tw) / 2, 100);
        
        // Mode descriptions
        g2.setColor(new Color(100, 200, 255));
        g2.setFont(new Font("Consolas", Font.PLAIN, 16));
        
        int startY = 200;
        
        // Classic Mode
        String classicDesc1 = "Only left/right movement";
        String classicDesc2 = "Game ends when enemies cross the line";
        int cw1 = g2.getFontMetrics().stringWidth(classicDesc1);
        int cw2 = g2.getFontMetrics().stringWidth(classicDesc2);
        if (selectedOption == 0) {
            g2.setColor(new Color(255, 200, 0));
            g2.drawString("► CLASSIC MODE ◄", (WIDTH - 220) / 2, startY);
        } else {
            g2.setColor(new Color(100, 200, 255));
            g2.drawString("CLASSIC MODE", (WIDTH - 200) / 2, startY);
        }
        g2.setColor(new Color(150, 200, 255));
        g2.drawString(classicDesc1, (WIDTH - cw1) / 2, startY + 50);
        g2.drawString(classicDesc2, (WIDTH - cw2) / 2, startY + 75);
        
        // Dodging Mode
        String dodgeDesc1 = "Move in all directions";
        String dodgeDesc2 = "Game ends if hit by enemies";
        int dw1 = g2.getFontMetrics().stringWidth(dodgeDesc1);
        int dw2 = g2.getFontMetrics().stringWidth(dodgeDesc2);
        startY = 330;
        if (selectedOption == 1) {
            g2.setColor(new Color(255, 200, 0));
            g2.drawString("► DODGING MODE ◄", (WIDTH - 220) / 2, startY);
        } else {
            g2.setColor(new Color(100, 200, 255));
            g2.drawString("DODGING MODE", (WIDTH - 200) / 2, startY);
        }
        g2.setColor(new Color(150, 200, 255));
        g2.drawString(dodgeDesc1, (WIDTH - dw1) / 2, startY + 50);
        g2.drawString(dodgeDesc2, (WIDTH - dw2) / 2, startY + 75);
        
        // Stage Mode
        String stageDesc1 = "Move freely and dodge enemies";
        String stageDesc2 = "Enemies spawn randomly each stage";
        int sw1 = g2.getFontMetrics().stringWidth(stageDesc1);
        int sw2 = g2.getFontMetrics().stringWidth(stageDesc2);
        startY = 450;
        if (selectedOption == 2) {
            g2.setColor(new Color(255, 200, 0));
            g2.drawString("► STAGE MODE ◄", (WIDTH - 220) / 2, startY);
        } else {
            g2.setColor(new Color(100, 200, 255));
            g2.drawString("STAGE MODE", (WIDTH - 200) / 2, startY);
        }
        g2.setColor(new Color(150, 200, 255));
        g2.drawString(stageDesc1, (WIDTH - sw1) / 2, startY + 50);
        g2.drawString(stageDesc2, (WIDTH - sw2) / 2, startY + 75);
        
        // Hint
        g2.setColor(new Color(100, 200, 255));
        g2.setFont(new Font("Consolas", Font.PLAIN, 14));
        String hint = "Use ↑↓ to select, Enter to confirm, Esc to return";
        int hw = g2.getFontMetrics().stringWidth(hint);
        g2.drawString(hint, (WIDTH - hw) / 2, 550);
    }

    private void drawPlayerCountMenu(Graphics2D g2) {
        drawTitle(g2, "Player Count", 100);

        int startY = 250;
        for (int i = 0; i < PLAYER_COUNT_OPTIONS.length; i++) {
            drawMenuItem(g2, PLAYER_COUNT_OPTIONS[i], i, startY + i * 100);
        }

        drawHint(g2, "Use ↑↓ to select, Enter to confirm, Esc to return");
    }

    private void drawMainMenu(Graphics2D g2) {
        drawTitle(g2, "SPACE INVADERS", 100);
        drawSubTitle(g2, "Classic Space Shooting Game", 140);

        int startY = 220;
        for (int i = 0; i < MAIN_MENU_OPTIONS.length; i++) {
            drawMenuItem(g2, MAIN_MENU_OPTIONS[i], i, startY + i * 80);
        }

        drawHint(g2, "Use ↑↓ to select, Enter to confirm");
    }
    
    private void drawDifficultyMenu(Graphics2D g2) {
        drawTitle(g2, "Select Difficulty", 100);

        int startY = 250;
        for (int i = 0; i < DIFFICULTY_OPTIONS.length; i++) {
            drawMenuItem(g2, DIFFICULTY_OPTIONS[i], i, startY + i * 100);
        }

        drawHint(g2, "Esc to return to menu");
    }
    
    private void drawControlsScreen(Graphics2D g2) {
        drawTitle(g2, "Controls", 80);

        String[] player1 = {
            "P1: ← → : Move left/right",
            "P1: ↑ ↓ : Move up/down (Dodging/Stage mode)",
            "P1: Space : Shoot"
        };
        String[] player2 = {
            "P2: W A S D : Move",
            "P2: H : Shoot"
        };
        String[] singlePlayer = {
            "U : Ultimate (Single Player Only)"
        };
        String[] common = {
            "P : Pause",
            "R : Restart game",
            "Esc : Return to menu"
        };

        int lineHeight = 30;
        int sectionGap = 26;
        int hintHeight = 80;
        int listHeight = 40 + player1.length * lineHeight + sectionGap + player2.length * lineHeight + sectionGap + singlePlayer.length * lineHeight + sectionGap + common.length * lineHeight + hintHeight;
        int listWidth = 560;
        int listX = (WIDTH - listWidth) / 2;
        int listY = 140;

        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRoundRect(listX, listY, listWidth, listHeight, 28, 28);
        g2.setColor(new Color(100, 200, 255, 200));
        g2.setStroke(new java.awt.BasicStroke(2));
        g2.drawRoundRect(listX, listY, listWidth, listHeight, 28, 28);

        int y = listY + 45;
        g2.setColor(new Color(170, 255, 210));
        g2.setFont(new Font("Consolas", Font.BOLD, 22));
        String player1Title = "PLAYER 1";
        g2.drawString(player1Title, listX + 30, y);
        y += lineHeight;

        g2.setFont(new Font("Consolas", Font.PLAIN, 20));
        for (String line : player1) {
            g2.drawString(line, listX + 30, y);
            y += lineHeight;
        }

        y += 10;
        g2.setFont(new Font("Consolas", Font.BOLD, 22));
        String player2Title = "PLAYER 2";
        g2.drawString(player2Title, listX + 30, y);
        y += lineHeight;

        g2.setFont(new Font("Consolas", Font.PLAIN, 20));
        for (String line : player2) {
            g2.drawString(line, listX + 30, y);
            y += lineHeight;
        }

        y += 10;
        g2.setFont(new Font("Consolas", Font.BOLD, 22));
        g2.drawString("SINGLE PLAYER", listX + 30, y);
        y += lineHeight;

        g2.setFont(new Font("Consolas", Font.PLAIN, 20));
        for (String line : singlePlayer) {
            g2.drawString(line, listX + 30, y);
            y += lineHeight;
        }

        y += 10;
        g2.setFont(new Font("Consolas", Font.BOLD, 22));
        g2.drawString("COMMON", listX + 30, y);
        y += lineHeight;

        g2.setFont(new Font("Consolas", Font.PLAIN, 20));
        for (String line : common) {
            g2.drawString(line, listX + 30, y);
            y += lineHeight;
        }

        y += 20;
        g2.setColor(new Color(150, 255, 150));
        g2.setFont(new Font("Consolas", Font.PLAIN, 16));
        String hint = "Press Enter or Esc to return to menu";
        int textWidth = g2.getFontMetrics().stringWidth(hint);
        g2.drawString(hint, (WIDTH - textWidth) / 2, y);
    }
    
    private void drawLeaderboardScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 255, 150));
        g2.setFont(new Font("Consolas", Font.BOLD, 60));
        String title = "Leaderboard";
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (WIDTH - tw) / 2, 100);

        g2.setColor(new Color(100, 200, 255));
        g2.setFont(new Font("Consolas", Font.PLAIN, 16));
        String note = "Only Stage Mode scores are recorded in the leaderboard.";
        int nw = g2.getFontMetrics().stringWidth(note);
        g2.drawString(note, (WIDTH - nw) / 2, 135);

        int tabWidth = 220;
        int tabHeight = 50;
        int tabY = 140;
        int singleX = (WIDTH / 2) - tabWidth - 10;
        int twoX = (WIDTH / 2) + 10;
        for (int i = 0; i < LEADERBOARD_MODE_OPTIONS.length; i++) {
            int x = (i == 0) ? singleX : twoX;
            boolean selected = selectedLeaderboardMode == i;
            g2.setColor(selected ? new Color(255, 200, 0, 180) : new Color(100, 200, 255, 120));
            g2.fillRoundRect(x, tabY, tabWidth, tabHeight, 20, 20);
            g2.setColor(selected ? new Color(255, 230, 120) : new Color(220, 240, 255));
            g2.setFont(new Font("Consolas", selected ? Font.BOLD : Font.PLAIN, 22));
            String text = LEADERBOARD_MODE_OPTIONS[i];
            int textW = g2.getFontMetrics().stringWidth(text);
            g2.drawString(text, x + (tabWidth - textW) / 2, tabY + 34);
        }

        java.util.List<Leaderboard.Entry> entries = gameFrame.getLeaderboard().getEntries(selectedLeaderboardMode == 1);
        int startY = 240;
        g2.setFont(new Font("Consolas", Font.PLAIN, 26));
        if (entries.isEmpty()) {
            String emptyText = "No scores yet. Play a game to add your score!";
            int ew = g2.getFontMetrics().stringWidth(emptyText);
            g2.drawString(emptyText, (WIDTH - ew) / 2, startY);
        } else {
            for (int i = 0; i < entries.size(); i++) {
                Leaderboard.Entry entry = entries.get(i);
                String line = String.format("%2d. %6d  %s", i + 1, entry.getScore(), entry.getTimestamp().format(java.time.format.DateTimeFormatter.ofPattern("MM/dd HH:mm")));
                int lw = g2.getFontMetrics().stringWidth(line);
                g2.drawString(line, (WIDTH - lw) / 2, startY + i * 40);
            }
        }

        g2.setColor(new Color(150, 255, 150));
        g2.setFont(new Font("Consolas", Font.PLAIN, 16));
        String hint = "Use ←→ to switch mode. Enter/Esc to return.";
        int hw = g2.getFontMetrics().stringWidth(hint);
        g2.drawString(hint, (WIDTH - hw) / 2, 560);
    }
    
    private void drawAboutScreen(Graphics2D g2) {
        drawTitle(g2, "About Game", 80);

        g2.setColor(new Color(100, 200, 255));
        g2.setFont(new Font("Consolas", Font.PLAIN, 24));

        String[] aboutInfo = {
            "SPACE INVADERS",
            "Classic Space Shooting Game",
            "",
            "Developer: Game Development Team",
            "Version: 1.0",
            "Year: 2026",
            "",
            "Thank you for playing!"
        };

        int startY = 150;
        for (int i = 0; i < aboutInfo.length; i++) {
            if (aboutInfo[i].isEmpty()) {
                startY += 20;
            } else {
                int textWidth = g2.getFontMetrics().stringWidth(aboutInfo[i]);
                g2.drawString(aboutInfo[i], (WIDTH - textWidth) / 2, startY);
                startY += 50;
            }
        }

        drawHint(g2, "Press Enter or Esc to return to menu");
    }
    
    private void drawMenuItem(Graphics2D g2, String text, int index, int y) {
        Font menuFont = selectedOption == index ? new Font("Consolas", Font.BOLD, 36) : new Font("Consolas", Font.PLAIN, 36);
        g2.setFont(menuFont);
        Rectangle2D bounds = g2.getFontMetrics().getStringBounds(text, g2);
        int textX = (WIDTH - (int) bounds.getWidth()) / 2;
        int boxX = textX - 30;
        int boxY = y - 35;
        int boxWidth = (int) bounds.getWidth() + 60;
        int boxHeight = 55;

        if (selectedOption == index) {
            g2.setColor(new Color(150, 120, 0, 120));
            g2.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 16, 16);
            g2.setColor(new Color(255, 200, 0));
            g2.setStroke(new java.awt.BasicStroke(3));
            g2.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 16, 16);
            g2.setColor(new Color(255, 230, 120));
            g2.drawString(text, textX, y);
            g2.drawString("◆", boxX - 50, y);
            g2.drawString("◆", boxX + boxWidth + 30, y);
        } else {
            g2.setColor(new Color(100, 200, 255));
            g2.drawString(text, textX, y);
        }
    }

    private void drawTitle(Graphics2D g2, String title, int y) {
        g2.setColor(new Color(0, 255, 150));
        g2.setFont(new Font("Consolas", Font.BOLD, 70));
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (WIDTH - tw) / 2, y);
    }

    private void drawSubTitle(Graphics2D g2, String text, int y) {
        g2.setColor(new Color(100, 200, 255));
        g2.setFont(new Font("Consolas", Font.PLAIN, 20));
        int tw = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, (WIDTH - tw) / 2, y);
    }

    private void drawHint(Graphics2D g2, String text) {
        g2.setColor(new Color(150, 255, 150));
        g2.setFont(new Font("Consolas", Font.PLAIN, 14));
        int tw = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, (WIDTH - tw) / 2, 560);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        
        switch(currentState) {
            case STATE_MAIN_MENU:
                handleMainMenuInput(code);
                break;
            case STATE_GAME_MODE:
                handleGameModeInput(code);
                break;
            case STATE_PLAYER_COUNT:
                handlePlayerCountInput(code);
                break;
            case STATE_DIFFICULTY:
                handleDifficultyInput(code);
                break;
            case STATE_CONTROLS:
                handleControlsInput(code);
                break;
            case STATE_ABOUT:
                handleAboutInput(code);
                break;
            case STATE_LEADERBOARD:
                handleLeaderboardInput(code);
                break;
        }
    }
    
    private void handleMainMenuInput(int code) {
        if (code == KeyEvent.VK_UP) {
            selectedOption = (selectedOption - 1 + MAIN_MENU_OPTIONS.length) % MAIN_MENU_OPTIONS.length;
            repaint();
        } else if (code == KeyEvent.VK_DOWN) {
            selectedOption = (selectedOption + 1) % MAIN_MENU_OPTIONS.length;
            repaint();
        } else if (code == KeyEvent.VK_ENTER) {
            switch(selectedOption) {
                case 0: // Start Game
                    currentState = STATE_GAME_MODE;
                    selectedOption = 0; // Reset to first mode
                    repaint();
                    break;
                case 1: // Leaderboard
                    selectedLeaderboardMode = 0;
                    currentState = STATE_LEADERBOARD;
                    repaint();
                    break;
                case 2: // Controls
                    currentState = STATE_CONTROLS;
                    repaint();
                    break;
                case 3: // About
                    currentState = STATE_ABOUT;
                    repaint();
                    break;
                case 4: // Exit
                    System.exit(0);
                    break;
            }
        }
    }
    
    private void handleGameModeInput(int code) {
        if (code == KeyEvent.VK_UP) {
            selectedOption = (selectedOption - 1 + GAME_MODE_OPTIONS.length) % GAME_MODE_OPTIONS.length;
            repaint();
        } else if (code == KeyEvent.VK_DOWN) {
            selectedOption = (selectedOption + 1) % GAME_MODE_OPTIONS.length;
            repaint();
        } else if (code == KeyEvent.VK_ENTER) {
            selectedGameMode = selectedOption;
<<<<<<< HEAD
            currentState = STATE_PLAYER_COUNT;
            selectedOption = selectedPlayerCount;
            repaint();
        } else if (code == KeyEvent.VK_ESCAPE) {
            currentState = STATE_MAIN_MENU;
            selectedOption = 0;
            repaint();
        }
    }
    
    private void handleDifficultyInput(int code) {
        if (code == KeyEvent.VK_UP) {
            selectedOption = (selectedOption - 1 + DIFFICULTY_OPTIONS.length) % DIFFICULTY_OPTIONS.length;
            repaint();
        } else if (code == KeyEvent.VK_DOWN) {
            selectedOption = (selectedOption + 1) % DIFFICULTY_OPTIONS.length;
            repaint();
        } else if (code == KeyEvent.VK_ENTER) {
            startGame();
        } else if (code == KeyEvent.VK_ESCAPE) {
            currentState = STATE_PLAYER_COUNT;
            selectedOption = selectedPlayerCount;
            repaint();
        }
    }
    
    private void handleControlsInput(int code) {
        if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_ESCAPE) {
            currentState = STATE_MAIN_MENU;
            selectedOption = 0;
            repaint();
        }
    }

    private void handlePlayerCountInput(int code) {
        if (code == KeyEvent.VK_UP) {
            selectedOption = (selectedOption - 1 + PLAYER_COUNT_OPTIONS.length) % PLAYER_COUNT_OPTIONS.length;
            repaint();
        } else if (code == KeyEvent.VK_DOWN) {
            selectedOption = (selectedOption + 1) % PLAYER_COUNT_OPTIONS.length;
            repaint();
        } else if (code == KeyEvent.VK_ENTER) {
            selectedPlayerCount = selectedOption;
            currentState = STATE_DIFFICULTY;
            selectedOption = 1;
            repaint();
        } else if (code == KeyEvent.VK_ESCAPE) {
            currentState = STATE_GAME_MODE;
            selectedOption = selectedGameMode;
            repaint();
        }
    }
    
    private void handleAboutInput(int code) {
        if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_ESCAPE) {
            currentState = STATE_MAIN_MENU;
            selectedOption = 0;
            repaint();
        }
    }
    
    private void handleLeaderboardInput(int code) {
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_UP) {
            selectedLeaderboardMode = 0;
            repaint();
        } else if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_DOWN) {
            selectedLeaderboardMode = 1;
            repaint();
        } else if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_ESCAPE) {
            currentState = STATE_MAIN_MENU;
            selectedOption = 0;
            repaint();
        }
    }
    
    private void startGame() {
        gameFrame.startGameWithSettings(selectedGameMode, selectedOption, selectedPlayerCount == 1);
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
    }
}
