@echo off
echo === Building CV Smart Analyzer ===
echo.

REM Set JAVA_HOME
set JAVA_HOME=%~dp0build-tools\jdk-25

REM Add Java and Maven to PATH
set PATH=%JAVA_HOME%\bin;%~dp0build-tools\maven\bin;%PATH%

REM Verify Java
echo Checking Java...
java -version
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java not found!
    pause
    exit /b 1
)

echo.
echo Checking Maven...
call mvn -version
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven not found!
    pause
    exit /b 1
)

echo.
pushd "%~dp0"
echo Building project...
echo This may take several minutes as dependencies are downloaded...
echo.

call mvn clean package

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ===================================
    echo BUILD SUCCESSFUL!
    echo ===================================
    echo.
    echo The application JAR file is located at:
    echo   target\CVSmartAnalyzer-1.0-SNAPSHOT-jar-with-dependencies.jar
    echo.
    echo To run the application:
    echo   run.bat
    echo.
) else (
    echo.
    echo ===================================
    echo BUILD FAILED!
    echo ===================================
    echo Please check the error messages above.
    echo.
)

popd
pause
