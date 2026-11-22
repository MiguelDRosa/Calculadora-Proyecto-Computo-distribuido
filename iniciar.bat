@echo off
echo ==================================================
echo   COMPILANDO PROYECTO ADS - TERCER PARCIAL
echo ==================================================
javac -cp . ModuloComun\*.java ModuloNodo\*.java ModuloServidor\*.java ModuloCliente\*.java ModuloGestor\*.java Servicios\*.java
if %errorlevel% neq 0 (
    echo Error de compilacion. Revisa el codigo.
    pause
    exit
)

echo Iniciando Nodo ADS...
start "NODO ADS (8001)" cmd /k "title NODO ADS (8001) && java ModuloNodo.NodoADS 8001"
timeout /t 2 /nobreak >nul

echo Iniciando 3 Celulas Servidoras (Vacias)...
start "CELULA 1" cmd /k "title CELULA 1 && java ModuloServidor.CelulaServidor 8001"
start "CELULA 2" cmd /k "title CELULA 2 && java ModuloServidor.CelulaServidor 8001"
start "CELULA 3" cmd /k "title CELULA 3 && java ModuloServidor.CelulaServidor 8001"

echo Iniciando Gestor de Servicios...
start "GESTOR SERVICIOS" cmd /k "title GESTOR SERVICIOS && java ModuloGestor.GestorServicios 8001"

echo Iniciando 2 Calculadoras GUI (Simultaneidad y Foliado)...
start "LOGS GUI 1" cmd /k "title LOGS GUI 1 && java ModuloCliente.CelulaClienteGUI 8001"
start "LOGS GUI 2" cmd /k "title LOGS GUI 2 && java ModuloCliente.CelulaClienteGUI 8001"
exit