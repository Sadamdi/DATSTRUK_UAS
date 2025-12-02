
/**
 * Kelas untuk algoritma Brute Force (Backtracking) Sudoku Solver
 * 
 * CARA KERJA ALGORITMA:
 * 1. Cari sel kosong pertama (dari kiri ke kanan, atas ke bawah)
 * 2. Coba tempatkan angka 1, lalu cek apakah valid
 * 3. Jika valid, simpan dan pindah ke sel berikutnya (rekursi)
 * 4. Jika tidak valid, coba angka berikutnya (2, 3, ..., 9)
 * 5. Jika semua angka tidak valid, backtrack ke sel sebelumnya
 * 6. Ulangi sampai semua 81 sel terisi
 */
public class SudokuSolver {
    
    private int[][] board;
    private SudokuValidator validator;
    private SolverCallback callback;
    private int stepCount = 0;
    private volatile boolean cancelled = false; // Flag untuk cancel
    private int lastLoggedStep = 0; // Untuk mengurangi log
    
    /**
     * Interface untuk callback saat solving (untuk animasi dan log)
     */
    public interface SolverCallback {
        void onCellProcessing(int row, int col);
        void onCellTesting(int row, int col, int num, boolean isValid);
        void onCellSolved(int row, int col, int num);
        void onBacktrack(int row, int col);
        void onLog(String message);
        void updateCell(int row, int col, int value, java.awt.Color color);
        void sleep(int milliseconds);
    }
    
    public SudokuSolver(int[][] board) {
        this.board = board;
        this.validator = new SudokuValidator(board);
    }
    
    /**
     * Set callback untuk animasi dan log
     */
    public void setCallback(SolverCallback callback) {
        this.callback = callback;
    }
    
    /**
     * Cancel proses solving
     */
    public void cancel() {
        this.cancelled = true;
    }
    
    /**
     * Mulai solving dengan animasi
     */
    public boolean solveWithAnimation() {
        stepCount = 0;
        cancelled = false;
        return solveSudokuWithAnimation(0, 0);
    }
    
    /**
     * Mulai solving tanpa animasi (lebih cepat)
     */
    public boolean solve() {
        cancelled = false;
        return solveSudoku(0, 0);
    }
    
    /**
     * Versi dengan animasi untuk visualisasi
     */
    private boolean solveSudokuWithAnimation(int row, int col) {
        if (cancelled) {
            return false; // Proses dibatalkan
        }
        
        if (row == SudokuConstants.GRID_SIZE) {
            return true; // Solusi ditemukan!
        }
        
        int nextRow = (col == SudokuConstants.GRID_SIZE - 1) ? row + 1 : row;
        int nextCol = (col == SudokuConstants.GRID_SIZE - 1) ? 0 : col + 1;
        
        if (board[row][col] != 0) {
            return solveSudokuWithAnimation(nextRow, nextCol);
        }
        
        stepCount++;
        
        // Highlight sel yang sedang diproses
        if (callback != null) {
            callback.onCellProcessing(row, col);
            callback.updateCell(row, col, 0, SudokuConstants.COLOR_CELL_PROCESSING);
            callback.onLog("📍 Step " + stepCount + ": Cek sel [" + row + "," + col + "]\n");
        }
        
        for (int num = 1; num <= 9; num++) {
            if (cancelled) {
                return false; // Proses dibatalkan
            }
            
            // Tampilkan angka yang sedang di-test
            if (callback != null) {
                callback.updateCell(row, col, num, SudokuConstants.COLOR_CELL_TESTING);
                callback.onLog("   → Coba angka " + num + "... ");
            }
            
            if (validator.isValidPlacement(row, col, num)) {
                board[row][col] = num;
                
                if (callback != null) {
                    callback.onCellTesting(row, col, num, true);
                    callback.onLog("✓ VALID\n");
                    callback.updateCell(row, col, num, SudokuConstants.COLOR_CELL_SOLVED);
                }
                
                if (solveSudokuWithAnimation(nextRow, nextCol)) {
                    return true;
                }
                
                // Backtrack
                if (callback != null) {
                    callback.onBacktrack(row, col);
                    callback.onLog("   ↩ BACKTRACK dari [" + row + "," + col + "] (angka " + num + " tidak mengarah ke solusi)\n");
                }
                board[row][col] = 0;
                
                if (callback != null) {
                    callback.updateCell(row, col, 0, SudokuConstants.COLOR_CELL_BACKTRACK);
                    callback.sleep(50); // Brief pause untuk show backtrack
                    callback.updateCell(row, col, 0, SudokuConstants.COLOR_CELL_PROCESSING);
                }
            } else {
                if (callback != null) {
                    callback.onCellTesting(row, col, num, false);
                    callback.onLog("✗ tidak valid\n");
                }
            }
        }
        
        if (callback != null) {
            callback.onLog("   ✗ Semua angka gagal untuk [" + row + "," + col + "], mundur...\n");
            callback.updateCell(row, col, 0, SudokuConstants.COLOR_CELL_DEFAULT);
        }
        
        return false;
    }
    
    /**
     * Versi tanpa animasi (lebih cepat)
     */
    private boolean solveSudoku(int row, int col) {
        if (cancelled) {
            return false; // Proses dibatalkan
        }
        
        if (row == SudokuConstants.GRID_SIZE) {
            return true;
        }
        
        int nextRow = (col == SudokuConstants.GRID_SIZE - 1) ? row + 1 : row;
        int nextCol = (col == SudokuConstants.GRID_SIZE - 1) ? 0 : col + 1;
        
        if (board[row][col] != 0) {
            return solveSudoku(nextRow, nextCol);
        }
        
        for (int num = 1; num <= 9; num++) {
            if (cancelled) {
                return false; // Proses dibatalkan
            }
            
            if (validator.isValidPlacement(row, col, num)) {
                board[row][col] = num;
                if (solveSudoku(nextRow, nextCol)) {
                    return true;
                }
                board[row][col] = 0;
            }
        }
        
        return false;
    }
    
    /**
     * Get step count
     */
    public int getStepCount() {
        return stepCount;
    }
}

