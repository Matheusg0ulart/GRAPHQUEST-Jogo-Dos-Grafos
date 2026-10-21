@echo off
echo ===================================================
echo     Iniciando GraphQuest - A Jornada dos Grafos
echo ===================================================

if not exist target\classes (
    echo Compilando aplicacao antes de iniciar...
    call compilar.bat
)

rem Limita o uso de memoria e evita compilacoes C2 pesadas em computadores modestos.
javaw -Xms64m -Xmx512m -XX:TieredStopAtLevel=1 -XX:CICompilerCount=2 -cp target\classes br.com.graphquest.Main %*
