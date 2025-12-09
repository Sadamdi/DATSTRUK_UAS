package gui;

import java.awt.Color;

/**
 * Konstanta untuk aplikasi Sudoku Solver
 * Berisi semua nilai tetap seperti ukuran, warna, dll
 * 
 * Warna mengikuti ThemeManager (light/dark).
 */
public class SudokuConstants {
    
    // Ukuran grid
    public static final int GRID_SIZE = 9;      // Ukuran grid 9x9
    public static final int SUBGRID_SIZE = 3;    // Ukuran subgrid 3x3
    public static final int CELL_SIZE = 60;      // Ukuran pixel per sel
    
    // Warna untuk sel (akan disinkronkan dari ThemeManager)
    public static Color COLOR_BACKGROUND;
    public static Color COLOR_CELL_DEFAULT;
    public static Color COLOR_CELL_INITIAL;
    public static Color COLOR_CELL_SOLVED;
    public static Color COLOR_CELL_PROCESSING;
    public static Color COLOR_CELL_TESTING;
    public static Color COLOR_CELL_BACKTRACK;
    
    // Warna untuk teks
    public static Color COLOR_TEXT_INITIAL;
    public static Color COLOR_TEXT_SOLVED;
    
    // Warna untuk border
    public static Color COLOR_BORDER_THICK;
    public static Color COLOR_BORDER_THIN;
    
    // Warna untuk tombol (langsung ambil dari ThemeManager, sama untuk kedua mode)
    public static final Color COLOR_BUTTON_SOLVE = ThemeManager.BTN_SOLVE;
    public static final Color COLOR_BUTTON_RESET = ThemeManager.BTN_RESET;
    public static final Color COLOR_BUTTON_LOAD = ThemeManager.BTN_LOAD;
    
    // Delay animasi (dalam milliseconds)
    public static final int ANIMATION_DELAY_SLOW = 200;
    public static final int ANIMATION_DELAY_MEDIUM = 50;
    public static final int ANIMATION_DELAY_FAST = 10;
    public static final int ANIMATION_DELAY_ULTRA_FAST = 1;
    // Mode baru: SUPER ULTRA -> delay tetap 1ms tapi visual bisa di-throttle (misal setiap 10 langkah)
    public static final int ANIMATION_DELAY_SUPER_ULTRA_FAST = 1;
    
    static {
        refreshFromTheme();
    }
    
    /**
     * Sinkronkan warna dengan ThemeManager (dipanggil saat mode light/dark berubah)
     */
    public static void refreshFromTheme() {
        COLOR_BACKGROUND = ThemeManager.getBackground();
        COLOR_CELL_DEFAULT = ThemeManager.getCell();
        COLOR_CELL_INITIAL = ThemeManager.getCellInitial();
        COLOR_CELL_SOLVED = ThemeManager.getCellSolved();
        COLOR_CELL_PROCESSING = ThemeManager.getCellProcessing();
        COLOR_CELL_TESTING = ThemeManager.getCellTesting();
        COLOR_CELL_BACKTRACK = ThemeManager.getCellBacktrack();
        
        COLOR_TEXT_INITIAL = ThemeManager.getText();
        COLOR_TEXT_SOLVED = ThemeManager.getTextSolved();
        
        COLOR_BORDER_THICK = ThemeManager.getBorder();
        COLOR_BORDER_THIN = ThemeManager.getBorderThin();
    }
}

