@echo off
echo =============================
echo  NextStep Backend - PROD
echo =============================

REM Di chuyen ve thu muc chua file bat
cd /d %~dp0

REM Load env file
for /f "usebackq tokens=1,* delims==" %%A in (".env") do (
  set %%A=%%B
)

REM Run Spring Boot (JAR nam trong target)
java -jar target\NextStepBackend-0.0.1-SNAPSHOT.jar

pause
