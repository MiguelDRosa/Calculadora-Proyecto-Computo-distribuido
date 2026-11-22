package ModuloNodo;

import ModuloComun.Mensaje;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class NodoADS {
    private int puerto;
    private String idNodo;
    private String archivoHistorial;
    
    private List<ObjectOutputStream> conexionesActivas = new CopyOnWriteArrayList<>();
    private Set<String> mensajesProcesados = ConcurrentHashMap.newKeySet();
    private Map<ObjectOutputStream, String> serviciosConectados = new ConcurrentHashMap<>();

    public NodoADS(int puerto) {
        this.puerto = puerto;
        this.idNodo = "NODO-" + puerto;
        this.archivoHistorial = "historial_nodo_" + puerto + ".txt";
    }

    public void iniciar() {
        System.out.println("==========================================");
        System.out.println("   NODO ADS INICIADO EN PUERTO: " + puerto);
        System.out.println("==========================================");
        guardarEnHistorial("NODO INICIADO - Escuchando en puerto " + puerto);

        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(puerto)) {
                while (true) {
                    Socket cliente = serverSocket.accept();
                    ObjectOutputStream out = new ObjectOutputStream(cliente.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(cliente.getInputStream());
                    
                    conexionesActivas.add(out);
                    guardarEnHistorial("NUEVA CONEXIÓN RECIBIDA.");
                    System.out.println("[+] Nueva conexión establecida. Total: " + conexionesActivas.size());
                    
                    new Thread(() -> escucharConexion(in, out)).start();
                }
            } catch (IOException e) {
                System.err.println("Error en el Nodo: " + e.getMessage());
            }
        }).start();
    }

    public void conectarConOtroNodo(String host, int puertoDestino) {
        try {
            Socket socket = new Socket(host, puertoDestino);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            
            conexionesActivas.add(out);
            guardarEnHistorial("CONECTADO A NODO VECINO EN PUERTO " + puertoDestino);
            new Thread(() -> escucharConexion(in, out)).start();
        } catch (Exception e) {}
    }

    private void escucharConexion(ObjectInputStream in, ObjectOutputStream outPropio) {
        try {
            while (true) {
                Mensaje msg = (Mensaje) in.readObject();
                
                if (mensajesProcesados.contains(msg.idMensaje)) continue;
                mensajesProcesados.add(msg.idMensaje);

                if (msg.idRemitente.startsWith("SRV-") && !msg.codigoContenido.equals("VACIA")) {
                    serviciosConectados.put(outPropio, msg.codigoContenido);
                }
                
                guardarEnHistorial("BROADCAST: " + msg.tipoMensaje + " | " + msg.codigoContenido);
                difundirMensaje(msg, outPropio);
            }
        } catch (Exception e) {
            conexionesActivas.remove(outPropio);
            System.out.println("[-] Conexión cerrada. Total conectados: " + conexionesActivas.size());
            
            String servicioPerdido = serviciosConectados.remove(outPropio);
            if (servicioPerdido != null && !servicioPerdido.equals("VACIA")) {
                System.out.println("[!] ALERTA: Falla detectada en servicio asociado a: " + servicioPerdido);
                System.out.println("[!] Disparando protocolo de clonación...");
                
                Mensaje msgClone = new Mensaje("CLONE", servicioPerdido, 0.0, 0.0, idNodo);
                difundirMensaje(msgClone, null);
                guardarEnHistorial("CLONACIÓN SOLICITADA PARA: " + servicioPerdido);
            }
        }
    }

    private void difundirMensaje(Mensaje msg, ObjectOutputStream outOrigen) {
        for (ObjectOutputStream out : conexionesActivas) {
            if (out != outOrigen) {
                try {
                    out.writeObject(msg);
                    out.flush();
                } catch (IOException e) {}
            }
        }
    }

    private synchronized void guardarEnHistorial(String evento) {
        String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        try (FileWriter fw = new FileWriter(archivoHistorial, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println("[" + fechaHora + "] " + evento);
        } catch (IOException e) {}
    }

    public static void main(String[] args) {
        int puertoAsignado = -1;
        if (args.length > 0) {
            puertoAsignado = Integer.parseInt(args[0]);
        } else {
            for (int p = 8001; p <= 8020; p++) {
                try (ServerSocket ss = new ServerSocket(p)) {
                    puertoAsignado = p;
                    break; 
                } catch (IOException e) {}
            }
        }
        if (puertoAsignado == -1) return;

        NodoADS miNodo = new NodoADS(puertoAsignado);
        miNodo.iniciar();

        for (int p = 8001; p < puertoAsignado; p++) {
            miNodo.conectarConOtroNodo("localhost", p);
        }
    }
}