@echo off
echo Kompilasi semua file Java...
echo - Kompilasi interface dan solver algorithms...
javac -encoding UTF-8 -cp . SudokuSolverAlgorithm.java BruteForceSolver.java HarrisHawksSolver.java SudokuValidator.java SudokuConstants.java
echo - Kompilasi GUI dan utility classes...
javac -encoding UTF-8 -cp . SudokuGUI.java ThemeManager.java PuzzleGenerator.java
echo - Kompilasi aplikasi utama...
javac -encoding UTF-8 -cp . solver/SudokuSolverApp.java game/SudokuGameApp.java MainMenu.java Main.java

if %ERRORLEVEL% EQU 0 (
    echo Copy file .class ke root directory...
    copy /Y solver\SudokuSolverApp*.class . >nul 2>&1
    copy /Y game\SudokuGameApp*.class . >nul 2>&1
    echo Kompilasi berhasil!
) else (
    echo Kompilasi gagal!
)

pause






