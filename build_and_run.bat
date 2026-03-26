@echo off
REM ============================================================
REM  build_and_run.bat  –  Windows Build & Run Script
REM  Double-click this file OR run it from Command Prompt
REM ============================================================

echo =============================================
echo   Smart Surveillance System - Build Script
echo =============================================
echo.

REM --- STEP 1: Set your OpenCV path here ---
REM Change this to wherever you extracted OpenCV!
SET OPENCV_VERSION=4100
SET OPENCV_DIR=C:\opencv\build\java

REM OpenCV JAR file (contains Java bindings)
SET OPENCV_JAR=%OPENCV_DIR%\opencv-%OPENCV_VERSION%.jar

REM OpenCV native DLL (the actual C++ engine)
SET OPENCV_DLL_DIR=%OPENCV_DIR%\x64

echo [1] Checking OpenCV installation...
IF NOT EXIST "%OPENCV_JAR%" (
    echo.
    echo ERROR: OpenCV JAR not found at:
    echo   %OPENCV_JAR%
    echo.
    echo Please:
    echo   1. Download OpenCV from https://opencv.org/releases/
    echo   2. Extract to C:\opencv\
    echo   3. Update OPENCV_DIR in this script
    echo.
    pause
    exit /b 1
)
echo     Found: %OPENCV_JAR%
echo.

REM --- STEP 2: Create output and capture folders ---
IF NOT EXIST "out"              mkdir out
IF NOT EXIST "captured_images"  mkdir captured_images
IF NOT EXIST "logs"             mkdir logs

REM --- STEP 3: Compile all Java source files ---
echo [2] Compiling Java source files...
javac -cp "%OPENCV_JAR%" -d out src\*.java

IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Compilation failed! Check the error messages above.
    pause
    exit /b 1
)
echo     Compilation successful!
echo.

REM --- STEP 4: Run the application ---
echo [3] Starting Smart Surveillance System...
echo.
java -cp "out;%OPENCV_JAR%" -Djava.library.path="%OPENCV_DLL_DIR%" Main

pause
