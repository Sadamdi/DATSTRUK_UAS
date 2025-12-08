import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

/**
 * Main class - Entry point aplikasi Sudoku Solver
 * Menghubungkan semua komponen: GUI, Solver, Validator
 */
public class Main extends JFrame {
    
    private SudokuGUI gui;
    private int[][] board;
    private boolean[][] isInitialCell;
    private boolean isSolving = false;
    private boolean skipAnimation = false; // Mode cepat tanpa animasi
    
    // Puzzle contoh
    private static final int[][] EXAMPLE_PUZZLE = {
        {5, 3, 0, 0, 7, 0, 0, 0, 0},
        {6, 0, 0, 1, 9, 5, 0, 0, 0},
        {0, 9, 8, 0, 0, 0, 0, 6, 0},
        {8, 0, 0, 0, 6, 0, 0, 0, 3},
        {4, 0, 0, 8, 0, 3, 0, 0, 1},
        {7, 0, 0, 0, 2, 0, 0, 0, 6},
        {0, 6, 0, 0, 0, 0, 2, 8, 0},
        {0, 0, 0, 4, 1, 9, 0, 0, 5},
        {0, 0, 0, 0, 8, 0, 0, 7, 9}
    };
    
    public Main() {
        setTitle("Sudoku Solver - Algoritma Brute Force");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true); // Bisa di-resize
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen default
        
        // Inisialisasi board
        board = new int[SudokuConstants.GRID_SIZE][SudokuConstants.GRID_SIZE];
        isInitialCell = new boolean[SudokuConstants.GRID_SIZE][SudokuConstants.GRID_SIZE];
        
        // Setup GUI
        gui = new SudokuGUI(this);
        gui.setActionListener(new SudokuGUI.GUIActionListener() {
            @Override
            public void onLoadExample() {
                loadExamplePuzzle();
            }
            
            @Override
            public void onSolve() {
                solvePuzzle();
            }
            
            @Override
            public void onReset() {
                resetBoard();
            }
            
            @Override
            public void onSpeedChange(int newDelay) {
                gui.setAnimationDelay(newDelay);
            }
            
            @Override
            public void onToggleAnimation(boolean skip) {
                skipAnimation = skip;
            }
            
            @Override
            public void onAlgorithmChange(SudokuGUI.SolverAlgorithm algorithm) {
                // Main.java tidak menggunakan pilihan algoritma (legacy file)
                // Method ini ada untuk backward compatibility
            }
        });
        
        // Setup layout - Side by side: Grid kiri, Log kanan
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(SudokuConstants.COLOR_BACKGROUND);
        
        // Header di atas
        mainPanel.add(gui.createHeaderPanel(), BorderLayout.NORTH);
        
        // Content area: Grid di kiri, Log di kanan
        JPanel contentPanel = new JPanel(new BorderLayout(15, 0));
        contentPanel.setBackground(SudokuConstants.COLOR_BACKGROUND);
        
        // Panel kiri: Grid + Buttons
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setBackground(SudokuConstants.COLOR_BACKGROUND);
        leftPanel.add(gui.createGridPanel(), BorderLayout.CENTER);
        leftPanel.add(gui.createButtonPanel(), BorderLayout.SOUTH);
        
        // Panel kanan: Log
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(SudokuConstants.COLOR_BACKGROUND);
        rightPanel.add(gui.createLogPanel(), BorderLayout.CENTER);
        rightPanel.setPreferredSize(new Dimension(400, 0)); // Lebar log panel
        
        contentPanel.add(leftPanel, BorderLayout.CENTER);
        contentPanel.add(rightPanel, BorderLayout.EAST);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        add(mainPanel);
        setSize(1200, 800); // Ukuran default lebih besar
        setLocationRelativeTo(null);
    }
    
    /**
     * Load puzzle contoh
     */
    private void loadExamplePuzzle() {
        resetBoard();
        gui.loadExamplePuzzle(EXAMPLE_PUZZLE);
        
        // Update board array
        for (int row = 0; row < SudokuConstants.GRID_SIZE; row++) {
            for (int col = 0; col < SudokuConstants.GRID_SIZE; col++) {
                board[row][col] = EXAMPLE_PUZZLE[row][col];
                isInitialCell[row][col] = (EXAMPLE_PUZZLE[row][col] != 0);
            }
        }
        
        JOptionPane.showMessageDialog(this,
            "Puzzle contoh telah dimuat.\n" +
            "Klik 'Solve' untuk menjalankan algoritma Brute Force.",
            "Puzzle Dimuat",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Solve puzzle dengan animasi
     */
    private void solvePuzzle() {
        if (isSolving) {
            return;
        }
        
        // Baca input dari GUI
        if (!readBoardFromGUI()) {
            JOptionPane.showMessageDialog(this,
                "Input tidak valid! Pastikan hanya memasukkan angka 1-9.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validasi puzzle awal
        SudokuValidator validator = new SudokuValidator(board);
        if (!validator.isValidInitialBoard()) {
            JOptionPane.showMessageDialog(this,
                "Puzzle tidak valid!\nTerdapat angka yang sama pada baris, kolom, atau subgrid 3x3.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Reset log
        gui.getLogArea().setText("");
        gui.addLog("=== MEMULAI ALGORITMA BRUTE FORCE ===\n");
        gui.addLog("Mencari solusi untuk puzzle Sudoku...\n\n");
        
        isSolving = true;
        
        // Jalankan di thread terpisah
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // Buat copy board untuk solver
                int[][] boardCopy = new int[SudokuConstants.GRID_SIZE][SudokuConstants.GRID_SIZE];
                for (int i = 0; i < SudokuConstants.GRID_SIZE; i++) {
                    System.arraycopy(board[i], 0, boardCopy[i], 0, SudokuConstants.GRID_SIZE);
                }
                
                SudokuSolver solver = new SudokuSolver(boardCopy);
                
                long startTime = System.currentTimeMillis();
                boolean solved;
                
                if (skipAnimation) {
                    // Mode cepat: tanpa animasi, langsung solve
                    gui.addLog("Mode CEPAT: Menyelesaikan tanpa animasi...\n\n");
                    solved = solver.solve();
                    
                    // Update log dengan ringkasan
                    gui.addLog("=== PROSES SELESAI ===\n");
                    gui.addLog("Total langkah: " + solver.getStepCount() + "\n");
                } else {
                    // Mode dengan animasi
                    solver.setCallback(new SudokuSolver.SolverCallback() {
                        @Override
                        public void onCellProcessing(int row, int col) {
                            // Already handled in updateCell
                        }
                        
                        @Override
                        public void onCellTesting(int row, int col, int num, boolean isValid) {
                            // Already handled in updateCell
                        }
                        
                        @Override
                        public void onCellSolved(int row, int col, int num) {
                            // Already handled in updateCell
                        }
                        
                        @Override
                        public void onBacktrack(int row, int col) {
                            // Already handled in updateCell
                        }
                        
                        @Override
                        public void onLog(String message) {
                            gui.addLog(message);
                        }
                        
                        @Override
                        public void updateCell(int row, int col, int value, java.awt.Color color) {
                            gui.updateCell(row, col, value, color);
                            gui.sleep(gui.getAnimationDelay());
                        }
                        
                        @Override
                        public void sleep(int milliseconds) {
                            gui.sleep(milliseconds);
                        }
                    });
                    
                    solved = solver.solveWithAnimation();
                }
                
                long endTime = System.currentTimeMillis();
                
                // Update board dengan hasil
                for (int i = 0; i < SudokuConstants.GRID_SIZE; i++) {
                    System.arraycopy(boardCopy[i], 0, board[i], 0, SudokuConstants.GRID_SIZE);
                }
                
                final int stepCount = solver.getStepCount();
                final long executionTime = endTime - startTime;
                
                SwingUtilities.invokeLater(() -> {
                    if (solved) {
                        gui.updateSolution(isInitialCell, board);
                        gui.addLog("\n=== SOLUSI DITEMUKAN! ===\n");
                        gui.addLog("Total langkah: " + stepCount + "\n");
                        gui.addLog("Waktu eksekusi: " + executionTime + " ms\n");
                        JOptionPane.showMessageDialog(Main.this,
                            "✅ Sudoku berhasil diselesaikan!\n" +
                            "Total langkah: " + stepCount + "\n" +
                            "Waktu eksekusi: " + executionTime + " ms",
                            "Solusi Ditemukan",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        gui.resetBoardVisuals(isInitialCell, board);
                        gui.addLog("\n=== TIDAK ADA SOLUSI ===\n");
                        gui.addLog("Puzzle tidak dapat diselesaikan.\n");
                        JOptionPane.showMessageDialog(Main.this,
                            "❌ Tidak dapat menemukan solusi!\n" +
                            "Puzzle mungkin tidak valid atau tidak memiliki solusi.",
                            "Tidak Ada Solusi",
                            JOptionPane.WARNING_MESSAGE);
                    }
                    isSolving = false;
                });
                
                return solved;
            }
        };
        
        worker.execute();
    }
    
    /**
     * Baca input dari GUI ke array board
     */
    private boolean readBoardFromGUI() {
        JTextField[][] cells = gui.getCells();
        
        for (int row = 0; row < SudokuConstants.GRID_SIZE; row++) {
            for (int col = 0; col < SudokuConstants.GRID_SIZE; col++) {
                String text = cells[row][col].getText().trim();
                if (text.isEmpty()) {
                    board[row][col] = 0;
                    isInitialCell[row][col] = false;
                } else {
                    try {
                        int value = Integer.parseInt(text);
                        if (value < 1 || value > 9) {
                            return false;
                        }
                        board[row][col] = value;
                        isInitialCell[row][col] = true;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * Reset board
     */
    private void resetBoard() {
        gui.resetCells();
        gui.getLogArea().setText("");
        
        for (int row = 0; row < SudokuConstants.GRID_SIZE; row++) {
            for (int col = 0; col < SudokuConstants.GRID_SIZE; col++) {
                board[row][col] = 0;
                isInitialCell[row][col] = false;
            }
        }
        isSolving = false;
    }
    
    /**
     * Entry point - Sekarang memanggil MainMenu
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        
        SwingUtilities.invokeLater(() -> {
            new MainMenu().setVisible(true);
        });
    }
}
