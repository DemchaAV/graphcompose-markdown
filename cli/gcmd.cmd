@echo off
rem gcmd - Markdown -> PDF launcher (Windows). Resolves the fat-jar next to this script,
rem so it works from any directory. Add this cli\ folder to PATH to call "gcmd" anywhere.
setlocal
set "JAR=%~dp0target\graph-compose-markdown-cli.jar"
if not exist "%JAR%" (
    echo gcmd: jar not found: "%JAR%" 1>&2
    echo Build it first: 1>&2
    echo     mvnw -q -ntp install -DskipTests 1>&2
    echo     mvnw -f cli\pom.xml -q -ntp package 1>&2
    exit /b 1
)
java -jar "%JAR%" %*
