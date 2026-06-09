@echo off
:: Maven Wrapper para Windows - descarga Maven automáticamente si no está instalado
setlocal EnableDelayedExpansion

set MVN_VERSION=3.9.6
set MAVEN_DIR=%USERPROFILE%\.mvn-wrapper\apache-maven-%MVN_VERSION%

if not exist "%MAVEN_DIR%\bin\mvn.cmd" (
    echo [INFO] Descargando Apache Maven %MVN_VERSION%...
    if not exist "%USERPROFILE%\.mvn-wrapper" mkdir "%USERPROFILE%\.mvn-wrapper"

    powershell -NoProfile -Command ^
        "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12;" ^
        "$url = 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MVN_VERSION%/apache-maven-%MVN_VERSION%-bin.zip';" ^
        "$zip = '%USERPROFILE%\.mvn-wrapper\maven.zip';" ^
        "Invoke-WebRequest -Uri $url -OutFile $zip;" ^
        "Expand-Archive $zip -DestinationPath '%USERPROFILE%\.mvn-wrapper' -Force;" ^
        "Remove-Item $zip"

    if not exist "%MAVEN_DIR%\bin\mvn.cmd" (
        echo [ERROR] No se pudo descargar Maven. Descargue manualmente desde https://maven.apache.org/download.cgi
        exit /b 1
    )
    echo [INFO] Maven %MVN_VERSION% listo.
)

set "PATH=%MAVEN_DIR%\bin;%PATH%"
call "%MAVEN_DIR%\bin\mvn.cmd" %*
endlocal
