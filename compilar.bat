@echo off
echo ===================================================
echo     Compilando GraphQuest - A Jornada dos Grafos
echo ===================================================

if not exist target\classes mkdir target\classes

dir /s /b src\main\java\*.java > sources.txt
javac -d target\classes -encoding UTF-8 @sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo [ERRO] Falha na compilacao dos fontes Java!
    pause
    exit /b %ERRORLEVEL%
)

echo [SUCESSO] Compilacao concluida com exito!
