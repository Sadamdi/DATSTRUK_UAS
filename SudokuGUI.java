import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.text.*;

/**
 * Kelas untuk komponen GUI Sudoku Solver
 * Menangani semua tampilan: grid, tombol, log panel
 */
public class SudokuGUI {
    
    private JFrame frame;
    private JTextField[][] cells;
    private JTextArea logArea;
    private JButton speedButton;
    private JButton skipButton;
    private int animationDelay = SudokuConstants.ANIMATION_DELAY_MEDIUM;
    private boolean skipAnimation = false;
    
    // Callback untuk interaksi
    public interface GUIActionListener {
        void onLoadExample();
        void onSolve();
        void onReset();
        void onSpeedChange(int newDelay);
        void onToggleAnimation(boolean skip);
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
        
        // Panel untuk kontrol kecepatan (baris 2)
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
            } else if (animationDelay == SudokuConstants.ANIMATION_DELAY_ULTRA_FAST) {
                animationDelay = SudokuConstants.ANIMATION_DELAY_SUPER_ULTRA_FAST;
            } else {
                animationDelay = SudokuConstants.ANIMATION_DELAY_SLOW;
            }
            speedButton.setText("⚡ " + getSpeedLabel());
            if (actionListener != null) actionListener.onSpeedChange(animationDelay);
        });
        
        // Tombol Skip Animation (Mode Cepat)
        skipButton = new JButton("⚡ Skip Animasi");
        skipButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        skipButton.setBackground(new Color(255, 152, 0)); // Orange
        skipButton.setForeground(Color.WHITE);
        skipButton.setFocusPainted(false);
        skipButton.setBorderPainted(false);
        skipButton.setPreferredSize(new Dimension(130, 30));
        skipButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        skipButton.addActionListener(e -> {
            skipAnimation = !skipAnimation;
            if (skipAnimation) {
                skipButton.setText("✓ Mode Cepat");
                skipButton.setBackground(new Color(76, 175, 80)); // Hijau
                skipButton.setToolTipText("Mode cepat aktif - tidak ada animasi");
            } else {
                skipButton.setText("⚡ Skip Animasi");
                skipButton.setBackground(new Color(255, 152, 0)); // Orange
                skipButton.setToolTipText("Klik untuk mode cepat (tanpa animasi)");
            }
            if (actionListener != null) actionListener.onToggleAnimation(skipAnimation);
        });
        
        speedPanel.add(speedLabel);
        speedPanel.add(speedButton);
        speedPanel.add(skipButton);
        
        buttonPanel.add(mainButtonsPanel);
        buttonPanel.add(Box.createVerticalStrut(5));
        buttonPanel.add(speedPanel);
        
        return buttonPanel;
    }
    
    private String getSpeedLabel() {
        if (animationDelay == SudokuConstants.ANIMATION_DELAY_SLOW) return "Lambat";
        if (animationDelay == SudokuConstants.ANIMATION_DELAY_MEDIUM) return "Sedang";
        if (animationDelay == SudokuConstants.ANIMATION_DELAY_FAST) return "Cepat";
        if (animationDelay == SudokuConstants.ANIMATION_DELAY_ULTRA_FAST) return "Ultra Fast";
        if (animationDelay == SudokuConstants.ANIMATION_DELAY_SUPER_ULTRA_FAST) return "Super Ultra";
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

