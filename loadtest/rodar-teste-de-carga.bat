@echo off
rem Teste de carga do Valtrea Volta: roda tudo no seu computador com o Docker Desktop (grátis), sem GitHub.
rem Mude os números abaixo se quiser outro cenário.
set USUARIOS=100
rem 0.1 = plano grátis do Render (o atual). 0.5 = plano Starter.
set CPUS=0.1
set RAMP=1m
set HOLD=3m

chcp 65001 >nul
cd /d "%~dp0"
docker info >nul 2>&1
if errorlevel 1 (
  echo O Docker Desktop nao esta aberto. Abra o Docker Desktop, espere ficar verde e rode de novo.
  pause
  exit /b 1
)
if not exist resultado mkdir resultado

echo [1/4] Subindo banco e API (a primeira vez demora: baixa e compila tudo)...
docker compose up -d --build db mail api || goto erro
echo [2/4] Criando %USUARIOS% contas de teste (com 0.1 CPU leva alguns minutos)...
docker compose run --rm preparo || goto erro
echo [3/4] Confirmando os e-mails das contas de teste...
docker compose run --rm confirmar || goto erro
echo [4/4] Teste: %USUARIOS% pessoas ao mesmo tempo, API com %CPUS% CPU...
docker compose run --rm carga
docker compose logs api > resultado\api.log 2>&1
docker compose down -v
echo.
echo Pronto. Resultado em: %~dp0resultado\resumo.md
pause
exit /b 0

:erro
echo Algo deu errado. O log da API esta em resultado\api.log
docker compose logs api > resultado\api.log 2>&1
docker compose down -v
pause
exit /b 1
