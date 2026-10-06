@echo off
if not exist out mkdir out
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt
if errorlevel 1 (
    echo.
    echo Compilation failed.
    del sources.txt 2>nul
    pause
    exit /b 1
)
del sources.txt 2>nul
java -cp out in.edu.tint.it.salon.SalonApp
pause
