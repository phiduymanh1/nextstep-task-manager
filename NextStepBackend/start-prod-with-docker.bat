@echo off
echo =======================================
echo  NextStep Backend - DOCKER LOCAL TEST
echo =======================================

REM Di chuyen ve thu muc chua file bat
cd /d %~dp0

REM 1. Build Docker Image
echo [1/2] Dang build Docker image tu Dockerfile...
docker build -t nextstep-backend .

REM Kiem tra neu build loi thi dung lai ngay
if %ERRORLEVEL% neq 0 (
    echo [LOI] Build Docker that bai! Kiem tra lai Dockerfile hoac code.
    pause
    exit /b
)

REM 2. Chay Container voi file .env
echo [2/2] Dang khoi chay Container...
echo Dang su dung file .env de nap cau hinh...
echo Truy cap tai: http://localhost:8080
docker run -p 8080:8080 --env-file .env nextstep-backend

pause