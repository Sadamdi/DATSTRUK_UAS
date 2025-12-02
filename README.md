## DATSTRUK_UAS – Project Sudoku (Struktur Data UAS)

Repo ini isinya proyek UAS Struktur Data berupa aplikasi **Sudoku** berbasis **Java Swing**.  
Di sini ada 2 mode utama:

- **Sudoku Solver** – mode buat **menyelesaikan puzzle Sudoku** pakai algoritma **Brute Force** + animasi langkah-langkahnya.
- **Sudoku Game** – mode **bermain Sudoku lawan bot** yang juga pakai algoritma **Brute Force**, tapi dibungkus kayak game turn-based (player vs komputer).

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

### 2. Struktur Kelas Utama

#### 2.1 Ringkasan kelas-kelas penting

- **`MainMenu.java`**
  - Tampilan menu awal.
  - Ada tombol ke:
    - Mode **Solver** (`SudokuSolverApp`)
    - Mode **Game** (`SudokuGameApp`)

- **`SudokuGUI.java`**
  - Kelas yang ngatur tampilan:
    - Grid 9x9 (`JTextField`).
    - Panel tombol (Solve, Reset, Load Example, dll).
    - Panel log (textarea) untuk menampilkan proses.
  - Dipakai oleh **dua mode**: `SudokuSolverApp` dan `SudokuGameApp`.

- **`SudokuSolver.java`**
  - Implementasi **algoritma brute force** buat nyelesaiin Sudoku.
  - Punya:
    - `solve()` – versi cepat tanpa animasi.
    - `solveWithAnimation()` – versi yang manggil callback ke GUI (update sel, warna, log, dll).
  - Ada **`SolverCallback`**:
    - `onCellProcessing`, `onCellTesting`, `onCellSolved`, `onBacktrack`, `onLog`, `updateCell`, `sleep`, dll.
    - Ini yang dipakai buat animasi per langkah di mode Solver.

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

#### 2.2 Tiga file utama

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

### 3. Cara Menjalankan Proyek

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

### 4. Penjelasan Singkat Algoritma Brute Force

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

### 5. Mode Sudoku Solver

Di `SudokuSolverApp` kamu bisa:

- **Load Puzzle**
  - Tombol untuk generate puzzle random.
  - Banyak sel kosong diatur secara random (misalnya 40–55 sel kosong), lalu diklasifikasi jadi `Easy/Medium/Hard` berdasarkan jumlah kosongnya.

- **Solve**
  - Jalanin algoritma brute force.
  - Ada dua mode:
    - **Dengan animasi**: kelihatan sel diwarnai, angka berubah, backtrack, dsb.
    - **Skip animasi**: langsung cari solusi tanpa visualisasi (lebih cepat).
  - Di akhir, tampil:
    - **Total langkah** yang dilakukan algoritma.
    - **Waktu eksekusi (ms)**.

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

- **`SudokuSolver.java`**
  - Isinya algoritma brute force buat nyelesain Sudoku.
  - `solve()` dipakai kalau mode cepat (tanpa animasi).
  - `solveWithAnimation()` dipakai kalau mau ada visualisasi:
    - Manggil callback setiap kali:
      - Nyoba angka di sel tertentu.
      - Angka diterima/ditolak.
      - Terjadi backtrack.
  - Menyimpan jumlah langkah dan bisa lapor balik ke `SudokuSolverApp`.

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

### 6. Mode Sudoku Game

Di `SudokuGameApp`:

- **Alur Permainan**
  1. Klik **"Mulai"**:
     - Program generate **solusi lengkap** dulu (pakai `SudokuSolver`).
     - Lalu beberapa angka dihapus random untuk jadi puzzle.
     - Sel awal (yang sudah terisi) di-lock (non-editable).
  2. **Turn-based**:
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

### 7. Lisensi

Proyek ini pake lisensi **MIT** (lihat file `LICENSE` di repo ini).


