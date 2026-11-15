@echo off
title Student Expense and Budget Management System (Rupees)
echo Launching Student Expense and Budget Management System...
java -cp bin com.studentexpense.Main
if %errorlevel% neq 0 (
    if exist StudentExpenseApp.jar (
        java -jar StudentExpenseApp.jar
    ) else if exist StudentExpenseManager.jar (
        java -jar StudentExpenseManager.jar
    )
)
pause
