@echo off
echo ========================================
echo   SUDOKU - Multi Algorithm Solver
echo ========================================
echo.
echo Kompilasi aplikasi...
echo.

echo Membersihkan file .class lama...
if exist algorithm\*.class del /Q algorithm\*.class 2>nul
if exist gui\*.class del /Q gui\*.class 2>nul
if exist game\*.class del /Q game\*.class 2>nul
if exist solver\*.class del /Q solver\*.class 2>nul
if exist *.class del /Q *.class 2>nul
echo.

echo - Step 1: Kompilasi interface dan solver algorithms...
javac -encoding UTF-8 -d . algorithm/SudokuSolverAlgorithm.java algorithm/BruteForceSolver.java algorithm/HarrisHawksSolver.java algorithm/SudokuValidator.java algorithm/SudokuSolver.java algorithm/PuzzleGenerator.java
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Kompilasi algorithm gagal!
    pause
    exit /b 1
)
echo   [OK] Algorithm compiled

echo - Step 2: Kompilasi GUI dan utility classes...
javac -encoding UTF-8 -d . gui/SudokuConstants.java gui/ThemeManager.java gui/SudokuGUI.java gui/MainMenu.java
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Kompilasi GUI gagal!
    pause
    exit /b 1
)
echo   [OK] GUI compiled

echo - Step 3: Kompilasi aplikasi utama...
javac -encoding UTF-8 -d . solver/SudokuSolverApp.java game/SudokuGameApp.java Main.java
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Kompilasi aplikasi gagal!
    echo.
    echo Cek apakah semua import sudah benar di file-file berikut:
    echo   - game/SudokuGameApp.java
    echo   - solver/SudokuSolverApp.java
    echo   - Main.java
    pause
    exit /b 1
)
echo   [OK] Application compiled

echo.
echo ========================================
echo Menjalankan aplikasi...
echo ========================================
java Main

pause
