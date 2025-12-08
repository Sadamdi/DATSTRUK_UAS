import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Menu utama aplikasi Sudoku
 * Design modern dengan Dark Mode support
 */
public class MainMenu extends JFrame {
    
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JButton solverButton;
    private JButton gameButton;
    private JToggleButton darkModeToggle;
    
    public MainMenu() {
        setTitle("Sudoku - Menu Utama");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        setupUI();
        applyTheme();
    }
    
    private void setupUI() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        
        // Top bar dengan dark mode toggle
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.setOpaque(false);
        
        darkModeToggle = new JToggleButton("🌙 Dark Mode");
        darkModeToggle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        darkModeToggle.setFocusPainted(false);
        darkModeToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        darkModeToggle.setSelected(ThemeManager.isDarkMode());
        darkModeToggle.addActionListener(e -> {
            ThemeManager.toggleDarkMode();
            applyTheme();
        });
        topBar.add(darkModeToggle);
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 60, 0));
        
        titleLabel = new JLabel("SUDOKU");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 72));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        subtitleLabel = new JLabel("2 Algoritma: Brute Force & Harris Hawks Optimization");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel authorLabel = new JLabel("Proyek UAS - Struktur Data");
        authorLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        authorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(15));
        headerPanel.add(subtitleLabel);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(authorLabel);
        
        // Menu buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 40, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 100, 20, 100));
        
        // Button 1: Sudoku Solver
        solverButton = createMenuButton(
            "🔍 SUDOKU SOLVER",
            "Selesaikan puzzle Sudoku dengan 2 algoritma",
            "• Brute Force (Backtracking)\n• Harris Hawks Optimization\n• Animasi step-by-step & Skip",
            ThemeManager.BTN_SOLVE
        );
        solverButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new SudokuSolverApp().setVisible(true));
        });
        
        // Button 2: Bermain Sudoku
        gameButton = createMenuButton(
            "🎮 BERMAIN SUDOKU",
            "Bermain melawan Bot dengan pilihan algoritma",
            "• Bot: Brute Force atau Harris Hawks\n• 3 tingkat kesulitan Bot\n• Mode turn-based + Sistem skor",
            ThemeManager.BTN_LOAD
        );
        gameButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new SudokuGameApp().setVisible(true));
        });
        
        buttonPanel.add(solverButton);
        buttonPanel.add(gameButton);
        
        // Footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setOpaque(false);
        JLabel footerLabel = new JLabel("Tekan tombol di atas untuk memulai");
        footerLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        footerPanel.add(footerLabel);
        
        // Assemble
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);
        northPanel.add(topBar, BorderLayout.NORTH);
        northPanel.add(headerPanel, BorderLayout.CENTER);
        
        mainPanel.add(northPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        setLocationRelativeTo(null);
    }
    
    private JButton createMenuButton(String title, String subtitle, String features, Color bgColor) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout());
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(350, 250));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        
        // Content panel
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLbl = new JLabel("<html><center>" + subtitle + "</center></html>");
        subtitleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLbl.setForeground(new Color(255, 255, 255, 200));
        subtitleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel featuresLbl = new JLabel("<html><div style='text-align:center'>" + 
            features.replace("\n", "<br>") + "</div></html>");
        featuresLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        featuresLbl.setForeground(new Color(255, 255, 255, 180));
        featuresLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        content.add(titleLbl);
        content.add(Box.createVerticalStrut(15));
        content.add(subtitleLbl);
        content.add(Box.createVerticalStrut(20));
        content.add(featuresLbl);
        
        button.add(content, BorderLayout.CENTER);
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private void applyTheme() {
        Color bg = ThemeManager.getBackground();
        Color text = ThemeManager.getText();
        Color textSecondary = ThemeManager.getTextSecondary();
        
        mainPanel.setBackground(bg);
        titleLabel.setForeground(text);
        subtitleLabel.setForeground(textSecondary);
        
        if (ThemeManager.isDarkMode()) {
            darkModeToggle.setText("☀️ Light Mode");
            darkModeToggle.setBackground(new Color(60, 60, 80));
            darkModeToggle.setForeground(Color.WHITE);
        } else {
            darkModeToggle.setText("🌙 Dark Mode");
            darkModeToggle.setBackground(new Color(240, 240, 245));
            darkModeToggle.setForeground(new Color(33, 37, 41));
        }
        
        // Refresh UI
        SwingUtilities.updateComponentTreeUI(this);
        repaint();
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }
}
