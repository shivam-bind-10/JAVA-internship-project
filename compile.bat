@echo off
title Compiling Student Expense and Budget Management System
echo ===============================================================
echo Compiling Student Expense and Budget Management System...
echo ===============================================================

if not exist bin mkdir bin

dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d bin @sources.txt
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed!
    del sources.txt
    pause
    exit /b %errorlevel%
)
del sources.txt

echo.
echo Packaging into executable JAR: StudentExpenseApp.jar...
jar --create --file StudentExpenseApp.jar --main-class com.studentexpense.Main -C bin .

rem Also update StudentExpenseManager.jar if not locked by running process
copy /Y StudentExpenseApp.jar StudentExpenseManager.jar >nul 2>&1

echo.
echo ===============================================================
echo Build Successful! Run 'run.bat' for GUI or 'run_cli.bat' for CLI.
echo ===============================================================
pause
