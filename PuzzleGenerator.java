import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Generator untuk membuat puzzle Sudoku secara random
 * Menggunakan algoritma Brute Force untuk validasi
 */
public class PuzzleGenerator {
    
    private static final Random random = new Random();
    
    /**
     * Generate puzzle random dengan jumlah sel kosong tertentu
     * @param emptyCells jumlah sel yang dikosongkan (semakin banyak semakin sulit)
     * @return puzzle 9x9
     */
    public static int[][] generatePuzzle(int emptyCells) {
        // Mulai dengan board kosong
        int[][] board = new int[9][9];
        
        // Generate solusi lengkap menggunakan brute force
        generateSolution(board, 0, 0);
        
        // Hapus beberapa sel secara random
        int removed = 0;
        while (removed < emptyCells) {
            int row = random.nextInt(9);
            int col = random.nextInt(9);
            
            if (board[row][col] != 0) {
                board[row][col] = 0;
                removed++;
            }
        }
        
        return board;
    }
    
    /**
     * Generate solusi lengkap dengan brute force + randomization
     */
    private static boolean generateSolution(int[][] board, int row, int col) {
        if (row == 9) {
            return true; // Selesai
        }
        
        int nextRow = (col == 8) ? row + 1 : row;
        int nextCol = (col == 8) ? 0 : col + 1;
        
        if (board[row][col] != 0) {
            return generateSolution(board, nextRow, nextCol);
        }
        
        // Shuffle angka 1-9 untuk randomness
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);
        
        for (int num : numbers) {
            if (isValid(board, row, col, num)) {
                board[row][col] = num;
                if (generateSolution(board, nextRow, nextCol)) {
                    return true;
                }
                board[row][col] = 0;
            }
        }
        
        return false;
    }
    
    /**
     * Cek validitas penempatan
     */
    private static boolean isValid(int[][] board, int row, int col, int num) {
        // Cek baris
        for (int c = 0; c < 9; c++) {
            if (board[row][c] == num) return false;
        }
        
        // Cek kolom
        for (int r = 0; r < 9; r++) {
            if (board[r][col] == num) return false;
        }
        
        // Cek subgrid 3x3
        int startRow = (row / 3) * 3;
        int startCol = (col / 3) * 3;
        for (int r = startRow; r < startRow + 3; r++) {
            for (int c = startCol; c < startCol + 3; c++) {
                if (board[r][c] == num) return false;
            }
        }
        
        return true;
    }
    
    /**
     * Generate banyak puzzle sekaligus
     * @param count jumlah puzzle
     * @param minEmpty minimum sel kosong
     * @param maxEmpty maximum sel kosong
     * @return array of puzzles
     */
    public static int[][][] generateMultiplePuzzles(int count, int minEmpty, int maxEmpty) {
        int[][][] puzzles = new int[count][9][9];
        
        for (int i = 0; i < count; i++) {
            int emptyCells = minEmpty + random.nextInt(maxEmpty - minEmpty + 1);
            puzzles[i] = generatePuzzle(emptyCells);
        }
        
        return puzzles;
    }
}

