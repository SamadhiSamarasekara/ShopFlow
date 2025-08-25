@echo off
echo Testing MySQL Database Connection...

:: Test database connection first
echo.
echo Running database connection test...
call mvn exec:java -Dexec.mainClass="com.shop.test.DatabaseConnectionTest"

if %ERRORLEVEL% neq 0 (
    echo.
    echo Database connection test failed!
    echo Please check:
    echo 1. XAMPP is running
    echo 2. MySQL service is started
    echo 3. Database 'shopdb' exists in phpMyAdmin
    echo 4. Database configuration in database.properties is correct
    pause
    exit /b 1
)

echo.
echo Database connection successful! Starting application...
echo.

:: Run the JavaFX application
call mvn javafx:run

if %ERRORLEVEL% neq 0 (
    echo Failed to start application!
    pause
    exit /b 1
)

pause
