@echo off
echo [1/2] Compilando todos los archivos...
javac ModuloComun/*.java
javac -cp . Servicios/*.java
javac ModuloNodo/*.java
javac ModuloServidor/*.java
javac ModuloGestor/*.java
javac ModuloCliente/*.java

echo [2/2] Iniciando el sistema...
start "Nodo Inmune" cmd /k java ModuloNodo.NodoADS 8001
timeout /t 2 >nul

start "Celula 1" cmd /k java ModuloServidor.CelulaServidor 8001
start "Celula 2" cmd /k java ModuloServidor.CelulaServidor 8001
start "Celula 3" cmd /k java ModuloServidor.CelulaServidor 8001

start "Gestor de Servicios" cmd /k java ModuloGestor.GestorServicios 8001
start "Calculadora 1" cmd /k java ModuloCliente.CelulaClienteGUI 8001
start "Calculadora 2" cmd /k java ModuloCliente.CelulaClienteGUI 8001