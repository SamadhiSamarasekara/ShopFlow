@echo off
echo Starting ShopFlow Management System...
echo.

:: Set JavaFX module path (adjust this path based on your JavaFX installation)
set JAVAFX_HOME=C:\Program Files\Java\javafx-sdk-17.0.2\lib
set MODULE_PATH=%JAVAFX_HOME%

:: Run the application with JavaFX modules
java --module-path "%MODULE_PATH%" --add-modules javafx.controls,javafx.fxml -cp "target/classes" com.shop.App

if %ERRORLEVEL% neq 0 (
    echo.
    echo Failed to start application!
    echo.
    echo Make sure JavaFX SDK is installed and the JAVAFX_HOME path is correct.
    echo Current JavaFX path: %JAVAFX_HOME%
    echo.
    pause
) else (
    echo.
    echo Application started successfully!
)

pause
