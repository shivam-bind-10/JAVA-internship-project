@echo off
title Student Expense and Budget Management System - Terminal CLI
java -cp bin com.studentexpense.Main --cli
if %errorlevel% neq 0 (
    if exist StudentExpenseApp.jar (
        java -jar StudentExpenseApp.jar --cli
    ) else if exist StudentExpenseManager.jar (
        java -jar StudentExpenseManager.jar --cli
    )
)
pause
