@echo off
javac -cp "lib/sqlite-jdbc.jar" -d . StudentExpenseTracker.java
if errorlevel 1 pause & exit /b 1
java -cp ".;lib/sqlite-jdbc.jar" student.expenses.StudentExpenseTracker
pause
