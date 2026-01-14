@echo off
REM Startup script for STS Help middleware server (Windows)

echo Starting STS Help Middleware Server
echo ======================================

REM Check if Python is installed
python --version >nul 2>&1
if errorlevel 1 (
    echo Error: Python is not installed
    exit /b 1
)

REM Check if we're in the right directory
if not exist server.py (
    echo Warning: server.py not found in current directory
    echo Changing to middleware directory...
    cd middleware 2>nul
    if errorlevel 1 (
        echo Error: Cannot find middleware directory
        exit /b 1
    )
)

REM Check if requirements are installed
python -c "import flask" >nul 2>&1
if errorlevel 1 (
    echo Installing requirements...
    pip install -r requirements.txt
    if errorlevel 1 (
        echo Error: Failed to install requirements
        exit /b 1
    )
)

REM Check for configuration
if not exist config.json (
    echo Warning: config.json not found
    echo You can create it from config.example.json or use environment variables
    echo.
    
    if "%OPENAI_API_KEY%"=="" (
        echo Error: No config.json and OPENAI_API_KEY environment variable not set
        echo Please either:
        echo   1. Copy config.example.json to config.json and edit it, or
        echo   2. Set OPENAI_API_KEY environment variable
        exit /b 1
    )
)

REM Start the server
echo Starting server on port %PORT%...
echo Press Ctrl+C to stop
echo.

python server.py
