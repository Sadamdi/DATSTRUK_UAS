import java.awt.Color;

/**
 * Konstanta untuk aplikasi Sudoku Solver
 * Berisi semua nilai tetap seperti ukuran, warna, dll
 */
public class SudokuConstants {
    
    // Ukuran grid
    public static final int GRID_SIZE = 9;      // Ukuran grid 9x9
    public static final int SUBGRID_SIZE = 3;    // Ukuran subgrid 3x3
    public static final int CELL_SIZE = 60;      // Ukuran pixel per sel
    
    // Warna untuk sel
    public static final Color COLOR_BACKGROUND = new Color(250, 250, 252);
    public static final Color COLOR_CELL_DEFAULT = Color.WHITE;
    public static final Color COLOR_CELL_INITIAL = new Color(240, 240, 245);
    public static final Color COLOR_CELL_SOLVED = new Color(232, 245, 233);
    public static final Color COLOR_CELL_PROCESSING = new Color(255, 235, 59); // Kuning
    public static final Color COLOR_CELL_TESTING = new Color(255, 183, 77); // Orange
    public static final Color COLOR_CELL_BACKTRACK = new Color(255, 138, 128); // Merah muda untuk backtrack
    
    // Warna untuk teks
    public static final Color COLOR_TEXT_INITIAL = new Color(33, 37, 41);
    public static final Color COLOR_TEXT_SOLVED = new Color(46, 125, 50);
    
    // Warna untuk border
    public static final Color COLOR_BORDER_THICK = new Color(33, 37, 41);
    public static final Color COLOR_BORDER_THIN = new Color(200, 200, 200);
    
    // Warna untuk tombol
    public static final Color COLOR_BUTTON_SOLVE = new Color(76, 175, 80);
    public static final Color COLOR_BUTTON_RESET = new Color(244, 67, 54);
    public static final Color COLOR_BUTTON_LOAD = new Color(33, 150, 243);
    
    // Delay animasi (dalam milliseconds)
    public static final int ANIMATION_DELAY_SLOW = 200;
    public static final int ANIMATION_DELAY_MEDIUM = 50;
    public static final int ANIMATION_DELAY_FAST = 10;
    public static final int ANIMATION_DELAY_ULTRA_FAST = 1;
    public static final int ANIMATION_DELAY_SUPER_ULTRA_FAST = 0; // Tanpa delay
}

