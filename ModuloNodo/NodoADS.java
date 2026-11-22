package ModuloNodo;

import ModuloComun.Mensaje;
import java.io.*;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class NodoADS {
    private static final CopyOnWriteArrayList<ManejadorCliente> clientes = new CopyOnWriteArrayList<>();
    
    // 3 Células + 1 Gestor + 2 GUIs = 6 conexiones normales. Si baja de 6, falta una célula.
    private static final int CONEXIONES_MINIMAS = 6;
    private static boolean recuperando = false;

    public static void main(String[] args) {
        int puerto = 8001;
        if (args.length > 0) {
            puerto = Integer.parseInt(args[0]);
        }

        System.out.println("==========================================");
        System.out.println("   NODO ADS INICIADO EN PUERTO: " + puerto);
        System.out.println("==========================================");

        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            while (true) {
                Socket socket = serverSocket.accept();
                ManejadorCliente manejador = new ManejadorCliente(socket);
                clientes.add(manejador);
                System.out.println("[+] Nueva conexión establecida. Total: " + clientes.size());
                
                if (clientes.size() >= CONEXIONES_MINIMAS) {
                    recuperando = false; // Apaga la alarma cuando ya se recuperó
                }
                
                new Thread(manejador).start();
            }
        } catch (IOException e) {
            System.err.println("[-] Error crítico en el servidor: " + e.getMessage());
        }
    }

    public static void retransmitir(Mensaje msg, ManejadorCliente origen) {
        for (ManejadorCliente cliente : clientes) {
            if (cliente != origen) {
                cliente.enviarMensaje(msg);
            }
        }
    }

    static class ManejadorCliente implements Runnable {
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;

        public ManejadorCliente(Socket socket) {
            this.socket = socket;
            try {
                this.out = new ObjectOutputStream(socket.getOutputStream());
                this.out.flush();
                this.in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {}
        }

        public synchronized void enviarMensaje(Mensaje msg) {
            try {
                out.writeObject(msg);
                out.reset();
                out.flush();
            } catch (IOException e) {}
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Mensaje msg = (Mensaje) in.readObject();
                    if (msg != null) {
                        retransmitir(msg, this);
                    }
                }
            } catch (Exception e) {
            } finally {
                clientes.remove(this);
                System.out.println("[-] Conexión cerrada. Total conectados: " + clientes.size());
                
                // Si se cae una célula y no estamos ya clonando una, levanta el reemplazo con Suma activa
                if (clientes.size() < CONEXIONES_MINIMAS && !recuperando) {
                    recuperando = true;
                    System.out.println("[!] ALERTA: Célula caída detectada. Clonando reemplazo...");
                    try {
                        Runtime.getRuntime().exec("cmd /c start \"CLON RECUPERACION\" cmd /k \"title CLON RECUPERACION && java ModuloServidor.CelulaServidor 8001 Suma\"");
                    } catch (IOException ex) {
                        System.err.println("[-] Error al clonar: " + ex.getMessage());
                    }
                }
                
                try { socket.close(); } catch (IOException e) {}
            }
        }
    }
}