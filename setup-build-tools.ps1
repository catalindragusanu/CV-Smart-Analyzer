# PowerShell script to download and install Java and Maven
# Run this script as Administrator for system-wide installation
# Or run as regular user for user-level installation

$ErrorActionPreference = "Stop"

Write-Host "=== CV Smart Analyzer - Build Tools Setup ===" -ForegroundColor Cyan
Write-Host ""

# Create a tools directory in the project
$projectRoot = $PSScriptRoot
$toolsDir = Join-Path $projectRoot "build-tools"

if (-not (Test-Path $toolsDir)) {
    New-Item -ItemType Directory -Path $toolsDir | Out-Null
}

Write-Host "Tools will be installed to: $toolsDir" -ForegroundColor Yellow
Write-Host ""

# Download and install OpenJDK 25 (LTS)
$jdkDir = Join-Path $toolsDir "jdk-25"
if (-not (Test-Path $jdkDir)) {
    Write-Host "Downloading OpenJDK 25..." -ForegroundColor Green

    $jdkUrl = "https://api.adoptium.net/v3/binary/latest/25/ga/windows/x64/jdk/hotspot/normal/eclipse"
    $jdkZip = Join-Path $toolsDir "openjdk-25.zip"

    try {
        Invoke-WebRequest -Uri $jdkUrl -OutFile $jdkZip -UseBasicParsing
        Write-Host "Extracting JDK..." -ForegroundColor Green
        Expand-Archive -Path $jdkZip -DestinationPath $toolsDir -Force

        $extractedFolder = Get-ChildItem $toolsDir -Directory |
            Where-Object { $_.Name -like "jdk-25*" } |
            Select-Object -First 1
        if ($extractedFolder) {
            Rename-Item -Path $extractedFolder.FullName -NewName "jdk-25" -Force
        } else {
            throw "Could not find the extracted JDK 25 directory."
        }

        Remove-Item $jdkZip -Force
        Write-Host "OpenJDK 25 installed successfully!" -ForegroundColor Green
    } catch {
        Write-Host "Failed to download or install JDK 25." -ForegroundColor Yellow
        Write-Host "Please download JDK 25 manually from: https://adoptium.net/temurin/releases/?version=25" -ForegroundColor Yellow
        Write-Host "Extract it to: $jdkDir" -ForegroundColor Yellow
    }
} else {
    Write-Host "OpenJDK 25 already installed." -ForegroundColor Green
}

# Download and install Maven
$mavenDir = Join-Path $toolsDir "maven"
if (-not (Test-Path $mavenDir)) {
    Write-Host "Downloading Apache Maven..." -ForegroundColor Green

    $mavenVersion = "3.9.15"
    $mavenUrl = "https://dlcdn.apache.org/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
    $mavenZip = Join-Path $toolsDir "maven.zip"

    try {
        Invoke-WebRequest -Uri $mavenUrl -OutFile $mavenZip -UseBasicParsing
        Write-Host "Extracting Maven..." -ForegroundColor Green
        Expand-Archive -Path $mavenZip -DestinationPath $toolsDir -Force

        # Rename extracted folder to maven
        $extractedFolder = Get-ChildItem $toolsDir -Directory | Where-Object { $_.Name -like "apache-maven-*" } | Select-Object -First 1
        if ($extractedFolder) {
            Rename-Item -Path $extractedFolder.FullName -NewName "maven" -Force
        }

        Remove-Item $mavenZip -Force
        Write-Host "Apache Maven installed successfully!" -ForegroundColor Green
    } catch {
        Write-Host "Failed to download Maven. Trying alternative mirror..." -ForegroundColor Yellow
        $mavenUrlBackup = "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
        try {
            Invoke-WebRequest -Uri $mavenUrlBackup -OutFile $mavenZip -UseBasicParsing
            Expand-Archive -Path $mavenZip -DestinationPath $toolsDir -Force
            $extractedFolder = Get-ChildItem $toolsDir -Directory | Where-Object { $_.Name -like "apache-maven-*" } | Select-Object -First 1
            if ($extractedFolder) {
                Rename-Item -Path $extractedFolder.FullName -NewName "maven" -Force
            }
            Remove-Item $mavenZip -Force
            Write-Host "Apache Maven installed successfully!" -ForegroundColor Green
        } catch {
            Write-Host "Please download Maven manually from: https://maven.apache.org/download.cgi" -ForegroundColor Yellow
            Write-Host "Extract it to: $mavenDir" -ForegroundColor Yellow
        }
    }
} else {
    Write-Host "Apache Maven already installed." -ForegroundColor Green
}

Write-Host ""
Write-Host "=== Setting up environment variables ===" -ForegroundColor Cyan

# Set JAVA_HOME and add to PATH for current session
$javaHome = $jdkDir
$mavenHome = $mavenDir

$env:JAVA_HOME = $javaHome
$env:PATH = "$javaHome\bin;$mavenHome\bin;$env:PATH"

Write-Host "JAVA_HOME set to: $javaHome" -ForegroundColor Green
Write-Host "Maven bin added to PATH" -ForegroundColor Green
Write-Host ""

# Verify installations
Write-Host "=== Verifying installations ===" -ForegroundColor Cyan
Write-Host ""

if (Test-Path "$javaHome\bin\java.exe") {
    Write-Host "Java version:" -ForegroundColor Yellow
    & "$javaHome\bin\java.exe" -version
} else {
    Write-Host "Java not found! Please check the installation." -ForegroundColor Red
}

Write-Host ""

if (Test-Path "$mavenHome\bin\mvn.cmd") {
    Write-Host "Maven version:" -ForegroundColor Yellow
    & "$mavenHome\bin\mvn.cmd" -version
} else {
    Write-Host "Maven not found! Please check the installation." -ForegroundColor Red
}

Write-Host ""
Write-Host "=== Setup Instructions ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "The build tools are now installed for this session only." -ForegroundColor Yellow
Write-Host ""
Write-Host "To build the project, run:" -ForegroundColor Green
Write-Host "  cd $projectRoot" -ForegroundColor White
Write-Host "  $mavenHome\bin\mvn.cmd clean package" -ForegroundColor White
Write-Host ""
Write-Host "To make these changes permanent (optional):" -ForegroundColor Green
Write-Host "  1. Search for 'Environment Variables' in Windows" -ForegroundColor White
Write-Host "  2. Click 'Environment Variables' button" -ForegroundColor White
Write-Host "  3. Under 'User variables', click 'New'" -ForegroundColor White
Write-Host "  4. Variable name: JAVA_HOME" -ForegroundColor White
Write-Host "     Variable value: $javaHome" -ForegroundColor White
Write-Host "  5. Edit 'Path' variable and add:" -ForegroundColor White
Write-Host "     %JAVA_HOME%\bin" -ForegroundColor White
Write-Host "     $mavenHome\bin" -ForegroundColor White
Write-Host ""
Write-Host "Setup complete!" -ForegroundColor Green
