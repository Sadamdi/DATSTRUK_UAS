@echo off
echo ========================================
echo   SUDOKU - Multi Algorithm Solver
echo ========================================
echo.
echo Kompilasi aplikasi...
echo - Kompilasi interface dan solver algorithms...
javac -encoding UTF-8 -cp . SudokuSolverAlgorithm.java BruteForceSolver.java HarrisHawksSolver.java SudokuValidator.java SudokuConstants.java SudokuSolver.java
echo - Kompilasi GUI dan utility classes...
javac -encoding UTF-8 -cp . SudokuGUI.java ThemeManager.java PuzzleGenerator.java
echo - Kompilasi aplikasi utama...
javac -encoding UTF-8 -cp . solver/SudokuSolverApp.java game/SudokuGameApp.java MainMenu.java Main.java

if %ERRORLEVEL% NEQ 0 (
    echo Kompilasi gagal!
    pause
    exit /b 1
)

echo Copy file .class ke root directory...
copy /Y solver\SudokuSolverApp*.class . >nul 2>&1
copy /Y game\SudokuGameApp*.class . >nul 2>&1

echo Menjalankan aplikasi...
java Main

pause






