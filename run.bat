@echo off
echo === CV Smart Analyzer ===
echo.

REM Set JAVA_HOME
set JAVA_HOME=%~dp0build-tools\jdk-25

REM Add Java to PATH
set PATH=%JAVA_HOME%\bin;%PATH%

REM Check if JAR file exists
if not exist "target\CVSmartAnalyzer-1.0-SNAPSHOT-jar-with-dependencies.jar" (
    echo ERROR: Application not built yet!
    echo Please run build.bat first.
    echo.
    pause
    exit /b 1
)

echo Starting CV Smart Analyzer...
echo.

java -jar target\CVSmartAnalyzer-1.0-SNAPSHOT-jar-with-dependencies.jar %*

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with an error.
    pause
)
