@echo off
if not exist out mkdir out
javac -d out src\salon\*.java
if errorlevel 1 (
    echo.
    echo Compilation failed.
    pause
    exit /b 1
)
java -cp out salon.SalonApp
pause
