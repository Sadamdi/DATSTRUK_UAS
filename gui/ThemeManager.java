package gui;

import java.awt.Color;

/**
 * Manager untuk tema aplikasi (Light/Dark Mode)
 */
public class ThemeManager {
    
    private static boolean darkMode = false;
    
    // Light Mode Colors
    public static final Color LIGHT_BACKGROUND = new Color(245, 247, 250);
    public static final Color LIGHT_PANEL = new Color(255, 255, 255);
    public static final Color LIGHT_CELL = new Color(255, 255, 255);
    public static final Color LIGHT_CELL_INITIAL = new Color(240, 242, 245);
    public static final Color LIGHT_CELL_SOLVED = new Color(220, 245, 220);
    public static final Color LIGHT_CELL_PROCESSING = new Color(255, 245, 157);
    public static final Color LIGHT_CELL_TESTING = new Color(255, 213, 128);
    public static final Color LIGHT_CELL_BACKTRACK = new Color(255, 171, 145);
    public static final Color LIGHT_TEXT = new Color(33, 37, 41);
    public static final Color LIGHT_TEXT_SECONDARY = new Color(108, 117, 125);
    public static final Color LIGHT_TEXT_SOLVED = new Color(46, 125, 50);
    public static final Color LIGHT_BORDER = new Color(33, 37, 41);
    public static final Color LIGHT_BORDER_THIN = new Color(200, 200, 200);
    public static final Color LIGHT_LOG_BG = new Color(30, 30, 30);
    public static final Color LIGHT_LOG_TEXT = new Color(0, 255, 0);
    
    // Dark Mode Colors
    public static final Color DARK_BACKGROUND = new Color(18, 18, 24);
    public static final Color DARK_PANEL = new Color(30, 30, 40);
    public static final Color DARK_CELL = new Color(45, 45, 55);
    public static final Color DARK_CELL_INITIAL = new Color(60, 60, 75);
    public static final Color DARK_CELL_SOLVED = new Color(45, 90, 45);
    public static final Color DARK_CELL_PROCESSING = new Color(100, 90, 30);
    public static final Color DARK_CELL_TESTING = new Color(120, 80, 20);
    public static final Color DARK_CELL_BACKTRACK = new Color(120, 50, 50);
    public static final Color DARK_TEXT = new Color(230, 230, 240);
    public static final Color DARK_TEXT_SECONDARY = new Color(160, 160, 180);
    public static final Color DARK_TEXT_SOLVED = new Color(100, 220, 100);
    public static final Color DARK_BORDER = new Color(100, 100, 120);
    public static final Color DARK_BORDER_THIN = new Color(60, 60, 75);
    public static final Color DARK_LOG_BG = new Color(15, 15, 20);
    public static final Color DARK_LOG_TEXT = new Color(0, 255, 100);
    
    // Button Colors (sama untuk kedua mode)
    public static final Color BTN_SOLVE = new Color(76, 175, 80);
    public static final Color BTN_RESET = new Color(244, 67, 54);
    public static final Color BTN_LOAD = new Color(33, 150, 243);
    public static final Color BTN_SKIP = new Color(255, 152, 0);
    public static final Color BTN_BACK = new Color(108, 117, 125);
    
    public static boolean isDarkMode() {
        return darkMode;
    }
    
    public static void setDarkMode(boolean dark) {
        darkMode = dark;
        SudokuConstants.refreshFromTheme();
    }
    
    public static void toggleDarkMode() {
        darkMode = !darkMode;
        SudokuConstants.refreshFromTheme();
    }
    
    // Getter methods untuk warna berdasarkan mode
    public static Color getBackground() {
        return darkMode ? DARK_BACKGROUND : LIGHT_BACKGROUND;
    }
    
    public static Color getPanel() {
        return darkMode ? DARK_PANEL : LIGHT_PANEL;
    }
    
    public static Color getCell() {
        return darkMode ? DARK_CELL : LIGHT_CELL;
    }
    
    public static Color getCellInitial() {
        return darkMode ? DARK_CELL_INITIAL : LIGHT_CELL_INITIAL;
    }
    
    public static Color getCellSolved() {
        return darkMode ? DARK_CELL_SOLVED : LIGHT_CELL_SOLVED;
    }
    
    public static Color getCellProcessing() {
        return darkMode ? DARK_CELL_PROCESSING : LIGHT_CELL_PROCESSING;
    }
    
    public static Color getCellTesting() {
        return darkMode ? DARK_CELL_TESTING : LIGHT_CELL_TESTING;
    }
    
    public static Color getCellBacktrack() {
        return darkMode ? DARK_CELL_BACKTRACK : LIGHT_CELL_BACKTRACK;
    }
    
    public static Color getText() {
        return darkMode ? DARK_TEXT : LIGHT_TEXT;
    }
    
    public static Color getTextSecondary() {
        return darkMode ? DARK_TEXT_SECONDARY : LIGHT_TEXT_SECONDARY;
    }
    
    public static Color getTextSolved() {
        return darkMode ? DARK_TEXT_SOLVED : LIGHT_TEXT_SOLVED;
    }
    
    public static Color getBorder() {
        return darkMode ? DARK_BORDER : LIGHT_BORDER;
    }
    
    public static Color getBorderThin() {
        return darkMode ? DARK_BORDER_THIN : LIGHT_BORDER_THIN;
    }
    
    public static Color getLogBackground() {
        return darkMode ? DARK_LOG_BG : LIGHT_LOG_BG;
    }
    
    public static Color getLogText() {
        return darkMode ? DARK_LOG_TEXT : LIGHT_LOG_TEXT;
    }
}

