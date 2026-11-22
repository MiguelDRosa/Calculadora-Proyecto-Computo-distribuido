package ModuloServidor;

import ModuloComun.Mensaje;
import ModuloComun.IMicroservicio;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class CelulaServidor {
    private IMicroservicio genActivo = null;
    private String idServidor;
    private String archivoHistorial;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public CelulaServidor(String host, int puertoNodo) {
        this.idServidor = "SRV-" + UUID.randomUUID().toString().substring(0, 5);
        this.archivoHistorial = "historial_" + idServidor + ".txt";
        System.out.println("==========================================");
        System.out.println("   CELULA SERVIDOR INICIADA (VACIA)");
        System.out.println("   ID: " + idServidor);
        System.out.println("==========================================");
        conectarAlNodo(host, puertoNodo);
    }

    public void cargarGenActivo(String nombreClase) {
        try {
            File file = new File("./Servicios/");
            URL url = file.toURI().toURL();
            URLClassLoader classLoader = new URLClassLoader(new URL[]{url});
            Class<?> claseGen = classLoader.loadClass(nombreClase);
            genActivo = (IMicroservicio) claseGen.getDeclaredConstructor().newInstance();
            guardarHistorial("GEN CARGADO: " + nombreClase);
            System.out.println("[+] Gen activo instalado: " + nombreClase);
        } catch (Exception e) {
            System.err.println("[-] Error al cargar gen: " + e.getMessage());
        }
    }

    private void conectarAlNodo(String host, int puerto) {
        try {
            Socket socket = new Socket(host, puerto);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            guardarHistorial("Conectado al Nodo ADS en puerto " + puerto);
            System.out.println("[*] Conectado a la malla. Esperando instrucciones...");
            escucharCampoDeDatos();
        } catch (IOException e) {
            System.err.println("[-] No se pudo conectar al Nodo en el puerto " + puerto);
        }
    }

    private void escucharCampoDeDatos() {
        try {
            while (true) {
                Mensaje msg = (Mensaje) in.readObject();
                
                if (msg.tipoMensaje.equals("TOPO")) {
                    String estado = (genActivo != null) ? genActivo.getCodigoRequerimiento() : "VACIA";
                    Mensaje resTopo = new Mensaje("TOPO_RES", estado, msg.folio, 0.0, idServidor, msg.huella);
                    out.writeObject(resTopo);
                    out.flush();
                } else if (msg.tipoMensaje.equals("ADMIN") && msg.folio.equals(this.idServidor)) {
                    if (msg.codigoContenido.startsWith("LOAD:")) {
                        String clase = msg.codigoContenido.split(":")[1];
                        cargarGenActivo(clase);
                    } else if (msg.codigoContenido.equals("UNLOAD")) {
                        genActivo = null;
                        System.out.println("[-] Servicio dado de baja. Celula vacia nuevamente.");
                        guardarHistorial("SERVICIO DESCARGADO");
                    }
                } else if (genActivo != null && msg.tipoMensaje.equals("REQ") && msg.codigoContenido.equals(genActivo.getCodigoRequerimiento())) {
                    guardarHistorial("PETICION ATRAPADA | Folio: " + msg.folio);
                    System.out.println("\n[!] Resolviendo folio: " + msg.folio);
                    
                    Mensaje ack = new Mensaje("ACK", "ACK_RECEPCION", msg.folio, 0.0, idServidor, msg.huella);
                    out.writeObject(ack);
                    out.flush();
                    
                    double resultado = genActivo.calcular(msg.num1, msg.num2);
                    Mensaje respuesta = new Mensaje("RES", genActivo.getCodigoRespuesta(), msg.folio, resultado, idServidor, msg.huella);
                    out.writeObject(respuesta);
                    out.flush();
                    System.out.println("    -> Respuesta enviada.");
                } else if (msg.tipoMensaje.equals("CLONE") && genActivo == null) {
                    System.out.println("[!] Solicitud de clonacion recibida para: " + msg.codigoContenido);
                    try {
                        Thread.sleep((long)(Math.random() * 3000));
                        if (genActivo == null) {
                            String gen = "";
                            if (msg.codigoContenido.contains("SUM")) gen = "Suma";
                            if (msg.codigoContenido.contains("SUB")) gen = "Resta";
                            if (msg.codigoContenido.contains("MUL")) gen = "Multiplicacion";
                            if (msg.codigoContenido.contains("DIV")) gen = "Division";
                            if (!gen.isEmpty()) {
                                cargarGenActivo(gen);
                                System.out.println("[+] Falla mitigada. Yo reemplazo el servicio perdido.");
                            }
                        }
                    } catch (InterruptedException e) {}
                }
            }
        } catch (Exception e) {}
    }

    private synchronized void guardarHistorial(String evento) {
        String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        try (FileWriter fw = new FileWriter(archivoHistorial, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println("[" + fechaHora + "] " + evento);
        } catch (IOException e) {}
    }

    public static void main(String[] args) {
        int puerto = 8001; 
        if (args.length > 0) {
            puerto = Integer.parseInt(args[0]);
        }
        new CelulaServidor("localhost", puerto);
    }
}