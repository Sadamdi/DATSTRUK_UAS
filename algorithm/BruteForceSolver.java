package algorithm;

import gui.SudokuConstants;

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
public class BruteForceSolver implements SudokuSolverAlgorithm {
    
    private int[][] board;
    private SudokuValidator validator;
    private SudokuSolverAlgorithm.SolverCallback callback;
    private int stepCount = 0;
    private volatile boolean cancelled = false;
    
    public BruteForceSolver(int[][] board) {
        this.board = board;
        this.validator = new SudokuValidator(board);
    }
    
    @Override
    public void setCallback(SolverCallback callback) {
        this.callback = callback;
    }
    
    @Override
    public void cancel() {
        this.cancelled = true;
    }
    
    @Override
    public boolean solveWithAnimation() {
        stepCount = 0;
        cancelled = false;
        return solveSudokuWithAnimation(0, 0);
    }
    
    @Override
    public boolean solve() {
        stepCount = 0;
        cancelled = false;
        return solveSudoku(0, 0);
    }
    
    @Override
    public int getStepCount() {
        return stepCount;
    }
    
    @Override
    public String getAlgorithmName() {
        return "Brute Force (Backtracking)";
    }
    
    /**
     * Versi dengan animasi untuk visualisasi
     */
    private boolean solveSudokuWithAnimation(int row, int col) {
        if (cancelled) {
            return false;
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
        
        // Proses Brute Force
        for (int num = 1; num <= 9; num++) {
            if (cancelled) {
                return false;
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
                    callback.sleep(50);
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
            return false;
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
                return false;
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
}
