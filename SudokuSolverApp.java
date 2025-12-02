import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Sudoku Solver App - Mode untuk menyelesaikan puzzle
 */
public class SudokuSolverApp extends JFrame {
    
    private SudokuGUI gui;
    private int[][] board;
    private boolean[][] isInitialCell;
    private boolean isSolving = false;
    private boolean skipAnimation = false;
    private SwingWorker<Boolean, Void> currentWorker; // Untuk stop proses
    
    // Puzzle akan di-generate secara random
    private java.util.Random random = new java.util.Random();
    private int puzzleCount = 0; // Counter untuk nomor puzzle
    
    public SudokuSolverApp() {
        setTitle("Sudoku Solver - Algoritma Brute Force");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
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
        
        // Panel untuk tombol dengan back button
        JPanel buttonContainer = new JPanel(new BorderLayout(0, 5));
        buttonContainer.setBackground(SudokuConstants.COLOR_BACKGROUND);
        
        // Tombol back
        JButton backButton = new JButton("← Kembali");
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backButton.setBackground(new Color(108, 117, 125));
        backButton.setForeground(Color.WHITE);
        backButton.setPreferredSize(new Dimension(120, 35));
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.setOpaque(true);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Kembali ke menu utama?",
                "Konfirmasi",
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                isSolving = false;
                if (currentWorker != null) {
                    currentWorker.cancel(true);
                }
                dispose();
                new MainMenu().setVisible(true);
            }
        });
        
        // Dark mode toggle
        JToggleButton darkModeBtn = new JToggleButton(ThemeManager.isDarkMode() ? "☀️ Light" : "🌙 Dark");
        darkModeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        darkModeBtn.setPreferredSize(new Dimension(100, 35));
        darkModeBtn.setFocusPainted(false);
        darkModeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        darkModeBtn.setSelected(ThemeManager.isDarkMode());
        darkModeBtn.addActionListener(e -> {
            ThemeManager.toggleDarkMode();
            darkModeBtn.setText(ThemeManager.isDarkMode() ? "☀️ Light" : "🌙 Dark");
            // Refresh diperlukan restart untuk tema penuh
            JOptionPane.showMessageDialog(this, 
                "Tema akan berubah sepenuhnya saat membuka jendela baru.",
                "Dark Mode", JOptionPane.INFORMATION_MESSAGE);
        });
        
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        backPanel.setBackground(SudokuConstants.COLOR_BACKGROUND);
        backPanel.add(backButton);
        backPanel.add(darkModeBtn);
        
        buttonContainer.add(backPanel, BorderLayout.NORTH);
        buttonContainer.add(gui.createButtonPanel(), BorderLayout.CENTER);
        
        leftPanel.add(buttonContainer, BorderLayout.SOUTH);
        
        // Panel kanan: Log
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(SudokuConstants.COLOR_BACKGROUND);
        rightPanel.add(gui.createLogPanel(), BorderLayout.CENTER);
        rightPanel.setPreferredSize(new Dimension(400, 0));
        
        contentPanel.add(leftPanel, BorderLayout.CENTER);
        contentPanel.add(rightPanel, BorderLayout.EAST);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        add(mainPanel);
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }
    
    private void loadExamplePuzzle() {
        resetBoard();
        
        // Generate puzzle random (40-55 sel kosong untuk variasi)
        int emptyCells = 40 + random.nextInt(16);
        int[][] selectedPuzzle = PuzzleGenerator.generatePuzzle(emptyCells);
        puzzleCount++;
        
        gui.loadExamplePuzzle(selectedPuzzle);
        
        for (int row = 0; row < SudokuConstants.GRID_SIZE; row++) {
            for (int col = 0; col < SudokuConstants.GRID_SIZE; col++) {
                board[row][col] = selectedPuzzle[row][col];
                isInitialCell[row][col] = (selectedPuzzle[row][col] != 0);
            }
        }
        
        // Tentukan tingkat kesulitan berdasarkan sel kosong
        String difficulty = "Easy";
        if (emptyCells >= 50) difficulty = "Hard";
        else if (emptyCells >= 45) difficulty = "Medium";
        
        JOptionPane.showMessageDialog(this,
            "Puzzle Random #" + puzzleCount + " telah dibuat!\n" +
            "Sel kosong: " + emptyCells + " (" + difficulty + ")\n" +
            "Klik 'Solve' untuk menjalankan algoritma Brute Force.",
            "Puzzle Baru",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void solvePuzzle() {
        if (isSolving) return;
        
        if (!readBoardFromGUI()) {
            JOptionPane.showMessageDialog(this,
                "Input tidak valid! Pastikan hanya memasukkan angka 1-9.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        SudokuValidator validator = new SudokuValidator(board);
        if (!validator.isValidInitialBoard()) {
            JOptionPane.showMessageDialog(this,
                "Puzzle tidak valid!\nTerdapat angka yang sama pada baris, kolom, atau subgrid 3x3.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        gui.getLogArea().setText("");
        gui.addLog("=== MEMULAI ALGORITMA BRUTE FORCE ===\n");
        gui.addLog("Mencari solusi untuk puzzle Sudoku...\n\n");
        
        isSolving = true;
        
        currentWorker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                int[][] boardCopy = new int[SudokuConstants.GRID_SIZE][SudokuConstants.GRID_SIZE];
                for (int i = 0; i < SudokuConstants.GRID_SIZE; i++) {
                    System.arraycopy(board[i], 0, boardCopy[i], 0, SudokuConstants.GRID_SIZE);
                }
                
                final SudokuSolver solver = new SudokuSolver(boardCopy);
                long startTime = System.currentTimeMillis();
                boolean solved;
                
                if (skipAnimation) {
                    gui.addLog("Mode CEPAT: Menyelesaikan tanpa animasi...\n\n");
                    solved = solver.solve();
                    gui.addLog("=== PROSES SELESAI ===\n");
                    gui.addLog("Total langkah: " + solver.getStepCount() + "\n");
                } else {
                    // Animasi dengan log proses brute force yang detail
                    // Delay dibaca real-time agar bisa diubah saat proses berjalan
                    
                    solver.setCallback(new SudokuSolver.SolverCallback() {
                        @Override
                        public void onCellProcessing(int row, int col) {}
                        @Override
                        public void onCellTesting(int row, int col, int num, boolean isValid) {}
                        @Override
                        public void onCellSolved(int row, int col, int num) {}
                        @Override
                        public void onBacktrack(int row, int col) {}
                        @Override
                        public void onLog(String message) {
                            if (!isCancelled()) {
                                // Tampilkan semua log proses brute force
                                SwingUtilities.invokeLater(() -> gui.addLog(message));
                            }
                        }
                        @Override
                        public void updateCell(int row, int col, int value, java.awt.Color color) {
                            if (!isCancelled()) {
                                // Selalu update sel untuk visualisasi
                                try {
                                    SwingUtilities.invokeAndWait(() -> {
                                        gui.updateCellDirect(row, col, value, color);
                                    });
                                } catch (Exception e) {
                                    SwingUtilities.invokeLater(() -> {
                                        gui.updateCellDirect(row, col, value, color);
                                    });
                                }
                                
                                // Sleep sesuai delay (bisa diubah real-time)
                                int currentDelay = gui.getAnimationDelay();
                                if (currentDelay > 0) {
                                    try {
                                        Thread.sleep(currentDelay);
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    }
                                }
                            }
                        }
                        @Override
                        public void sleep(int milliseconds) {
                            int currentDelay = gui.getAnimationDelay();
                            if (currentDelay > 0 && !isCancelled()) {
                                try {
                                    Thread.sleep(Math.min(milliseconds, currentDelay));
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            }
                        }
                    });
                    solved = solver.solveWithAnimation();
                }
                
                long endTime = System.currentTimeMillis();
                
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
                        JOptionPane.showMessageDialog(SudokuSolverApp.this,
                            "✅ Sudoku berhasil diselesaikan!\n" +
                            "Total langkah: " + stepCount + "\n" +
                            "Waktu eksekusi: " + executionTime + " ms",
                            "Solusi Ditemukan",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        gui.resetBoardVisuals(isInitialCell, board);
                        gui.addLog("\n=== TIDAK ADA SOLUSI ===\n");
                        gui.addLog("Puzzle tidak dapat diselesaikan.\n");
                        JOptionPane.showMessageDialog(SudokuSolverApp.this,
                            "❌ Tidak dapat menemukan solusi!",
                            "Tidak Ada Solusi",
                            JOptionPane.WARNING_MESSAGE);
                    }
                    isSolving = false;
                });
                
                return solved;
            }
        };
        
        currentWorker.execute();
    }
    
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
                        if (value < 1 || value > 9) return false;
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
    
    private void resetBoard() {
        // Stop proses yang sedang berjalan
        if (isSolving && currentWorker != null) {
            currentWorker.cancel(true);
            // Cancel juga solver jika ada
            gui.addLog("\n⚠️ Proses dihentikan oleh user\n");
        }
        
        gui.resetCells();
        gui.getLogArea().setText("");
        for (int row = 0; row < SudokuConstants.GRID_SIZE; row++) {
            for (int col = 0; col < SudokuConstants.GRID_SIZE; col++) {
                board[row][col] = 0;
                isInitialCell[row][col] = false;
            }
        }
        isSolving = false;
        currentWorker = null;
    }
}

