@echo off
echo ===================================================
echo     Executando Testes Automatizados - GraphQuest
echo ===================================================

call compilar.bat
if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%

java -Dgraphquest.profile=target\perfil-testes.properties -cp target\classes br.com.graphquest.testes.TestesGrafo
if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%

where mvn >nul 2>nul
if errorlevel 1 goto sem_maven
mvn test
exit /b %ERRORLEVEL%

:sem_maven
echo [AVISO] Maven nao encontrado. Os testes JUnit podem ser executados com "mvn test" em um ambiente com Maven.
exit /b 0
