import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.JComboBox;

/**
 * Kelas untuk komponen GUI Sudoku Solver
 * Menangani semua tampilan: grid, tombol, log panel
 */
public class SudokuGUI {
    
    // Enum untuk algoritma solver
    public enum SolverAlgorithm {
        BRUTE_FORCE("Brute Force (Backtracking)"),
        HARRIS_HAWKS("Harris Hawks Optimization");
        
        private final String displayName;
        
        SolverAlgorithm(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    private JFrame frame;
    private JTextField[][] cells;
    private JTextArea logArea;
    private JButton speedButton;
    private JButton skipButton;
    private JComboBox<SolverAlgorithm> algorithmCombo;
    private int animationDelay = SudokuConstants.ANIMATION_DELAY_MEDIUM;
    // Flag lokal hanya untuk mencegah double-klik saat skip dipicu
    private boolean skipAnimation = false;
    
    // Reset flag skip animation (dipanggil saat proses baru dimulai)
    public void resetSkipAnimation() {
        skipAnimation = false;
        if (skipButton != null) {
            skipButton.setEnabled(true);
            skipButton.setText("⚡ Skip Animasi");
            skipButton.setBackground(ThemeManager.BTN_SKIP);
        }
    }
    // Untuk mode kecepatan tertinggi: update visual bisa di-throttle (mis. setiap 10 langkah)
    private boolean throttleVisualUpdates = false;
    
    // Callback untuk interaksi
    public interface GUIActionListener {
        void onLoadExample();
        void onSolve();
        void onReset();
        void onSpeedChange(int newDelay);
        void onToggleAnimation(boolean skip);
        void onAlgorithmChange(SolverAlgorithm algorithm);
    }
    
    private GUIActionListener actionListener;
    
    public SudokuGUI(JFrame frame) {
        this.frame = frame;
        this.cells = new JTextField[SudokuConstants.GRID_SIZE][SudokuConstants.GRID_SIZE];
    }
    
    public void setActionListener(GUIActionListener listener) {
        this.actionListener = listener;
    }
    
    /**
     * Membuat panel header dengan judul
     */
    public JPanel createHeaderPanel() {
        JLabel titleLabel = new JLabel("SUDOKU SOLVER", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(33, 37, 41));
        
        JLabel subtitleLabel = new JLabel("Algoritma Brute Force (Backtracking)", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(108, 117, 125));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(SudokuConstants.COLOR_BACKGROUND);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        return headerPanel;
    }
    
    /**
     * Membuat panel grid 9x9
     */
    public JPanel createGridPanel() {
        JPanel gridPanel = new JPanel(new GridLayout(SudokuConstants.GRID_SIZE, SudokuConstants.GRID_SIZE, 0, 0));
        gridPanel.setPreferredSize(new Dimension(SudokuConstants.CELL_SIZE * SudokuConstants.GRID_SIZE, SudokuConstants.CELL_SIZE * SudokuConstants.GRID_SIZE));
        gridPanel.setBorder(BorderFactory.createLineBorder(SudokuConstants.COLOR_BORDER_THICK, 3));
        
        for (int row = 0; row < SudokuConstants.GRID_SIZE; row++) {
            for (int col = 0; col < SudokuConstants.GRID_SIZE; col++) {
                cells[row][col] = createCell(row, col);
                gridPanel.add(cells[row][col]);
            }
        }
        
        JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapperPanel.setBackground(SudokuConstants.COLOR_BACKGROUND);
        wrapperPanel.add(gridPanel);
        
        return wrapperPanel;
    }
    
    /**
     * Membuat satu sel (JTextField) dengan validasi input
     */
    private JTextField createCell(int row, int col) {
        JTextField cell = new JTextField();
        cell.setHorizontalAlignment(JTextField.CENTER);
        cell.setFont(new Font("Consolas", Font.BOLD, 24));
        cell.setBackground(SudokuConstants.COLOR_CELL_DEFAULT);
        cell.setForeground(SudokuConstants.COLOR_TEXT_INITIAL);
        
        // Border lebih tebal untuk memisahkan subgrid 3x3
        int top = (row % SudokuConstants.SUBGRID_SIZE == 0) ? 2 : 1;
        int left = (col % SudokuConstants.SUBGRID_SIZE == 0) ? 2 : 1;
        int bottom = (row == SudokuConstants.GRID_SIZE - 1) ? 2 : 1;
        int right = (col == SudokuConstants.GRID_SIZE - 1) ? 2 : 1;
        
        cell.setBorder(BorderFactory.createMatteBorder(top, left, bottom, right, SudokuConstants.COLOR_BORDER_THIN));
        
        // Validasi input: hanya boleh angka 1-9, maksimal 1 digit
        ((AbstractDocument) cell.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) 
                    throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String newText = currentText.substring(0, offset) + text + currentText.substring(offset + length);
                
                if (newText.isEmpty() || (newText.length() == 1 && newText.matches("[1-9]"))) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
            
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) 
                    throws BadLocationException {
                replace(fb, offset, 0, string, attr);
            }
        });
        
        return cell;
    }
    
    /**
     * Membuat panel tombol
     */
    public JPanel createButtonPanel() {
        // Gunakan BoxLayout vertikal agar semua tombol terlihat
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(SudokuConstants.COLOR_BACKGROUND);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        // Panel untuk tombol utama (baris 1)
        JPanel mainButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        mainButtonsPanel.setBackground(SudokuConstants.COLOR_BACKGROUND);
        
        JButton loadButton = createStyledButton("📥 Load Contoh", SudokuConstants.COLOR_BUTTON_LOAD);
        loadButton.addActionListener(e -> {
            if (actionListener != null) actionListener.onLoadExample();
        });
        
        JButton solveButton = createStyledButton("🔍 Solve", SudokuConstants.COLOR_BUTTON_SOLVE);
        solveButton.addActionListener(e -> {
            if (actionListener != null) actionListener.onSolve();
        });
        
        JButton resetButton = createStyledButton("🗑️ Reset", SudokuConstants.COLOR_BUTTON_RESET);
        resetButton.addActionListener(e -> {
            if (actionListener != null) actionListener.onReset();
        });
        
        mainButtonsPanel.add(loadButton);
        mainButtonsPanel.add(solveButton);
        mainButtonsPanel.add(resetButton);
        
        // Panel untuk pilihan algoritma (baris 2)
        JPanel algorithmPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        algorithmPanel.setBackground(SudokuConstants.COLOR_BACKGROUND);
        
        JLabel algoLabel = new JLabel("Algoritma: ");
        algoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        algorithmCombo = new JComboBox<>(SolverAlgorithm.values());
        algorithmCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        algorithmCombo.setPreferredSize(new Dimension(220, 30));
        algorithmCombo.addActionListener(e -> {
            if (actionListener != null) {
                actionListener.onAlgorithmChange((SolverAlgorithm) algorithmCombo.getSelectedItem());
            }
        });
        
        algorithmPanel.add(algoLabel);
        algorithmPanel.add(algorithmCombo);
        
        // Panel untuk kontrol kecepatan (baris 3)
        JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        speedPanel.setBackground(SudokuConstants.COLOR_BACKGROUND);
        
        JLabel speedLabel = new JLabel("Kecepatan: ");
        speedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        speedButton = new JButton("⚡ " + getSpeedLabel());
        speedButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        speedButton.setPreferredSize(new Dimension(120, 30));
        speedButton.addActionListener(e -> {
            // Toggle kecepatan: Lambat -> Sedang -> Cepat -> Ultra Fast -> Super Ultra Fast -> Lambat
            if (animationDelay == SudokuConstants.ANIMATION_DELAY_SLOW) {
                animationDelay = SudokuConstants.ANIMATION_DELAY_MEDIUM;
            } else if (animationDelay == SudokuConstants.ANIMATION_DELAY_MEDIUM) {
                animationDelay = SudokuConstants.ANIMATION_DELAY_FAST;
            } else if (animationDelay == SudokuConstants.ANIMATION_DELAY_FAST) {
                animationDelay = SudokuConstants.ANIMATION_DELAY_ULTRA_FAST;
            } else if (animationDelay == SudokuConstants.ANIMATION_DELAY_ULTRA_FAST && !throttleVisualUpdates) {
                // Pindah ke mode SUPER ULTRA: delay sama (1ms) tapi visual di-throttle
                animationDelay = SudokuConstants.ANIMATION_DELAY_SUPER_ULTRA_FAST;
                throttleVisualUpdates = true;
            } else {
                // Kembali ke mode LAMBAT dan matikan throttle visual
                animationDelay = SudokuConstants.ANIMATION_DELAY_SLOW;
                throttleVisualUpdates = false;
            }
            // Jika saat ini bukan mode SUPER ULTRA, pastikan throttle mati
            if (animationDelay != SudokuConstants.ANIMATION_DELAY_SUPER_ULTRA_FAST) {
                throttleVisualUpdates = false;
            }
            speedButton.setText("⚡ " + getSpeedLabel());
            if (actionListener != null) actionListener.onSpeedChange(animationDelay);
        });
        
        // Tombol Skip Animation (Mode Cepat - sekali klik untuk auto-skip animasi saat proses berjalan)
        skipButton = new JButton("⚡ Skip Animasi");
        skipButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        skipButton.setBackground(new Color(255, 152, 0)); // Orange
        skipButton.setForeground(Color.WHITE);
        skipButton.setFocusPainted(false);
        skipButton.setBorderPainted(false);
        skipButton.setPreferredSize(new Dimension(130, 30));
        skipButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        skipButton.addActionListener(e -> {
            // Sekali klik: kirim sinyal ke solver untuk langsung skip animasi.
            // Tombol dinonaktifkan sampai proses selesai / di-reset.
            if (skipAnimation) return; // sudah pernah dipicu
            skipAnimation = true;
            skipButton.setEnabled(false);
            skipButton.setText("⏩ Skipping...");
            if (actionListener != null) actionListener.onToggleAnimation(true);
        });
        
        speedPanel.add(speedLabel);
        speedPanel.add(speedButton);
        speedPanel.add(skipButton);
        
        buttonPanel.add(mainButtonsPanel);
        buttonPanel.add(Box.createVerticalStrut(5));
        buttonPanel.add(algorithmPanel);
        buttonPanel.add(Box.createVerticalStrut(5));
        buttonPanel.add(speedPanel);
        
        return buttonPanel;
    }
    
    private String getSpeedLabel() {
        if (animationDelay == SudokuConstants.ANIMATION_DELAY_SLOW) return "Lambat";
        if (animationDelay == SudokuConstants.ANIMATION_DELAY_MEDIUM) return "Sedang";
        if (animationDelay == SudokuConstants.ANIMATION_DELAY_FAST) return "Cepat";
        if (animationDelay == SudokuConstants.ANIMATION_DELAY_ULTRA_FAST && !throttleVisualUpdates) return "Ultra Fast";
        if (animationDelay == SudokuConstants.ANIMATION_DELAY_SUPER_ULTRA_FAST && throttleVisualUpdates) return "Super Ultra Fast";
        return "Custom";
    }
    
    /**
     * Membuat tombol dengan styling konsisten
     */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(140, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    /**
     * Membuat panel log
     */
    public JPanel createLogPanel() {
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBackground(SudokuConstants.COLOR_BACKGROUND);
        logPanel.setBorder(BorderFactory.createTitledBorder("Log Proses"));
        logPanel.setPreferredSize(new Dimension(400, 0)); // Lebar tetap, tinggi fleksibel
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(0, 255, 0));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        logPanel.add(scrollPane, BorderLayout.CENTER);
        
        return logPanel;
    }
    
    // Getter dan setter
    public JTextField[][] getCells() {
        return cells;
    }
    
    public JTextArea getLogArea() {
        return logArea;
    }
    
    public JButton getSkipButton() {
        return skipButton;
    }
    
    public int getAnimationDelay() {
        return animationDelay;
    }
    
    public void setAnimationDelay(int delay) {
        this.animationDelay = delay;
    }
    
    public boolean isThrottleVisualUpdates() {
        return throttleVisualUpdates;
    }
    
    public SolverAlgorithm getSelectedAlgorithm() {
        if (algorithmCombo != null) {
            return (SolverAlgorithm) algorithmCombo.getSelectedItem();
        }
        return SolverAlgorithm.BRUTE_FORCE;
    }
    
    public void setSelectedAlgorithm(SolverAlgorithm algorithm) {
        if (algorithmCombo != null) {
            algorithmCombo.setSelectedItem(algorithm);
        }
    }
    
    /**
     * Update sel dengan warna tertentu (dengan invokeLater)
     */
    public void updateCell(int row, int col, int value, Color bgColor) {
        SwingUtilities.invokeLater(() -> {
            updateCellDirect(row, col, value, bgColor);
        });
    }
    
    /**
     * Update sel langsung tanpa invokeLater (untuk optimasi)
     */
    public void updateCellDirect(int row, int col, int value, Color bgColor) {
        if (value == 0) {
            cells[row][col].setText("");
        } else {
            cells[row][col].setText(String.valueOf(value));
        }
        cells[row][col].setBackground(bgColor);
        // Jangan force repaint setiap kali - biarkan Swing handle sendiri untuk performa lebih baik
    }
    
    /**
     * Tambahkan log message
     */
    public void addLog(String message) {
        SwingUtilities.invokeLater(() -> {
            if (logArea != null) {
                logArea.append(message);
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }
        });
    }
    
    /**
     * Sleep untuk animasi
     */
    public void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Load puzzle contoh ke grid
     */
    public void loadExamplePuzzle(int[][] puzzle) {
        for (int row = 0; row < SudokuConstants.GRID_SIZE; row++) {
            for (int col = 0; col < SudokuConstants.GRID_SIZE; col++) {
                if (puzzle[row][col] != 0) {
                    cells[row][col].setText(String.valueOf(puzzle[row][col]));
                    cells[row][col].setBackground(SudokuConstants.COLOR_CELL_INITIAL);
                } else {
                    cells[row][col].setText("");
                    cells[row][col].setBackground(SudokuConstants.COLOR_CELL_DEFAULT);
                }
            }
        }
    }
    
    /**
     * Reset semua sel
     */
    public void resetCells() {
        for (int row = 0; row < SudokuConstants.GRID_SIZE; row++) {
            for (int col = 0; col < SudokuConstants.GRID_SIZE; col++) {
                cells[row][col].setText("");
                cells[row][col].setBackground(SudokuConstants.COLOR_CELL_DEFAULT);
                cells[row][col].setForeground(SudokuConstants.COLOR_TEXT_INITIAL);
            }
        }
    }
    
    /**
     * Update tampilan setelah solusi ditemukan
     */
    public void updateSolution(boolean[][] isInitialCell, int[][] board) {
        for (int row = 0; row < SudokuConstants.GRID_SIZE; row++) {
            for (int col = 0; col < SudokuConstants.GRID_SIZE; col++) {
                cells[row][col].setText(String.valueOf(board[row][col]));
                
                if (!isInitialCell[row][col]) {
                    cells[row][col].setBackground(SudokuConstants.COLOR_CELL_SOLVED);
                    cells[row][col].setForeground(SudokuConstants.COLOR_TEXT_SOLVED);
                }
            }
        }
    }
    
    /**
     * Reset tampilan visual (untuk backtrack)
     */
    public void resetBoardVisuals(boolean[][] isInitialCell, int[][] board) {
        for (int row = 0; row < SudokuConstants.GRID_SIZE; row++) {
            for (int col = 0; col < SudokuConstants.GRID_SIZE; col++) {
                if (board[row][col] == 0) {
                    cells[row][col].setText("");
                    cells[row][col].setBackground(SudokuConstants.COLOR_CELL_DEFAULT);
                } else {
                    cells[row][col].setText(String.valueOf(board[row][col]));
                    if (isInitialCell[row][col]) {
                        cells[row][col].setBackground(SudokuConstants.COLOR_CELL_INITIAL);
                    } else {
                        cells[row][col].setBackground(SudokuConstants.COLOR_CELL_DEFAULT);
                    }
                }
                cells[row][col].setForeground(SudokuConstants.COLOR_TEXT_INITIAL);
            }
        }
    }
}

