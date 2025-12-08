import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

/**
 * Game Mode - Bermain Sudoku melawan komputer
 * Bot menggunakan algoritma Brute Force
 */
public class SudokuGameApp extends JFrame {
    
    private SudokuGUI gui;
    private int[][] playerBoard;
    private int[][] solution;
    private boolean[][] isInitialCell;
    private boolean[][] playerFilled;
    private boolean[][] botFilled;
    private boolean gameStarted = false;
    private boolean gameOver = false;
    private boolean isPlayerTurn = true; // true = player turn, false = bot turn
    private boolean showProcess = true;
    private boolean skipProcess = false;
    private int playerScore = 0;
    private int botScore = 0;
    private int lastFilledRow = -1;
    private int lastFilledCol = -1;
    
    // Tingkat kesulitan bot
    private String botDifficulty = "Medium"; // Easy, Medium, Hard
    private Random random = new Random();
    private SudokuGUI.SolverAlgorithm selectedAlgorithm = SudokuGUI.SolverAlgorithm.BRUTE_FORCE;
    
    // Komponen UI
    private JLabel scoreLabel;
    private JLabel statusLabel;
    private JLabel difficultyLabel;
    private JButton startButton;
    private JButton submitButton;
    private JButton backButton;
    private JCheckBox showProcessCheck;
    private JCheckBox skipProcessCheck;
    private JComboBox<String> difficultyCombo;
    
    public SudokuGameApp() {
        setTitle("Bermain Sudoku - Multi Algorithm");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        playerBoard = new int[SudokuConstants.GRID_SIZE][SudokuConstants.GRID_SIZE];
        solution = new int[SudokuConstants.GRID_SIZE][SudokuConstants.GRID_SIZE];
        isInitialCell = new boolean[SudokuConstants.GRID_SIZE][SudokuConstants.GRID_SIZE];
        playerFilled = new boolean[SudokuConstants.GRID_SIZE][SudokuConstants.GRID_SIZE];
        botFilled = new boolean[SudokuConstants.GRID_SIZE][SudokuConstants.GRID_SIZE];
        
        setupGUI();
    }
    
    private void setupGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(SudokuConstants.COLOR_BACKGROUND);
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Content: Grid kiri, Info kanan
        JPanel contentPanel = new JPanel(new BorderLayout(15, 0));
        contentPanel.setBackground(SudokuConstants.COLOR_BACKGROUND);
        
        // Panel kiri: Grid + Controls
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setBackground(SudokuConstants.COLOR_BACKGROUND);
        
        gui = new SudokuGUI(this);
        gui.setActionListener(new SudokuGUI.GUIActionListener() {
            @Override
            public void onLoadExample() {}
            @Override
            public void onSolve() {}
            @Override
            public void onReset() {
                resetGame();
            }
            @Override
            public void onSpeedChange(int newDelay) {}
            @Override
            public void onToggleAnimation(boolean skip) {}
            
            @Override
            public void onAlgorithmChange(SudokuGUI.SolverAlgorithm algorithm) {
                selectedAlgorithm = algorithm;
                String algoName = algorithm == SudokuGUI.SolverAlgorithm.BRUTE_FORCE 
                    ? "Brute Force" 
                    : "Harris Hawks";
                setTitle("Bermain Sudoku - Bot " + algoName);
                updateBotAlgorithmDescription();
            }
        });
        
        leftPanel.add(gui.createGridPanel(), BorderLayout.CENTER);
        leftPanel.add(createGameControls(), BorderLayout.SOUTH);
        
        // Panel kanan: Info & Log
        JPanel rightPanel = createInfoPanel();
        rightPanel.setPreferredSize(new Dimension(350, 0));
        
        contentPanel.add(leftPanel, BorderLayout.CENTER);
        contentPanel.add(rightPanel, BorderLayout.EAST);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }
    
    private JPanel createHeaderPanel() {
        JLabel titleLabel = new JLabel("BERMAIN SUDOKU", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(33, 37, 41));
        
        JLabel subtitleLabel = new JLabel("Lawan komputer dengan pilihan algoritma: Brute Force atau Harris Hawks", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(108, 117, 125));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(SudokuConstants.COLOR_BACKGROUND);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        return headerPanel;
    }
    
    private JPanel createGameControls() {
        // Gunakan BoxLayout vertikal agar semua tombol terlihat
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(SudokuConstants.COLOR_BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Baris 1: Tombol kembali dan mulai
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        row1.setBackground(SudokuConstants.COLOR_BACKGROUND);
        
        backButton = createStyledButton("← Kembali", new Color(108, 117, 125));
        backButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Kembali ke menu utama?",
                "Konfirmasi",
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new MainMenu().setVisible(true);
            }
        });
        
        // Dark mode toggle
        JToggleButton darkModeBtn = new JToggleButton(ThemeManager.isDarkMode() ? "☀️" : "🌙");
        darkModeBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        darkModeBtn.setPreferredSize(new Dimension(50, 40));
        darkModeBtn.setFocusPainted(false);
        darkModeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        darkModeBtn.setSelected(ThemeManager.isDarkMode());
        darkModeBtn.setToolTipText("Toggle Dark Mode");
        darkModeBtn.addActionListener(e -> {
            ThemeManager.toggleDarkMode();
            darkModeBtn.setText(ThemeManager.isDarkMode() ? "☀️" : "🌙");
            JOptionPane.showMessageDialog(this, 
                "Tema akan berubah sepenuhnya saat membuka jendela baru.",
                "Dark Mode", JOptionPane.INFORMATION_MESSAGE);
        });
        
        startButton = createStyledButton("🎮 Mulai", SudokuConstants.COLOR_BUTTON_SOLVE);
        startButton.addActionListener(e -> startGame());
        
        row1.add(backButton);
        row1.add(darkModeBtn);
        row1.add(startButton);
        
        // Baris 2: Tombol submit dan reset
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        row2.setBackground(SudokuConstants.COLOR_BACKGROUND);
        
        submitButton = createStyledButton("✅ Submit", new Color(76, 175, 80));
        submitButton.setEnabled(false);
        submitButton.addActionListener(e -> submitAnswer());
        
        JButton resetButton = createStyledButton("🔄 Reset", SudokuConstants.COLOR_BUTTON_RESET);
        resetButton.addActionListener(e -> resetGame());
        
        row2.add(submitButton);
        row2.add(resetButton);
        
        mainPanel.add(row1);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(row2);
        
        return mainPanel;
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(130, 40));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(SudokuConstants.COLOR_BACKGROUND);
        
        // Score Panel
        JPanel scorePanel = new JPanel(new GridLayout(3, 1, 5, 5));
        scorePanel.setBorder(new TitledBorder("Skor"));
        scorePanel.setBackground(SudokuConstants.COLOR_BACKGROUND);
        
        scoreLabel = new JLabel("<html><center><b>Player: 0</b><br>Bot: 0</center></html>");
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scorePanel.add(scoreLabel);
        
        statusLabel = new JLabel("Klik 'Mulai Permainan' untuk memulai");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setForeground(new Color(108, 117, 125));
        scorePanel.add(statusLabel);
        
        // Difficulty Panel
        JPanel difficultyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        difficultyPanel.setBorder(new TitledBorder("Tingkat Kesulitan Bot"));
        difficultyPanel.setBackground(SudokuConstants.COLOR_BACKGROUND);
        
        difficultyLabel = new JLabel("Bot: ");
        difficultyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        String[] difficulties = {"Easy", "Medium", "Hard"};
        difficultyCombo = new JComboBox<>(difficulties);
        difficultyCombo.setSelectedItem("Medium");
        difficultyCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        difficultyCombo.addActionListener(e -> {
            botDifficulty = (String) difficultyCombo.getSelectedItem();
            updateDifficultyDescription();
        });
        
        difficultyPanel.add(difficultyLabel);
        difficultyPanel.add(difficultyCombo);
        
        // Options
        JPanel optionsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        optionsPanel.setBorder(new TitledBorder("Opsi"));
        optionsPanel.setBackground(SudokuConstants.COLOR_BACKGROUND);
        
        showProcessCheck = new JCheckBox("Tampilkan Proses Bot", true);
        showProcessCheck.setBackground(SudokuConstants.COLOR_BACKGROUND);
        showProcessCheck.addActionListener(e -> showProcess = showProcessCheck.isSelected());
        
        skipProcessCheck = new JCheckBox("Skip Proses Bot", false);
        skipProcessCheck.setBackground(SudokuConstants.COLOR_BACKGROUND);
        skipProcessCheck.addActionListener(e -> skipProcess = skipProcessCheck.isSelected());
        
        optionsPanel.add(showProcessCheck);
        optionsPanel.add(skipProcessCheck);
        
        // Log Panel
        JPanel logPanel = gui.createLogPanel();
        logPanel.setPreferredSize(new Dimension(0, 300));
        
        // Gabungkan difficulty dan options
        JPanel topPanel = new JPanel(new BorderLayout(0, 5));
        topPanel.setBackground(SudokuConstants.COLOR_BACKGROUND);
        topPanel.add(scorePanel, BorderLayout.NORTH);
        topPanel.add(difficultyPanel, BorderLayout.CENTER);
        topPanel.add(optionsPanel, BorderLayout.SOUTH);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(logPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void startGame() {
        resetGame();
        gameStarted = true;
        gameOver = false;
        
        // Generate puzzle
        generatePuzzle();
        
        // Load puzzle ke GUI
        gui.loadExamplePuzzle(playerBoard);
        
        // Mark initial cells dan disable mereka
        JTextField[][] cells = gui.getCells();
        for (int row = 0; row < SudokuConstants.GRID_SIZE; row++) {
            for (int col = 0; col < SudokuConstants.GRID_SIZE; col++) {
                if (playerBoard[row][col] != 0) {
                    isInitialCell[row][col] = true;
                    cells[row][col].setEditable(false); // Disable sel awal
                }
            }
        }
        
        startButton.setEnabled(false);
        submitButton.setEnabled(true);
        isPlayerTurn = true;
        statusLabel.setText("Giliran Anda! Isi 1 sel kosong (bebas pilih), lalu klik Submit");
        statusLabel.setForeground(new Color(33, 150, 243));
        
        gui.getLogArea().setText("");
        gui.addLog("=== PERMAINAN DIMULAI ===\n");
        gui.addLog("Mode Turn-Based: Isi 1 sel per giliran\n");
        
        String algoName = selectedAlgorithm == SudokuGUI.SolverAlgorithm.BRUTE_FORCE 
            ? "Brute Force (Backtracking)" 
            : "Harris Hawks Optimization";
        gui.addLog("🤖 Bot menggunakan: " + algoName + "\n");
        gui.addLog("Giliran: PLAYER\n\n");
    }
    
    private void generatePuzzle() {
        // Generate puzzle random menggunakan PuzzleGenerator (lebih random dan valid)
        Random rand = new Random();
        int cellsToRemove = 40 + rand.nextInt(11); // 40-50 cells
        
        // Generate puzzle dengan solusi lengkap
        int[][] puzzle = PuzzleGenerator.generatePuzzle(cellsToRemove);
        
        // Generate solusi lengkap untuk puzzle ini
        int[][] fullSolution = new int[SudokuConstants.GRID_SIZE][SudokuConstants.GRID_SIZE];
        for (int i = 0; i < SudokuConstants.GRID_SIZE; i++) {
            System.arraycopy(puzzle[i], 0, fullSolution[i], 0, SudokuConstants.GRID_SIZE);
        }
        
        // Solve untuk mendapatkan solusi lengkap (gunakan Brute Force untuk generate solution cepat)
        BruteForceSolver solver = new BruteForceSolver(fullSolution);
        boolean solved = solver.solve();
        
        if (!solved) {
            // Fallback: generate ulang jika tidak bisa diselesaikan
            puzzle = PuzzleGenerator.generatePuzzle(cellsToRemove);
            for (int i = 0; i < SudokuConstants.GRID_SIZE; i++) {
                System.arraycopy(puzzle[i], 0, fullSolution[i], 0, SudokuConstants.GRID_SIZE);
            }
            BruteForceSolver solver2 = new BruteForceSolver(fullSolution);
            solver2.solve();
        }
        
        // Copy solusi lengkap dan puzzle awal
        for (int i = 0; i < SudokuConstants.GRID_SIZE; i++) {
            System.arraycopy(fullSolution[i], 0, solution[i], 0, SudokuConstants.GRID_SIZE);
            System.arraycopy(puzzle[i], 0, playerBoard[i], 0, SudokuConstants.GRID_SIZE);
        }
    }
    
    private void submitAnswer() {
        if (!gameStarted || gameOver || !isPlayerTurn) return;
        
        // Cari sel yang baru diisi user
        int newRow = -1, newCol = -1;
        JTextField[][] cells = gui.getCells();
        
        for (int row = 0; row < SudokuConstants.GRID_SIZE; row++) {
            for (int col = 0; col < SudokuConstants.GRID_SIZE; col++) {
                if (isInitialCell[row][col] || playerFilled[row][col] || botFilled[row][col]) {
                    continue; // Skip sel yang sudah terisi
                }
                
                String text = cells[row][col].getText().trim();
                if (!text.isEmpty()) {
                    newRow = row;
                    newCol = col;
                    break;
                }
            }
            if (newRow != -1) break;
        }
        
        if (newRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Isi 1 sel kosong terlebih dahulu!",
                "Belum Ada Input",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Baca nilai yang diisi
        String text = cells[newRow][newCol].getText().trim();
        int value;
        try {
            value = Integer.parseInt(text);
            if (value < 1 || value > 9) {
                highlightError(newRow, newCol, "Angka harus 1-9");
                return;
            }
        } catch (NumberFormatException e) {
            highlightError(newRow, newCol, "Input tidak valid");
            return;
        }
        
        // Validasi: cek apakah tidak bentrok (baris, kolom, subgrid)
        playerBoard[newRow][newCol] = value;
        SudokuValidator validator = new SudokuValidator(playerBoard);
        
        // Validasi akan otomatis skip sel (newRow, newCol) saat mengecek
        boolean isValidLocal = validator.isValidPlacement(newRow, newCol, value);
        
        // Cek apakah grid 3x3 yang sedang diisi tersisa ≤ 3 sel kosong
        int subgridEmptyCount = countEmptyCellsInSubgrid(newRow, newCol);
        boolean isValidSubgrid = true;
        
        if (subgridEmptyCount <= 3) {
            // Jika grid tersebut tersisa ≤ 3 sel kosong, cek apakah grid tersebut masih bisa diselesaikan
            isValidSubgrid = canSubgridBeSolved(newRow, newCol);
        }
        
        if (!isValidLocal) {
            // Salah karena bentrok lokal (baris/kolom/subgrid)
            highlightError(newRow, newCol, "Angka ini melanggar aturan Sudoku (bentrok dengan baris/kolom/subgrid)");
            playerBoard[newRow][newCol] = 0;
            cells[newRow][newCol].setText("");
            
            gui.addLog("❌ Player salah di [" + newRow + "," + newCol + "] = " + value + " (bentrok)\n");
            gui.addLog("Bot akan memperbaiki...\n");
            isPlayerTurn = false;
            submitButton.setEnabled(false);
            botFixCell(newRow, newCol);
            return;
        }
        
        if (!isValidSubgrid) {
            // Salah karena membuat grid 3x3 tidak bisa diselesaikan (hanya dicek jika grid tersisa ≤ 3 sel)
            highlightError(newRow, newCol, "Angka ini membuat grid 3x3 tidak bisa diselesaikan");
            playerBoard[newRow][newCol] = 0;
            cells[newRow][newCol].setText("");
            
            gui.addLog("❌ Player salah di [" + newRow + "," + newCol + "] = " + value + " (grid tidak valid)\n");
            gui.addLog("Bot akan memperbaiki...\n");
            isPlayerTurn = false;
            submitButton.setEnabled(false);
            botFixCell(newRow, newCol);
            return;
        }
        
        // Benar: tidak bentrok dan puzzle masih valid
        playerScore++;
        playerFilled[newRow][newCol] = true;
        cells[newRow][newCol].setEditable(false); // Disable sel yang sudah benar
        gui.updateCell(newRow, newCol, value, SudokuConstants.COLOR_CELL_SOLVED);
        gui.addLog("✅ Player benar di [" + newRow + "," + newCol + "] = " + value + " (tidak bentrok) (+1 poin)\n");
        updateScore();
        
        // Cek apakah puzzle selesai
        if (isPuzzleComplete()) {
            gameOver = true;
            statusLabel.setText("🎉 Puzzle Selesai! Skor Anda: " + playerScore + " | Bot: " + botScore);
            statusLabel.setForeground(new Color(76, 175, 80));
            submitButton.setEnabled(false);
            JOptionPane.showMessageDialog(this,
                "🎉 Puzzle selesai!\n" +
                "Skor Anda: " + playerScore + "\n" +
                "Skor Bot: " + botScore,
                "Puzzle Selesai",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Bot turn
        isPlayerTurn = false;
        submitButton.setEnabled(false);
        statusLabel.setText("Giliran Bot...");
        statusLabel.setForeground(new Color(255, 152, 0));
        botPlay();
    }
    
    private void highlightError(int row, int col) {
        highlightError(row, col, "Salah");
    }
    
    private void highlightError(int row, int col, String message) {
        JTextField[][] cells = gui.getCells();
        cells[row][col].setBackground(new Color(255, 200, 200)); // Merah muda
        cells[row][col].setForeground(new Color(200, 0, 0)); // Merah
        
        // Reset setelah 2 detik
        Timer timer = new Timer(2000, e -> {
            if (!isInitialCell[row][col] && !playerFilled[row][col] && !botFilled[row][col]) {
                cells[row][col].setBackground(SudokuConstants.COLOR_CELL_DEFAULT);
                cells[row][col].setForeground(SudokuConstants.COLOR_TEXT_INITIAL);
            }
        });
        timer.setRepeats(false);
        timer.start();
        
        statusLabel.setText("❌ " + message);
        statusLabel.setForeground(new Color(244, 67, 54));
    }
    
    private boolean isPuzzleComplete() {
        for (int row = 0; row < SudokuConstants.GRID_SIZE; row++) {
            for (int col = 0; col < SudokuConstants.GRID_SIZE; col++) {
                if (playerBoard[row][col] == 0) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Hitung jumlah sel kosong yang tersisa di grid 3x3 tertentu
     */
    private int countEmptyCellsInSubgrid(int row, int col) {
        int startRow = (row / SudokuConstants.SUBGRID_SIZE) * SudokuConstants.SUBGRID_SIZE;
        int startCol = (col / SudokuConstants.SUBGRID_SIZE) * SudokuConstants.SUBGRID_SIZE;
        
        int count = 0;
        for (int r = startRow; r < startRow + SudokuConstants.SUBGRID_SIZE; r++) {
            for (int c = startCol; c < startCol + SudokuConstants.SUBGRID_SIZE; c++) {
                if (playerBoard[r][c] == 0) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * Cek apakah grid 3x3 tertentu masih bisa diselesaikan
     * (cek apakah semua angka 1-9 bisa ditempatkan di grid tersebut)
     */
    private boolean canSubgridBeSolved(int row, int col) {
        int startRow = (row / SudokuConstants.SUBGRID_SIZE) * SudokuConstants.SUBGRID_SIZE;
        int startCol = (col / SudokuConstants.SUBGRID_SIZE) * SudokuConstants.SUBGRID_SIZE;
        
        // Buat copy board untuk testing
        int[][] testBoard = new int[SudokuConstants.GRID_SIZE][SudokuConstants.GRID_SIZE];
        for (int i = 0; i < SudokuConstants.GRID_SIZE; i++) {
            System.arraycopy(playerBoard[i], 0, testBoard[i], 0, SudokuConstants.GRID_SIZE);
        }
        
        // Cek apakah grid 3x3 ini masih bisa diisi dengan angka 1-9 tanpa bentrok
        // Kita cek apakah setiap angka 1-9 masih bisa ditempatkan di grid tersebut
        boolean[] usedNumbers = new boolean[10]; // 0-9, index 0 tidak dipakai
        
        // Hitung angka yang sudah ada di grid
        for (int r = startRow; r < startRow + SudokuConstants.SUBGRID_SIZE; r++) {
            for (int c = startCol; c < startCol + SudokuConstants.SUBGRID_SIZE; c++) {
                if (testBoard[r][c] != 0) {
                    usedNumbers[testBoard[r][c]] = true;
                }
            }
        }
        
        // Cek apakah setiap angka yang belum digunakan masih bisa ditempatkan
        SudokuValidator validator = new SudokuValidator(testBoard);
        for (int num = 1; num <= 9; num++) {
            if (!usedNumbers[num]) {
                // Cari sel kosong di grid ini yang bisa ditempati angka ini
                boolean canPlace = false;
                for (int r = startRow; r < startRow + SudokuConstants.SUBGRID_SIZE; r++) {
                    for (int c = startCol; c < startCol + SudokuConstants.SUBGRID_SIZE; c++) {
                        if (testBoard[r][c] == 0) {
                            // Cek apakah angka ini bisa ditempatkan di sel ini (cek baris dan kolom juga)
                            if (validator.isValidPlacement(r, c, num)) {
                                canPlace = true;
                                break;
                            }
                        }
                    }
                    if (canPlace) break;
                }
                
                // Jika ada angka yang tidak bisa ditempatkan di grid ini, grid tidak valid
                if (!canPlace) {
                    return false;
                }
            }
        }
        
        // Jika semua angka masih bisa ditempatkan, grid masih valid
        return true;
    }
    
    private void botFixCell(int wrongRow, int wrongCol) {
        // Bot benerin sel yang salah
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    Thread.sleep(1000); // Delay 1 detik
                } catch (InterruptedException e) {}
                
                int correctValue = solution[wrongRow][wrongCol];
                
                SwingUtilities.invokeLater(() -> {
                    playerBoard[wrongRow][wrongCol] = correctValue;
                    botFilled[wrongRow][wrongCol] = true;
                    
                    JTextField[][] cells = gui.getCells();
                    cells[wrongRow][wrongCol].setEditable(false); // Disable sel yang diisi bot
                    gui.updateCell(wrongRow, wrongCol, correctValue, new Color(255, 235, 59)); // Kuning untuk bot
                    botScore++;
                    gui.addLog("✅ Bot memperbaiki [" + wrongRow + "," + wrongCol + "] = " + correctValue + " (+1 poin)\n");
                    updateScore();
                    
                    // Cek apakah puzzle selesai
                    if (isPuzzleComplete()) {
                        gameOver = true;
                        statusLabel.setText("🎉 Puzzle Selesai! Skor Anda: " + playerScore + " | Bot: " + botScore);
                        statusLabel.setForeground(new Color(76, 175, 80));
                        JOptionPane.showMessageDialog(SudokuGameApp.this,
                            "🎉 Puzzle selesai!\n" +
                            "Skor Anda: " + playerScore + "\n" +
                            "Skor Bot: " + botScore,
                            "Puzzle Selesai",
                            JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    
                    // Bot play lagi
                    botPlay();
                });
                
                return null;
            }
        };
        
        worker.execute();
    }
    
    private void updateDifficultyDescription() {
        String desc = "";
        switch (botDifficulty) {
            case "Easy": desc = "Bot sering salah (50%)"; break;
            case "Medium": desc = "Bot kadang salah (30%)"; break;
            case "Hard": desc = "Bot tidak pernah salah"; break;
        }
        difficultyLabel.setText("Bot: " + desc);
    }
    
    private void updateBotAlgorithmDescription() {
        String algoName = selectedAlgorithm == SudokuGUI.SolverAlgorithm.BRUTE_FORCE 
            ? "Brute Force (Backtracking)" 
            : "Harris Hawks Optimization";
        gui.addLog("\n🤖 Bot akan menggunakan algoritma: " + algoName + "\n");
    }
    
    private int getBotValue(int row, int col) {
        int correctValue = solution[row][col];
        
        // Tentukan apakah bot akan salah berdasarkan tingkat kesulitan
        double errorChance = 0;
        switch (botDifficulty) {
            case "Easy": errorChance = 0.5; break;   // 50% salah
            case "Medium": errorChance = 0.3; break; // 30% salah
            case "Hard": errorChance = 0; break;     // 0% salah (tidak pernah salah)
        }
        
        if (random.nextDouble() < errorChance) {
            // Bot salah - pilih angka random yang salah tapi valid (tidak bentrok)
            java.util.List<Integer> validWrongNumbers = new java.util.ArrayList<>();
            for (int num = 1; num <= 9; num++) {
                if (num != correctValue) {
                    // Cek apakah angka ini tidak bentrok
                    playerBoard[row][col] = num;
                    SudokuValidator validator = new SudokuValidator(playerBoard);
                    if (validator.isValidPlacement(row, col, num)) {
                        validWrongNumbers.add(num);
                    }
                    playerBoard[row][col] = 0;
                }
            }
            
            if (!validWrongNumbers.isEmpty()) {
                // Pilih angka salah yang valid secara acak
                return validWrongNumbers.get(random.nextInt(validWrongNumbers.size()));
            }
        }
        
        // Bot benar
        return correctValue;
    }
    
    private void botPlay() {
        // Bot menggunakan brute force untuk mencari sel kosong berikutnya
        // Bot mengikuti urutan dari kiri atas ke kanan bawah (sesuai algoritma brute force)
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Bot cari sel kosong pertama dari kiri atas ke kanan bawah
                // Mengikuti algoritma brute force: dari [0,0] sampai [8,8]
                for (int row = 0; row < SudokuConstants.GRID_SIZE; row++) {
                    for (int col = 0; col < SudokuConstants.GRID_SIZE; col++) {
                        // Cari sel kosong yang belum diisi player atau bot
                        if (playerBoard[row][col] == 0 && !isInitialCell[row][col] && 
                            !playerFilled[row][col] && !botFilled[row][col]) {
                            
                            // Bot menggunakan brute force untuk mencari nilai
                            // Tingkat kesulitan menentukan apakah bot bisa salah
                            int botValue = getBotValue(row, col);
                            int correctValue = solution[row][col];
                            boolean botIsCorrect = (botValue == correctValue);
                            
                            final int finalRow = row;
                            final int finalCol = col;
                            final int finalBotValue = botValue;
                            final boolean finalBotIsCorrect = botIsCorrect;
                            
                            SwingUtilities.invokeLater(() -> {
                                JTextField[][] cells = gui.getCells();
                                
                                if (finalBotIsCorrect) {
                                    // Bot benar
                                    playerBoard[finalRow][finalCol] = finalBotValue;
                                    botFilled[finalRow][finalCol] = true;
                                    botScore++;
                                    
                                    cells[finalRow][finalCol].setEditable(false);
                                    
                                    String algoShort = selectedAlgorithm == SudokuGUI.SolverAlgorithm.BRUTE_FORCE 
                                        ? "BF" : "HHO";
                                    
                                    if (showProcess && !skipProcess) {
                                        gui.addLog("✅ Bot (" + algoShort + ") mengisi [" + finalRow + "," + finalCol + "] = " + finalBotValue + " (+1 poin)\n");
                                        gui.updateCell(finalRow, finalCol, finalBotValue, new Color(255, 235, 59));
                                    } else {
                                        gui.updateCell(finalRow, finalCol, finalBotValue, new Color(255, 235, 59));
                                        gui.addLog("✅ Bot (" + algoShort + ") mengisi [" + finalRow + "," + finalCol + "] = " + finalBotValue + " (+1 poin)\n");
                                    }
                                } else {
                                    // Bot salah - highlight merah sebentar
                                    gui.addLog("❌ Bot SALAH! mencoba [" + finalRow + "," + finalCol + "] = " + finalBotValue + " (seharusnya " + solution[finalRow][finalCol] + ")\n");
                                    gui.updateCell(finalRow, finalCol, finalBotValue, new Color(255, 200, 200));
                                    
                                    // Reset setelah delay
                                    Timer timer = new Timer(1000, evt -> {
                                        cells[finalRow][finalCol].setText("");
                                        cells[finalRow][finalCol].setBackground(SudokuConstants.COLOR_CELL_DEFAULT);
                                        gui.addLog("Giliran kembali ke PLAYER\n");
                                        
                                        // Player turn
                                        isPlayerTurn = true;
                                        submitButton.setEnabled(true);
                                        statusLabel.setText("Bot salah! Giliran Anda! Isi 1 sel kosong");
                                        statusLabel.setForeground(new Color(33, 150, 243));
                                    });
                                    timer.setRepeats(false);
                                    timer.start();
                                    return;
                                }
                                updateScore();
                                
                                // Cek apakah puzzle selesai
                                if (isPuzzleComplete()) {
                                    gameOver = true;
                                    statusLabel.setText("🎉 Puzzle Selesai! Skor Anda: " + playerScore + " | Bot: " + botScore);
                                    statusLabel.setForeground(new Color(76, 175, 80));
                                    JOptionPane.showMessageDialog(SudokuGameApp.this,
                                        "🎉 Puzzle selesai!\n" +
                                        "Skor Anda: " + playerScore + "\n" +
                                        "Skor Bot: " + botScore,
                                        "Puzzle Selesai",
                                        JOptionPane.INFORMATION_MESSAGE);
                                    return;
                                }
                                
                                // Player turn lagi
                                isPlayerTurn = true;
                                submitButton.setEnabled(true);
                                statusLabel.setText("Giliran Anda! Isi 1 sel kosong (bebas), lalu klik Submit");
                                statusLabel.setForeground(new Color(33, 150, 243));
                                gui.addLog("Giliran: PLAYER\n");
                            });
                            
                            if (showProcess && !skipProcess) {
                                Thread.sleep(500);
                            }
                            
                            return null; // Hanya isi 1 sel per turn
                        }
                    }
                }
                return null;
            }
        };
        
        worker.execute();
    }
    
    
    
    private void updateScore() {
        scoreLabel.setText("<html><center><b>Player: " + playerScore + "</b><br>Bot: " + botScore + "</center></html>");
    }
    
    private void resetGame() {
        gui.resetCells();
        gui.getLogArea().setText("");
        
        // Enable semua sel lagi
        JTextField[][] cells = gui.getCells();
        for (int row = 0; row < SudokuConstants.GRID_SIZE; row++) {
            for (int col = 0; col < SudokuConstants.GRID_SIZE; col++) {
                cells[row][col].setEditable(true);
                playerBoard[row][col] = 0;
                solution[row][col] = 0;
                isInitialCell[row][col] = false;
                playerFilled[row][col] = false;
                botFilled[row][col] = false;
            }
        }
        
        gameStarted = false;
        gameOver = false;
        isPlayerTurn = true;
        playerScore = 0;
        botScore = 0;
        startButton.setEnabled(true);
        submitButton.setEnabled(false);
        statusLabel.setText("Klik 'Mulai Permainan' untuk memulai");
        statusLabel.setForeground(new Color(108, 117, 125));
        updateScore();
    }
}

