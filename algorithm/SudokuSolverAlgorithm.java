package algorithm;

/**
 * Interface untuk algoritma Sudoku Solver
 * Memungkinkan berbagai algoritma (Brute Force, Harris Hawks, dll)
 */
public interface SudokuSolverAlgorithm {
    
    /**
     * Interface untuk callback saat solving (untuk animasi dan log)
     */
    interface SolverCallback {
        void onCellProcessing(int row, int col);
        void onCellTesting(int row, int col, int num, boolean isValid);
        void onCellSolved(int row, int col, int num);
        void onBacktrack(int row, int col);
        void onLog(String message);
        void updateCell(int row, int col, int value, java.awt.Color color);
        void sleep(int milliseconds);
    }
    
    /**
     * Set callback untuk animasi dan log
     */
    void setCallback(SolverCallback callback);
    
    /**
     * Cancel proses solving
     */
    void cancel();
    
    /**
     * Mulai solving dengan animasi
     */
    boolean solveWithAnimation();
    
    /**
     * Mulai solving tanpa animasi (lebih cepat)
     */
    boolean solve();
    
    /**
     * Get step count
     */
    int getStepCount();
    
    /**
     * Get nama algoritma
     */
    String getAlgorithmName();
}












