package algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import gui.SudokuConstants;

/**
 * Implementasi Harris Hawks Optimization (HHO) untuk Sudoku Solver
 * 
 * CARA KERJA ALGORITMA HHO:
 * Harris Hawks Optimization adalah metaheuristic algorithm yang terinspirasi
 * dari perilaku berburu kooperatif Harris Hawks di alam.
 * 
 * Fase-fase dalam HHO:
 * 1. EXPLORATION: Hawks mencari mangsa secara acak (diversifikasi solusi)
 * 2. EXPLOITATION: Hawks menyerang mangsa (intensifikasi solusi terbaik)
 *    - Soft Besiege: Mangsa masih punya energi, hawks mengelilingi
 *    - Hard Besiege: Mangsa lemah, hawks menyerang langsung
 *    - Soft Besiege + Rapid Dives: Hawks melakukan serangan mendadak
 *    - Hard Besiege + Rapid Dives: Serangan final yang intens
 * 
 * Adaptasi untuk Sudoku:
 * - Hawk = Kandidat solusi sudoku (board 9x9)
 * - Prey (mangsa) = Solusi optimal sudoku
 * - Fitness = Jumlah konflik/kesalahan dalam board (semakin kecil semakin baik)
 * - Population = Kumpulan kandidat board sudoku
 */
public class HarrisHawksSolver implements SudokuSolverAlgorithm {
    
    private int[][] board;
    private int[][] initialBoard; // Board awal (read-only)
    private boolean[][] isFixed; // Sel yang tidak boleh diubah
    private SudokuValidator validator;
    private SudokuSolverAlgorithm.SolverCallback callback;
    private int stepCount = 0;
    private volatile boolean cancelled = false;
    private Random random = new Random();
    
    // Untuk tracking perubahan animasi
    private int[][] previousBoard; // Board dari iterasi sebelumnya
    
    // Parameter HHO
    private static final int POPULATION_SIZE = 10; // Jumlah hawks (solusi kandidat)
    private static final int MAX_ITERATIONS = 1000; // Iterasi maksimal
    private static final double EPSILON = 1e-6; // Threshold untuk konvergensi
    
    // Population hawks
    private List<Hawk> population;
    private Hawk rabbitPosition; // Posisi mangsa (solusi terbaik saat ini)
    
    /**
     * Class untuk merepresentasikan satu Hawk (kandidat solusi)
     */
    private class Hawk {
        int[][] position; // Board sudoku
        double fitness; // Fitness (jumlah konflik)
        
        Hawk() {
            position = new int[SudokuConstants.GRID_SIZE][SudokuConstants.GRID_SIZE];
            fitness = Double.MAX_VALUE;
        }
        
        Hawk(int[][] pos) {
            position = new int[SudokuConstants.GRID_SIZE][SudokuConstants.GRID_SIZE];
            for (int i = 0; i < SudokuConstants.GRID_SIZE; i++) {
                System.arraycopy(pos[i], 0, position[i], 0, SudokuConstants.GRID_SIZE);
            }
            fitness = calculateFitness(position);
        }
        
        void copyPosition(int[][] source) {
            for (int i = 0; i < SudokuConstants.GRID_SIZE; i++) {
                System.arraycopy(source[i], 0, position[i], 0, SudokuConstants.GRID_SIZE);
            }
        }
    }
    
    public HarrisHawksSolver(int[][] board) {
        this.board = board;
        this.initialBoard = new int[SudokuConstants.GRID_SIZE][SudokuConstants.GRID_SIZE];
        this.isFixed = new boolean[SudokuConstants.GRID_SIZE][SudokuConstants.GRID_SIZE];
        
        // Copy initial board dan tandai sel yang fixed
        for (int i = 0; i < SudokuConstants.GRID_SIZE; i++) {
            for (int j = 0; j < SudokuConstants.GRID_SIZE; j++) {
                initialBoard[i][j] = board[i][j];
                isFixed[i][j] = (board[i][j] != 0);
            }
        }
        
        this.validator = new SudokuValidator(board);
        this.population = new ArrayList<>();
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
        return harrisHawksOptimization(true);
    }
    
    @Override
    public boolean solve() {
        stepCount = 0;
        cancelled = false;
        return harrisHawksOptimization(false);
    }
    
    @Override
    public int getStepCount() {
        return stepCount;
    }
    
    @Override
    public String getAlgorithmName() {
        return "Harris Hawks Optimization";
    }
    
    /**
     * Main HHO Algorithm
     */
    private boolean harrisHawksOptimization(boolean withAnimation) {
        if (callback != null && withAnimation) {
            callback.onLog("🦅 === HARRIS HAWKS OPTIMIZATION ===\n");
            callback.onLog("Inisialisasi populasi " + POPULATION_SIZE + " hawks...\n\n");
        }
        
        // 1. Inisialisasi populasi
        initializePopulation();
        
        // 2. Evaluasi fitness awal dan cari rabbit (solusi terbaik)
        rabbitPosition = findBestHawk();
        
        // 3. Inisialisasi previousBoard untuk tracking perubahan
        previousBoard = new int[SudokuConstants.GRID_SIZE][SudokuConstants.GRID_SIZE];
        for (int i = 0; i < SudokuConstants.GRID_SIZE; i++) {
            System.arraycopy(rabbitPosition.position[i], 0, previousBoard[i], 0, SudokuConstants.GRID_SIZE);
        }
        
        if (callback != null && withAnimation) {
            callback.onLog("✓ Populasi diinisialisasi\n");
            callback.onLog("Fitness terbaik awal: " + String.format("%.2f", rabbitPosition.fitness) + " konflik\n\n");
            
            // Tampilkan board awal
            visualizeBoardChanges(rabbitPosition.position, true);
        }
        
        // 3. Main loop - iterasi HHO
        for (int iter = 0; iter < MAX_ITERATIONS && !cancelled; iter++) {
            stepCount++;
            
            // Energy dari rabbit (prey) - menurun setiap iterasi
            double E0 = 2 * random.nextDouble() - 1; // [-1, 1]
            double E = 2 * E0 * (1 - (double)iter / MAX_ITERATIONS);
            
            if (callback != null && withAnimation && iter % 10 == 0) {
                callback.onLog("📍 Iterasi " + (iter + 1) + "/" + MAX_ITERATIONS + "\n");
                callback.onLog("   Energi mangsa: " + String.format("%.3f", E) + "\n");
                callback.onLog("   Fitness terbaik: " + String.format("%.2f", rabbitPosition.fitness) + " konflik\n");
            }
            
            // Update setiap hawk
            for (int i = 0; i < population.size() && !cancelled; i++) {
                Hawk hawk = population.get(i);
                
                // Exploration vs Exploitation
                double q = random.nextDouble();
                double rand = random.nextDouble();
                
                if (Math.abs(E) >= 1) {
                    // EXPLORATION PHASE - Hawks mencari mangsa secara acak
                    if (q >= 0.5) {
                        // Perch based on other hawks
                        int randIdx = random.nextInt(population.size());
                        Hawk randHawk = population.get(randIdx);
                        int[][] newPos = explorePerturbation(randHawk.position, hawk.position);
                        hawk.copyPosition(newPos);
                    } else {
                        // Perch on random trees (random position in search space)
                        int[][] newPos = exploreRandom();
                        hawk.copyPosition(newPos);
                    }
                    
                    // Skip visualisasi per hawk - hanya visualisasi saat ada improvement
                } else {
                    // EXPLOITATION PHASE - Hawks menyerang mangsa
                    double r = random.nextDouble();
                    
                    if (r >= 0.5 && Math.abs(E) >= 0.5) {
                        // Soft besiege
                        int[][] newPos = softBesiege(hawk.position, E);
                        hawk.copyPosition(newPos);
                        
                    } else if (r >= 0.5 && Math.abs(E) < 0.5) {
                        // Hard besiege
                        int[][] newPos = hardBesiege(hawk.position, E);
                        hawk.copyPosition(newPos);
                        
                    } else if (r < 0.5 && Math.abs(E) >= 0.5) {
                        // Soft besiege with progressive rapid dives
                        int[][] newPos = softBesiegeWithDives(hawk.position, E);
                        hawk.copyPosition(newPos);
                        
                    } else {
                        // Hard besiege with progressive rapid dives
                        int[][] newPos = hardBesiegeWithDives(hawk.position, E);
                        hawk.copyPosition(newPos);
                    }
                    
                    // Skip visualisasi per hawk - hanya visualisasi saat ada improvement
                }
                
                // Update fitness hawk
                hawk.fitness = calculateFitness(hawk.position);
                
                // Update rabbit position jika menemukan solusi lebih baik
                if (hawk.fitness < rabbitPosition.fitness) {
                    rabbitPosition = new Hawk(hawk.position);
                    
                    if (callback != null && withAnimation) {
                        callback.onLog("   ⭐ Solusi lebih baik ditemukan! Fitness: " + 
                                      String.format("%.2f", rabbitPosition.fitness) + "\n");
                        
                        // Visualisasi perubahan: highlight sel yang berubah
                        visualizeBoardChanges(rabbitPosition.position, false);
                        
                        // Update previousBoard
                        for (int r = 0; r < SudokuConstants.GRID_SIZE; r++) {
                            System.arraycopy(rabbitPosition.position[r], 0, previousBoard[r], 0, SudokuConstants.GRID_SIZE);
                        }
                    }
                }
            }
            
            // Cek apakah sudah menemukan solusi optimal (fitness = 0)
            if (rabbitPosition.fitness < EPSILON) {
                if (callback != null && withAnimation) {
                    callback.onLog("\n🎉 SOLUSI OPTIMAL DITEMUKAN! Fitness: " + 
                                  String.format("%.2f", rabbitPosition.fitness) + "\n");
                }
                
                // Copy solusi ke board utama
                for (int i = 0; i < SudokuConstants.GRID_SIZE; i++) {
                    System.arraycopy(rabbitPosition.position[i], 0, board[i], 0, SudokuConstants.GRID_SIZE);
                }
                
                if (callback != null && withAnimation) {
                    // Tampilkan solusi final dengan warna hijau
                    for (int row = 0; row < SudokuConstants.GRID_SIZE; row++) {
                        for (int col = 0; col < SudokuConstants.GRID_SIZE; col++) {
                            if (!isFixed[row][col]) {
                                callback.updateCell(row, col, board[row][col], SudokuConstants.COLOR_CELL_SOLVED);
                            }
                        }
                    }
                    callback.sleep(500);
                }
                
                return true;
            }
        }
        
        // Jika belum menemukan solusi optimal setelah MAX_ITERATIONS
        // Gunakan solusi terbaik yang ditemukan
        if (rabbitPosition.fitness < 5.0) { // Threshold acceptable
            for (int i = 0; i < SudokuConstants.GRID_SIZE; i++) {
                System.arraycopy(rabbitPosition.position[i], 0, board[i], 0, SudokuConstants.GRID_SIZE);
            }
            
            if (callback != null && withAnimation) {
                callback.onLog("\n⚠️ Solusi terbaik ditemukan dengan " + 
                              String.format("%.2f", rabbitPosition.fitness) + " konflik\n");
            }
            
            return rabbitPosition.fitness < EPSILON;
        }
        
        return false;
    }
    
    /**
     * Inisialisasi populasi hawks dengan solusi acak yang valid
     */
    private void initializePopulation() {
        population.clear();
        
        for (int i = 0; i < POPULATION_SIZE; i++) {
            int[][] newPosition = generateRandomValidBoard();
            Hawk hawk = new Hawk(newPosition);
            population.add(hawk);
        }
    }
    
    /**
     * Generate board random yang valid (mengisi sel kosong dengan angka acak)
     */
    private int[][] generateRandomValidBoard() {
        int[][] newBoard = new int[SudokuConstants.GRID_SIZE][SudokuConstants.GRID_SIZE];
        
        // Copy initial board
        for (int i = 0; i < SudokuConstants.GRID_SIZE; i++) {
            System.arraycopy(initialBoard[i], 0, newBoard[i], 0, SudokuConstants.GRID_SIZE);
        }
        
        // Isi sel kosong dengan angka acak (1-9)
        for (int row = 0; row < SudokuConstants.GRID_SIZE; row++) {
            for (int col = 0; col < SudokuConstants.GRID_SIZE; col++) {
                if (!isFixed[row][col]) {
                    // Cari angka yang valid atau random jika tidak ada
                    List<Integer> validNumbers = getValidNumbers(newBoard, row, col);
                    if (!validNumbers.isEmpty()) {
                        newBoard[row][col] = validNumbers.get(random.nextInt(validNumbers.size()));
                    } else {
                        newBoard[row][col] = 1 + random.nextInt(9);
                    }
                }
            }
        }
        
        return newBoard;
    }
    
    /**
     * Dapatkan list angka yang valid untuk posisi tertentu
     */
    private List<Integer> getValidNumbers(int[][] board, int row, int col) {
        List<Integer> validNumbers = new ArrayList<>();
        SudokuValidator tempValidator = new SudokuValidator(board);
        
        for (int num = 1; num <= 9; num++) {
            if (tempValidator.isValidPlacement(row, col, num)) {
                validNumbers.add(num);
            }
        }
        
        return validNumbers;
    }
    
    /**
     * Cari hawk dengan fitness terbaik (rabbit)
     */
    private Hawk findBestHawk() {
        Hawk best = population.get(0);
        for (Hawk hawk : population) {
            if (hawk.fitness < best.fitness) {
                best = hawk;
            }
        }
        return new Hawk(best.position);
    }
    
    /**
     * Hitung fitness (jumlah konflik dalam board)
     * Fitness = 0 berarti solusi sempurna
     */
    private double calculateFitness(int[][] position) {
        int conflicts = 0;
        
        // Hitung konflik di setiap baris
        for (int row = 0; row < SudokuConstants.GRID_SIZE; row++) {
            boolean[] seen = new boolean[10];
            for (int col = 0; col < SudokuConstants.GRID_SIZE; col++) {
                int val = position[row][col];
                if (val > 0 && val <= 9) {
                    if (seen[val]) conflicts++;
                    seen[val] = true;
                }
            }
        }
        
        // Hitung konflik di setiap kolom
        for (int col = 0; col < SudokuConstants.GRID_SIZE; col++) {
            boolean[] seen = new boolean[10];
            for (int row = 0; row < SudokuConstants.GRID_SIZE; row++) {
                int val = position[row][col];
                if (val > 0 && val <= 9) {
                    if (seen[val]) conflicts++;
                    seen[val] = true;
                }
            }
        }
        
        // Hitung konflik di setiap subgrid 3x3
        for (int boxRow = 0; boxRow < 3; boxRow++) {
            for (int boxCol = 0; boxCol < 3; boxCol++) {
                boolean[] seen = new boolean[10];
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        int row = boxRow * 3 + i;
                        int col = boxCol * 3 + j;
                        int val = position[row][col];
                        if (val > 0 && val <= 9) {
                            if (seen[val]) conflicts++;
                            seen[val] = true;
                        }
                    }
                }
            }
        }
        
        return conflicts;
    }
    
    /**
     * EXPLORATION: Perturbasi berdasarkan hawk lain
     */
    private int[][] explorePerturbation(int[][] randPos, int[][] currentPos) {
        int[][] newPos = new int[SudokuConstants.GRID_SIZE][SudokuConstants.GRID_SIZE];
        
        for (int i = 0; i < SudokuConstants.GRID_SIZE; i++) {
            for (int j = 0; j < SudokuConstants.GRID_SIZE; j++) {
                if (isFixed[i][j]) {
                    newPos[i][j] = initialBoard[i][j];
                } else {
                    // Mix between random hawk and current hawk
                    if (random.nextDouble() < 0.5) {
                        newPos[i][j] = randPos[i][j];
                    } else {
                        newPos[i][j] = currentPos[i][j];
                    }
                }
            }
        }
        
        // Random mutation untuk beberapa sel
        int mutations = 1 + random.nextInt(3);
        for (int m = 0; m < mutations; m++) {
            int row = random.nextInt(SudokuConstants.GRID_SIZE);
            int col = random.nextInt(SudokuConstants.GRID_SIZE);
            if (!isFixed[row][col]) {
                List<Integer> validNums = getValidNumbers(newPos, row, col);
                if (!validNums.isEmpty()) {
                    newPos[row][col] = validNums.get(random.nextInt(validNums.size()));
                } else {
                    newPos[row][col] = 1 + random.nextInt(9);
                }
            }
        }
        
        return newPos;
    }
    
    /**
     * EXPLORATION: Generate posisi acak baru
     */
    private int[][] exploreRandom() {
        return generateRandomValidBoard();
    }
    
    /**
     * EXPLOITATION: Soft Besiege
     */
    private int[][] softBesiege(int[][] currentPos, double E) {
        int[][] newPos = new int[SudokuConstants.GRID_SIZE][SudokuConstants.GRID_SIZE];
        
        for (int i = 0; i < SudokuConstants.GRID_SIZE; i++) {
            for (int j = 0; j < SudokuConstants.GRID_SIZE; j++) {
                if (isFixed[i][j]) {
                    newPos[i][j] = initialBoard[i][j];
                } else {
                    // Gerak menuju rabbit dengan perturbasi
                    double rand = random.nextDouble();
                    if (rand < Math.abs(E)) {
                        newPos[i][j] = rabbitPosition.position[i][j];
                    } else {
                        newPos[i][j] = currentPos[i][j];
                    }
                }
            }
        }
        
        // Local search: perbaiki beberapa sel dengan konflik
        improveConflicts(newPos, 2);
        
        return newPos;
    }
    
    /**
     * EXPLOITATION: Hard Besiege
     */
    private int[][] hardBesiege(int[][] currentPos, double E) {
        int[][] newPos = new int[SudokuConstants.GRID_SIZE][SudokuConstants.GRID_SIZE];
        
        // Hard besiege: lebih agresif menuju rabbit
        for (int i = 0; i < SudokuConstants.GRID_SIZE; i++) {
            for (int j = 0; j < SudokuConstants.GRID_SIZE; j++) {
                if (isFixed[i][j]) {
                    newPos[i][j] = initialBoard[i][j];
                } else {
                    newPos[i][j] = rabbitPosition.position[i][j];
                }
            }
        }
        
        // Local search lebih intensif
        improveConflicts(newPos, 3);
        
        return newPos;
    }
    
    /**
     * EXPLOITATION: Soft Besiege with Rapid Dives
     */
    private int[][] softBesiegeWithDives(int[][] currentPos, double E) {
        // Coba soft besiege dulu
        int[][] Y = softBesiege(currentPos, E);
        double fitnessY = calculateFitness(Y);
        
        // Jika tidak lebih baik, coba levy flight (dive)
        if (fitnessY >= calculateFitness(currentPos)) {
            int[][] Z = levyFlightDive(currentPos);
            double fitnessZ = calculateFitness(Z);
            
            if (fitnessZ < fitnessY) {
                return Z;
            }
        }
        
        return Y;
    }
    
    /**
     * EXPLOITATION: Hard Besiege with Rapid Dives
     */
    private int[][] hardBesiegeWithDives(int[][] currentPos, double E) {
        // Coba hard besiege dulu
        int[][] Y = hardBesiege(currentPos, E);
        double fitnessY = calculateFitness(Y);
        
        // Jika tidak lebih baik, coba levy flight (dive)
        if (fitnessY >= calculateFitness(currentPos)) {
            int[][] Z = levyFlightDive(currentPos);
            double fitnessZ = calculateFitness(Z);
            
            if (fitnessZ < fitnessY) {
                return Z;
            }
        }
        
        return Y;
    }
    
    /**
     * Levy Flight - Pola dive acak dengan distribusi Levy
     */
    private int[][] levyFlightDive(int[][] currentPos) {
        int[][] newPos = new int[SudokuConstants.GRID_SIZE][SudokuConstants.GRID_SIZE];
        
        for (int i = 0; i < SudokuConstants.GRID_SIZE; i++) {
            System.arraycopy(currentPos[i], 0, newPos[i], 0, SudokuConstants.GRID_SIZE);
        }
        
        // Random jumps (levy flight) - mutasi lebih banyak sel
        int jumps = 3 + random.nextInt(5);
        for (int j = 0; j < jumps; j++) {
            int row = random.nextInt(SudokuConstants.GRID_SIZE);
            int col = random.nextInt(SudokuConstants.GRID_SIZE);
            
            if (!isFixed[row][col]) {
                List<Integer> validNums = getValidNumbers(newPos, row, col);
                if (!validNums.isEmpty()) {
                    newPos[row][col] = validNums.get(random.nextInt(validNums.size()));
                } else {
                    newPos[row][col] = 1 + random.nextInt(9);
                }
            }
        }
        
        return newPos;
    }
    
    /**
     * Local search untuk memperbaiki konflik
     */
    private void improveConflicts(int[][] position, int maxFixes) {
        int fixes = 0;
        
        for (int row = 0; row < SudokuConstants.GRID_SIZE && fixes < maxFixes; row++) {
            for (int col = 0; col < SudokuConstants.GRID_SIZE && fixes < maxFixes; col++) {
                if (!isFixed[row][col]) {
                    // Cek apakah sel ini menyebabkan konflik
                    int oldVal = position[row][col];
                    
                    // Cari angka yang valid
                    List<Integer> validNums = getValidNumbers(position, row, col);
                    if (!validNums.isEmpty() && !validNums.contains(oldVal)) {
                        position[row][col] = validNums.get(random.nextInt(validNums.size()));
                        fixes++;
                    }
                }
            }
        }
    }
    
    /**
     * Visualisasi perubahan board dengan highlight
     * Step 1: Highlight sel yang berubah dengan KUNING
     * Step 2: Reset semua ke PUTIH
     */
    private void visualizeBoardChanges(int[][] newPosition, boolean isInitial) {
        if (callback == null) return;
        
        if (isInitial) {
            // Tampilan awal: tampilkan semua dengan warna default
            for (int row = 0; row < SudokuConstants.GRID_SIZE; row++) {
                for (int col = 0; col < SudokuConstants.GRID_SIZE; col++) {
                    if (!isFixed[row][col]) {
                        callback.updateCell(row, col, newPosition[row][col], SudokuConstants.COLOR_CELL_DEFAULT);
                    }
                }
            }
            callback.sleep(50);
            return;
        }
        
        // Step 1: Highlight sel yang BERUBAH dengan KUNING
        boolean hasChanges = false;
        for (int row = 0; row < SudokuConstants.GRID_SIZE; row++) {
            for (int col = 0; col < SudokuConstants.GRID_SIZE; col++) {
                if (!isFixed[row][col]) {
                    if (previousBoard[row][col] != newPosition[row][col]) {
                        // Sel berubah -> KUNING
                        callback.updateCell(row, col, newPosition[row][col], new java.awt.Color(255, 235, 59)); // Kuning
                        hasChanges = true;
                    } else {
                        // Sel tidak berubah -> tetap putih
                        callback.updateCell(row, col, newPosition[row][col], SudokuConstants.COLOR_CELL_DEFAULT);
                    }
                }
            }
        }
        
        if (hasChanges) {
            // Pause untuk melihat perubahan (kuning)
            callback.sleep(200);
            
            // Step 2: Reset SEMUA sel ke PUTIH
            for (int row = 0; row < SudokuConstants.GRID_SIZE; row++) {
                for (int col = 0; col < SudokuConstants.GRID_SIZE; col++) {
                    if (!isFixed[row][col]) {
                        callback.updateCell(row, col, newPosition[row][col], SudokuConstants.COLOR_CELL_DEFAULT);
                    }
                }
            }
            callback.sleep(50);
        }
    }
}
