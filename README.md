## DATSTRUK_UAS – Project Sudoku (Struktur Data UAS)

Repo ini isinya proyek UAS Struktur Data berupa aplikasi **Sudoku** berbasis **Java Swing**.  
Di sini ada 2 mode utama dengan **2 algoritma** yang bisa dipilih:

- **Sudoku Solver** – mode buat **menyelesaikan puzzle Sudoku** pakai:
  - **Brute Force (Backtracking)** – algoritma klasik dengan rekursi dan backtracking
  - **Harris Hawks Optimization (HHO)** – algoritma metaheuristic terinspirasi dari perilaku berburu Harris Hawks
  - Dengan animasi langkah-langkahnya dan opsi skip animasi
- **Sudoku Game** – mode **bermain Sudoku lawan bot** dengan pilihan algoritma bot (Brute Force atau Harris Hawks), dibungkus kayak game turn-based (player vs komputer).

---

### 1. Gambaran Umum Aplikasi

- **Bahasa & UI**
  - Dibuat dengan **Java** (Swing).
  - Tampilan pakai grid Sudoku 9x9, ada panel log di samping, dan beberapa tombol kontrol.
  - Ada **dark mode** yang bisa di-toggle dari tombol di UI.

- **Mode yang tersedia**
  - `SudokuSolverApp` – fokus ke **penyelesaian puzzle** dan visualisasi algoritma brute force.
  - `SudokuGameApp` – fokus ke **permainan**: player isi angka, bot juga isi angka, ada **skor** dan **giliran (turn)**.
  - `MainMenu` – jendela utama untuk milih mau masuk ke mode Solver atau Game.

---

### 2. Struktur Proyek

Proyek ini diorganisir dalam beberapa folder berdasarkan fungsinya:

```
UAS/
├── Main.java                    # Entry point aplikasi
├── README.md                    # Dokumentasi proyek
├── LICENSE                      # Lisensi MIT
├── compile.bat                 # Script untuk compile semua file Java
├── run.bat                     # Script untuk menjalankan aplikasi
│
├── algorithm/                  # Folder algoritma solver dan utility
│   ├── SudokuSolverAlgorithm.java    # Interface untuk semua algoritma solver
│   ├── BruteForceSolver.java         # Implementasi algoritma Brute Force (Backtracking)
│   ├── HarrisHawksSolver.java        # Implementasi algoritma Harris Hawks Optimization
│   ├── SudokuSolver.java             # Legacy solver (deprecated)
│   ├── SudokuValidator.java          # Validator untuk cek validitas papan Sudoku
│   └── PuzzleGenerator.java          # Generator puzzle Sudoku random
│
├── gui/                        # Folder komponen GUI dan tema
│   ├── MainMenu.java                 # Menu utama aplikasi
│   ├── SudokuGUI.java               # Komponen GUI untuk grid dan kontrol
│   ├── SudokuConstants.java         # Konstanta global (ukuran grid, warna, dll)
│   └── ThemeManager.java            # Manager untuk tema dark/light mode
│
├── solver/                     # Folder mode Solver
│   └── SudokuSolverApp.java         # Aplikasi mode penyelesaian puzzle
│
└── game/                       # Folder mode Game
    └── SudokuGameApp.java           # Aplikasi mode permainan vs bot
```

#### Penjelasan Struktur Folder:

- **`algorithm/`** – Berisi semua logika algoritma dan utility:
  - Interface dan implementasi algoritma solver (Brute Force, Harris Hawks)
  - Validator untuk memastikan aturan Sudoku terpenuhi
  - Generator untuk membuat puzzle baru secara random

- **`gui/`** – Berisi komponen antarmuka pengguna:
  - Menu utama untuk navigasi antar mode
  - Komponen GUI yang dipakai bersama (grid, tombol, log panel)
  - Konstanta dan manager tema untuk styling aplikasi

- **`solver/`** – Berisi aplikasi mode Solver:
  - Window/frame khusus untuk mode penyelesaian puzzle
  - Mengatur visualisasi algoritma dan animasi

- **`game/`** – Berisi aplikasi mode Game:
  - Window/frame khusus untuk mode permainan turn-based
  - Mengatur logika permainan, skor, dan giliran player vs bot

- **Root folder** – Berisi file utama:
  - `Main.java` sebagai entry point aplikasi
  - File batch untuk memudahkan compile dan run
  - Dokumentasi dan lisensi

---

### 3. Struktur Kelas Utama

#### 3.1 Ringkasan kelas-kelas penting

- **`MainMenu.java`**
  - Tampilan menu awal.
  - Ada tombol ke:
    - Mode **Solver** (`SudokuSolverApp`)
    - Mode **Game** (`SudokuGameApp`)

- **`SudokuGUI.java`**
  - Kelas yang ngatur tampilan:
    - Grid 9x9 (`JTextField`).
    - **Dropdown pilihan algoritma** (Brute Force atau Harris Hawks).
    - Panel tombol (Solve, Reset, Load Example, Skip Animasi, dll).
    - Panel log (textarea) untuk menampilkan proses.
  - Dipakai oleh **dua mode**: `SudokuSolverApp` dan `SudokuGameApp`.

- **`SudokuSolverAlgorithm.java`** (Interface)
  - Interface untuk semua algoritma solver Sudoku
  - Mendefinisikan method yang harus diimplementasi:
    - `solve()` – versi cepat tanpa animasi
    - `solveWithAnimation()` – versi dengan callback ke GUI
    - `setCallback()`, `cancel()`, `getStepCount()`, `getAlgorithmName()`
  - Ada **`SolverCallback`** interface:
    - `onCellProcessing`, `onCellTesting`, `onCellSolved`, `onBacktrack`, `onLog`, `updateCell`, `sleep`
    - Dipakai untuk animasi per langkah

- **`BruteForceSolver.java`** (implements SudokuSolverAlgorithm)
  - Implementasi **algoritma Brute Force (Backtracking)**
  - Cara kerja:
    - Cari sel kosong pertama (kiri ke kanan, atas ke bawah)
    - Coba angka 1-9, validasi setiap angka
    - Jika valid, simpan dan rekursi ke sel berikutnya
    - Jika tidak valid atau tidak ada solusi, backtrack ke sel sebelumnya
  - Kompleksitas: O(9^n) dimana n = jumlah sel kosong

- **`HarrisHawksSolver.java`** (implements SudokuSolverAlgorithm)
  - Implementasi **algoritma Harris Hawks Optimization (HHO)**
  - Metaheuristic algorithm terinspirasi dari perilaku berburu kooperatif Harris Hawks
  - Cara kerja:
    - **Inisialisasi**: Buat populasi hawks (kandidat solusi) secara random
    - **Exploration**: Hawks mencari mangsa (solusi) secara random (diversifikasi)
    - **Exploitation**: Hawks menyerang mangsa dengan 4 strategi:
      - Soft Besiege: Mangsa masih berenergi, hawks mengelilingi
      - Hard Besiege: Mangsa lemah, hawks menyerang langsung
      - Soft Besiege + Rapid Dives: Serangan mendadak
      - Hard Besiege + Rapid Dives: Serangan final intens
    - **Fitness**: Jumlah konflik dalam board (baris, kolom, subgrid)
    - **Konvergensi**: Iterasi berlanjut hingga fitness = 0 (solusi sempurna)
  - Parameter:
    - Population size: 10 hawks
    - Max iterations: 1000
  - Cocok untuk puzzle yang sulit diselesaikan dengan brute force

- **`SudokuSolver.java`** (Legacy - deprecated)
  - Class lama yang sekarang digantikan oleh BruteForceSolver
  - Masih ada untuk backward compatibility

- **`SudokuValidator.java`**
  - Cek apakah papan valid:
    - Tidak ada angka duplikat di **baris**.
    - Tidak ada angka duplikat di **kolom**.
    - Tidak ada angka duplikat di **subgrid 3x3**.
  - Dipakai:
    - Sebelum solve puzzle.
    - Saat player isi angka di mode Game (cek apakah angka melanggar aturan).

- **`PuzzleGenerator.java`**
  - Generate puzzle Sudoku secara random.
  - Mengatur banyaknya **sel kosong** buat set semacam tingkat kesulitan.

- **`SudokuConstants.java`**
  - Konstanta global:
    - `GRID_SIZE` (9).
    - Warna-warna UI (background, warna sel default, warna angka awal, warna sel solved, dll).

- **`ThemeManager.java`**
  - Ngatur **tema** (Dark/Light).
  - Dipakai di `SudokuSolverApp` dan `SudokuGameApp` lewat tombol toggle dark mode.

#### 3.2 Tiga file utama

- **`Main.java`**
  - Ini adalah **entry point** aplikasi (punya method `public static void main(String[] args)`).
  - Tugas utamanya:
    - Inisialisasi tampilan awal (biasanya langsung membuat dan menampilkan `MainMenu`).
    - Di sinilah aplikasi pertama kali dijalankan saat kamu ketik `java Main`.
  - Secara konsep:
    - File ini tidak berisi logika Sudoku.
    - Hanya sebagai “gerbang” untuk buka jendela utama.

- **`SudokuGameApp.java`**
  - Kelas ini adalah **window / frame** untuk mode **bermain Sudoku lawan komputer (bot)**.
  - Peran utama:
    - Mengatur **UI game**:
      - Grid Sudoku (pakai `SudokuGUI`).
      - Panel skor (Player vs Bot).
      - Tombol `Mulai`, `Submit`, `Reset`, `Kembali`, dan toggle dark mode.
      - Panel pengaturan bot (pilih level `Easy/Medium/Hard`, opsi show/skip proses bot).
    - Menyimpan **state permainan**:
      - `playerBoard` → papan yang sedang dimainkan player.
      - `solution` → solusi lengkap (hasil generate / solve di belakang layar).
      - `isInitialCell` → menandai sel yang sudah ada angkanya dari awal.
      - `playerFilled` dan `botFilled` → sel mana yang diisi player dan sel mana yang diisi bot.
      - `playerScore` dan `botScore`.
      - Status game: sudah mulai/belum, game over atau belum, giliran siapa sekarang.
    - Mengatur **alur turn-based**:
      - `startGame()` → reset papan, generate puzzle + solusi, kunci angka awal, set giliran pertama ke player.
      - `submitAnswer()`:
        - Baca 1 sel yang baru diisi player.
        - Validasi angka (1–9 dan tidak melanggar aturan Sudoku via `SudokuValidator`).
        - Kalau valid → tambahkan skor player, kunci sel, cek apakah puzzle selesai.
        - Kalau tidak valid → highlight error, hapus angka, lalu panggil `botFixCell(...)`.
      - `botPlay()`:
        - Bot cari sel kosong berikutnya (urut dari kiri atas ke kanan bawah).
        - Tentukan angka yang akan diisi pakai `getBotValue(...)` (dipengaruhi level bot).
        - Update papan, update skor bot, dan tulis log ke panel log.
        - Kalau puzzle belum selesai, giliran dikembalikan ke player.
    - Mengatur **tingkat kesulitan bot**:
      - `getBotValue(row, col)`:
        - Ambil nilai benar dari `solution[row][col]`.
        - Berdasarkan difficulty (`Easy/Medium/Hard`), bot punya **peluang salah** tertentu.
        - Kalau “dipaksa salah”, dia pilih angka lain yang masih valid secara aturan Sudoku, supaya salahnya realistis.
    - Mengurus **reset**:
      - `resetGame()` membersihkan papan, log, skor, dan mengembalikan tombol ke kondisi awal.

- **`SudokuSolverApp.java`**
  - Kelas ini adalah **window / frame** untuk mode **menyelesaikan puzzle Sudoku (Solver)**.
  - Peran utama:
    - Mengatur **UI solver**:
      - Grid Sudoku (pakai `SudokuGUI`).
      - Tombol `Load Example / Generate Puzzle`, `Solve`, `Reset`, `Kembali`.
      - Slider untuk mengatur **kecepatan animasi**.
      - Toggle untuk **skip animasi** (mode cepat).
      - Panel log di sebelah kanan untuk menampilkan langkah-langkah brute force.
    - Menangani **puzzle yang mau diselesaikan**:
      - `board` → papan Sudoku yang sedang dipecahkan.
      - `isInitialCell` → sel mana yang dianggap angka awal (tidak diubah saat menampilkan solusi).
      - `loadExamplePuzzle()`:
        - Generate puzzle baru lewat `PuzzleGenerator`.
        - Mengisi `board` dan menandai `isInitialCell`.
        - Menentukan “tingkat kesulitan” berdasarkan jumlah sel kosong (Easy/Medium/Hard) lalu tampilkan info ke user.
    - Menjalankan **algoritma brute force**:
      - `solvePuzzle()`:
        - Baca isi grid dari GUI ke array `board`.
        - Validasi awal pakai `SudokuValidator` (cek puzzle-nya valid atau tidak).
        - Menyiapkan `SwingWorker` supaya proses solving jalan di background (biar UI tidak nge-freeze).
        - Kalau mode **skip animasi**:
          - Panggil `solver.solve()` langsung, tanpa update GUI tiap langkah.
        - Kalau **pakai animasi**:
          - Set `SolverCallback` ke `SudokuSolver`:
            - `updateCell(...)` buat ganti angka & warna sel di grid.
            - `onLog(...)` buat nulis log ke panel log.
            - `sleep(...)` buat atur delay antar langkah (disesuaikan slider).
          - Panggil `solver.solveWithAnimation()`.
      - Di akhir:
        - Kalau berhasil:
          - Salin hasil ke `board`, tampilkan di grid via `gui.updateSolution(...)`.
          - Tampilkan total langkah dan waktu eksekusi.
        - Kalau gagal:
          - Tampilkan pesan “tidak ada solusi” dan reset tampilan sel.
    - Mengurus **reset dan stop proses**:
      - `resetBoard()`:
        - Kalau lagi solving, `currentWorker.cancel(true)` untuk membatalkan.
        - Kosongkan grid, log, dan array `board` + `isInitialCell`.

---

### 4. Cara Menjalankan Proyek

Syarat:
- Sudah install **Java JDK** (minimal 8 atau di atasnya).
- Pastikan sedang ada di folder proyek ini (yang isinya `Main.java`, `SudokuGameApp.java`, dll).

Contoh jalanin via terminal / PowerShell (dari folder proyek ini):

```bash
cd /path/ke/folder-proyek

# Compile semua file .java
javac *.java

# Jalankan aplikasi (entry point di Main)
java Main
```

Kalau pakai IDE (IntelliJ / NetBeans / Eclipse):
- Import folder ini sebagai **Java Project**.
- Set `Main` sebagai **main class**, lalu **Run**.

---

### 5. Penjelasan Singkat Algoritma Brute Force

Secara garis besar, algoritma brute force di `SudokuSolver` kerja seperti ini:

1. Cari sel kosong pertama (scan dari kiri atas sampai kanan bawah).
2. Coba isi angka 1–9:
   - Setiap angka dicek valid/tidak pakai `SudokuValidator` (baris, kolom, subgrid).
3. Kalau ada angka yang valid:
   - Isi sel tersebut, lalu rekursif lanjut ke sel kosong berikutnya.
4. Kalau di satu sel **tidak ada angka yang valid**:
   - **Backtrack**: kosongkan sel tersebut dan balik ke sel sebelumnya untuk coba angka lain.
5. Berhenti saat:
   - Semua sel sudah terisi (solusi ditemukan), atau
   - Semua kemungkinan sudah dicoba dan gagal (tidak ada solusi).

Di mode Solver:
- Setiap langkah bisa divisualisasikan:
  - Sel yang sedang dicoba, angka yang dites, backtrack, dsb.
  - Kecepatan animasi bisa diatur.
- Ada log teks yang menjelaskan proses (cocok buat jelasin ke dosen/temen tentang cara kerja brute force).

Di mode Game:
- Algoritma brute force dipakai untuk:
  - Generate solusi lengkap.
  - Dari solusi itu, dibuat puzzle dengan beberapa sel dikosongkan.
  - Bot pakai solusi ini buat “bermain” dan memperbaiki kesalahan player.

---

### 6. Mode Sudoku Solver

Di `SudokuSolverApp` kamu bisa:

- **Pilih Algoritma**
  - Dropdown untuk memilih algoritma:
    - **Brute Force (Backtracking)** – algoritma klasik deterministik
    - **Harris Hawks Optimization** – algoritma metaheuristic stokastik

- **Load Puzzle**
  - Tombol untuk generate puzzle random
  - Banyak sel kosong diatur secara random (misalnya 40–55 sel kosong)
  - Diklasifikasi jadi `Easy/Medium/Hard` berdasarkan jumlah kosongnya

- **Solve**
  - Jalankan algoritma yang dipilih
  - Ada dua mode:
    - **Dengan animasi**: kelihatan sel diwarnai, angka berubah, proses algoritma
      - Kecepatan bisa diatur: Lambat → Sedang → Cepat → Ultra Fast → Super Ultra Fast
    - **Skip animasi**: tombol untuk langsung ke hasil tanpa visualisasi (jauh lebih cepat)
  - Di akhir, tampil:
    - **Total langkah** yang dilakukan algoritma
    - **Waktu eksekusi (ms)**
    - Untuk HHO: juga menampilkan fitness score

- **Reset**
  - Kosongkan papan dan log.
  - Stop proses solving kalau lagi jalan (pakai `SwingWorker` + `cancel(true)`).

#### File-file yang terlibat di mode Solver

- **`SudokuSolverApp.java`**
  - Frame utama mode Solver: ngatur layout (grid di kiri, log di kanan, tombol di bawah).
  - Di sini juga ada logika buat:
    - Baca puzzle dari grid.
    - Validasi awal puzzle.
    - Mulai proses solving di background (pakai `SwingWorker`).
    - Nampilin hasil (atau pesan kalau tidak ada solusi).

- **`SudokuGUI.java`**
  - Ngasih komponen yang dipakai Solver:
    - `createGridPanel()` buat bikin tampilan kotak-kotak Sudoku.
    - `createButtonPanel()` buat tombol-tombol utama (Load, Solve, Reset, dll).
    - `createLogPanel()` buat area log proses brute force.
  - Juga punya method helper buat:
    - Ngambil semua `JTextField` (grid).
    - Nge-update warna dan isi sel selama animasi solving.

- **`BruteForceSolver.java`** dan **`HarrisHawksSolver.java`**
  - Implementasi algoritma solver yang dipilih user
  - Keduanya implement interface `SudokuSolverAlgorithm`
  - `solve()` dipakai kalau mode cepat (tanpa animasi)
  - `solveWithAnimation()` dipakai kalau mau ada visualisasi:
    - Manggil callback setiap kali ada update (angka ditest, diterima, backtrack, iterasi HHO, dll)
  - Menyimpan jumlah langkah dan bisa lapor balik ke `SudokuSolverApp`

- **`SudokuValidator.java`**
  - Dipakai dua kali:
    - Cek apakah puzzle awal yang dimasukin user valid.
    - Dipakai juga di dalam proses generate puzzle/solusi (jaga aturan Sudoku tetap benar).

- **`PuzzleGenerator.java`**
  - Dipanggil dari `SudokuSolverApp.loadExamplePuzzle()` buat bikin puzzle contoh.
  - Ngatur seberapa banyak sel yang dikosongin biar dapat variasi tingkat kesulitan.

- **`ThemeManager.java` & `SudokuConstants.java`**
  - Ngatur warna, tema, dan ukuran grid yang juga kepake di tampilan mode Solver.

---

### 7. Mode Sudoku Game

Di `SudokuGameApp`:

- **Alur Permainan**
  1. **Pilih Algoritma Bot**: Dropdown untuk memilih Brute Force atau Harris Hawks
  2. Klik **"Mulai"**:
     - Program generate **solusi lengkap** dulu (pakai `BruteForceSolver` untuk generate cepat)
     - Lalu beberapa angka dihapus random untuk jadi puzzle
     - Sel awal (yang sudah terisi) di-lock (non-editable)
  3. **Turn-based**:
     - **Player** isi 1 sel kosong (bebas pilih mana saja).
     - Klik **Submit**.
     - Kalau angkanya:
       - **Valid (tidak melanggar aturan Sudoku)** → Player dapat poin, sel di-lock.
       - **Tidak valid** → Sel diwarnai merah, bot akan memperbaiki dan dapat poin.
     - Setelah giliran player, giliran pindah ke **Bot**.
  3. Bot:
     - Pilih sel kosong berikutnya (urut dari kiri atas ke kanan bawah).
     - Tentukan angka berdasarkan tingkat kesulitan:
       - `Easy` – sering salah (sekitar 50%).
       - `Medium` – kadang salah (sekitar 30%).
       - `Hard` – selalu benar (0% salah).
     - Kalau bot salah:
       - Ditunjukkan di grid + log, lalu giliran balik ke player.
  4. Game berakhir saat semua sel sudah terisi.
     - Muncul popup berisi skor akhir: **Player vs Bot**.

- **Opsi Tambahan**
  - **Show Process Bot**: log detail apa yang dikerjakan bot.
  - **Skip Process Bot**: biar bot gerak cepat tanpa banyak delay.
  - **Dark Mode**: toggle icon 🌙 / ☀️ di bagian kontrol.

#### File-file yang terlibat di mode Game

- **`SudokuGameApp.java`**
  - Frame utama untuk mode game.
  - Ngatur:
    - Papan permainan dan status setiap sel (awal, diisi player, diisi bot).
    - Skor player dan bot.
    - Logika giliran (player dulu, lalu bot, dan seterusnya).
    - Validasi input player sebelum di-accept.

- **`SudokuGUI.java`**
  - Dipakai lagi di sini untuk:
    - Menampilkan grid Sudoku yang sama seperti di mode Solver.
    - Menyediakan panel log untuk cerita langkah-langkah player dan bot.

- **`SudokuSolver.java`**
  - Dipakai di belakang layar untuk:
    - Menyusun solusi lengkap puzzle (full board 9x9).
    - Jadi referensi nilai yang “benar” buat bot dan untuk memperbaiki kesalahan player.

- **`SudokuValidator.java`**
  - Dipakai saat player isi angka:
    - Cek apakah angka yang dimasukkan melanggar aturan Sudoku atau tidak (baris, kolom, subgrid).
  - Kalau tidak valid → dipakai bantu memberi tahu error dan memicu perbaikan oleh bot.

- **`SudokuConstants.java` & `ThemeManager.java`**
  - Sama seperti di mode Solver, dipakai buat:
    - Warna sel (default, benar, salah, diisi bot, dll).
    - Tema gelap/terang dan styling umum.

---

### 8. Lisensi

Proyek ini pake lisensi **MIT** Lihat file [LICENSE](LICENSE) untuk detail lengkap.


