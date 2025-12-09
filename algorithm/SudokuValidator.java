package algorithm;

import gui.SudokuConstants;

/**
 * Kelas untuk validasi aturan Sudoku
 * Mengecek apakah penempatan angka valid sesuai aturan:
 * - Tidak ada angka yang sama di baris
 * - Tidak ada angka yang sama di kolom
 * - Tidak ada angka yang sama di subgrid 3x3
 */
public class SudokuValidator {
    
    private int[][] board;
    
    public SudokuValidator(int[][] board) {
        this.board = board;
    }
    
    /**
     * Cek apakah angka boleh ditempatkan di posisi (row, col)
     * Harus memenuhi 3 syarat: baris, kolom, dan subgrid
     * Skip sel (row, col) saat validasi
     */
    public boolean isValidPlacement(int row, int col, int num) {
        return isValidInRow(row, num, col) && 
               isValidInColumn(col, num, row) && 
               isValidInSubgrid(row, col, num, row, col);
    }
    
    /**
     * Cek apakah ada angka yang sama di baris (skip sel tertentu)
     */
    public boolean isValidInRow(int row, int num, int skipCol) {
        for (int col = 0; col < SudokuConstants.GRID_SIZE; col++) {
            if (col == skipCol) continue; // Skip sel yang sedang dicek
            if (board[row][col] == num) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Cek apakah ada angka yang sama di baris
     */
    public boolean isValidInRow(int row, int num) {
        return isValidInRow(row, num, -1); // -1 berarti tidak skip sel apapun
    }
    
    /**
     * Cek apakah ada angka yang sama di kolom (skip sel tertentu)
     */
    public boolean isValidInColumn(int col, int num, int skipRow) {
        for (int row = 0; row < SudokuConstants.GRID_SIZE; row++) {
            if (row == skipRow) continue; // Skip sel yang sedang dicek
            if (board[row][col] == num) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Cek apakah ada angka yang sama di kolom
     */
    public boolean isValidInColumn(int col, int num) {
        return isValidInColumn(col, num, -1); // -1 berarti tidak skip sel apapun
    }
    
    /**
     * Cek apakah ada angka yang sama di kotak 3x3 (skip sel tertentu)
     * 
     * PENJELASAN:
     * Sudoku punya 9 kotak 3x3. Setiap kotak punya posisi awal tertentu.
     * 
     * Contoh: Jika kita di posisi (4, 5):
     * - row/3 = 4/3 = 1 (integer division)
     * - 1 * 3 = 3 -> kotak 3x3 dimulai dari baris 3
     * - col/3 = 5/3 = 1
     * - 1 * 3 = 3 -> kotak 3x3 dimulai dari kolom 3
     * - Jadi kotak 3x3-nya adalah dari (3,3) sampai (5,5)
     */
    public boolean isValidInSubgrid(int row, int col, int num, int skipRow, int skipCol) {
        // Hitung posisi awal kotak 3x3
        int startRow = (row / SudokuConstants.SUBGRID_SIZE) * SudokuConstants.SUBGRID_SIZE;
        int startCol = (col / SudokuConstants.SUBGRID_SIZE) * SudokuConstants.SUBGRID_SIZE;
        
        // Loop cek semua 9 sel dalam kotak 3x3
        for (int r = startRow; r < startRow + SudokuConstants.SUBGRID_SIZE; r++) {
            for (int c = startCol; c < startCol + SudokuConstants.SUBGRID_SIZE; c++) {
                if (r == skipRow && c == skipCol) continue; // Skip sel yang sedang dicek
                if (board[r][c] == num) {
                    return false; // Ketemu angka yang sama, tidak valid
                }
            }
        }
        return true; // Tidak ada angka yang sama, valid
    }
    
    /**
     * Cek apakah ada angka yang sama di kotak 3x3
     */
    public boolean isValidInSubgrid(int row, int col, int num) {
        return isValidInSubgrid(row, col, num, -1, -1); // -1 berarti tidak skip sel apapun
    }
    
    /**
     * Validasi apakah board awal valid (tidak ada konflik)
     * Mengecek semua sel yang sudah terisi
     */
    public boolean isValidInitialBoard() {
        for (int row = 0; row < SudokuConstants.GRID_SIZE; row++) {
            for (int col = 0; col < SudokuConstants.GRID_SIZE; col++) {
                if (board[row][col] != 0) {
                    int num = board[row][col];
                    board[row][col] = 0; // Temporarily remove for validation
                    if (!isValidPlacement(row, col, num)) {
                        board[row][col] = num; // Restore
                        return false;
                    }
                    board[row][col] = num; // Restore
                }
            }
        }
        return true;
    }
}





