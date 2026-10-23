@echo off
chcp 65001 > nul
cd /d "%~dp0"
echo ============================================================
echo   COMPILING & RUNNING HOTEL MANAGEMENT JAVA JDBC BACKEND     
echo ============================================================
if not exist "bin" mkdir bin

echo Compiling Java source files...
javac -encoding UTF-8 -cp "lib\mysql-connector-j.jar" -d bin src\com\hotel\*.java

if %ERRORLEVEL% equ 0 (
    echo [SUCCESS] Compilation complete with 0 errors!
    echo.
    java -Dfile.encoding=UTF-8 -cp "bin;lib\mysql-connector-j.jar" com.hotel.HotelApp
) else (
    echo [ERROR] Compilation failed!
)
pause
