package ModuloGestor;

import ModuloComun.Mensaje;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class GestorServicios extends JFrame {
    private String idGestor = "GESTOR-" + UUID.randomUUID().toString().substring(0, 4);
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private DefaultTableModel modeloTabla;
    private JTable tablaCelulas;

    public GestorServicios(String host, int puerto) {
        crearInterfaz();
        conectarAlNodo(host, puerto);
    }

    private void crearInterfaz() {
        setTitle("Panel de Control ADS - " + idGestor);
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        
        String[] columnas = {"ID Célula", "Servicio Actual"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tablaCelulas = new JTable(modeloTabla);
        add(new JScrollPane(tablaCelulas), BorderLayout.CENTER);

        
        JPanel panelBotones = new JPanel(new GridLayout(2, 3, 5, 5));
        
        JButton btnTopo = new JButton("Actualizar Topología");
        JButton btnSuma = new JButton("Inyectar Suma");
        JButton btnResta = new JButton("Inyectar Resta");
        JButton btnMult = new JButton("Inyectar Multiplicación");
        JButton btnDiv = new JButton("Inyectar División");
        JButton btnVaciar = new JButton("Vaciar Célula (UNLOAD)");

        btnTopo.addActionListener(e -> solicitarTopologia());
        btnSuma.addActionListener(e -> enviarComandoAdmin("LOAD:Suma"));
        btnResta.addActionListener(e -> enviarComandoAdmin("LOAD:Resta"));
        btnMult.addActionListener(e -> enviarComandoAdmin("LOAD:Multiplicacion"));
        btnDiv.addActionListener(e -> enviarComandoAdmin("LOAD:Division"));
        btnVaciar.addActionListener(e -> enviarComandoAdmin("UNLOAD"));

        panelBotones.add(btnTopo);
        panelBotones.add(btnVaciar);
        panelBotones.add(btnSuma);
        panelBotones.add(btnResta);
        panelBotones.add(btnMult);
        panelBotones.add(btnDiv);

        add(panelBotones, BorderLayout.SOUTH);
    }

    private void conectarAlNodo(String host, int puerto) {
        new Thread(() -> {
            try {
                Socket socket = new Socket(host, puerto);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                
                System.out.println("[+] Gestor conectado a la red.");
                solicitarTopologia(); 

                while (true) {
                    Mensaje msg = (Mensaje) in.readObject();
                    
                    
                    if (msg.tipoMensaje.equals("TOPO_RES")) {
                        agregarCelulAAlaTabla(msg.idRemitente, msg.codigoContenido);
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Desconectado de la red ADS.");
            }
        }).start();
    }

    private void solicitarTopologia() {
        modeloTabla.setRowCount(0); 
        try {
            
            Mensaje msgTopo = new Mensaje("TOPO", "REQUEST", 0.0, 0.0, idGestor);
            out.writeObject(msgTopo);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error solicitando topología.");
        }
    }

    private void enviarComandoAdmin(String comando) {
        int filaSeleccionada = tablaCelulas.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una célula de la tabla primero.");
            return;
        }

        String idDestino = (String) modeloTabla.getValueAt(filaSeleccionada, 0);

        try {
          
            Mensaje msgAdmin = new Mensaje("ADMIN", comando, 0, 0, idGestor);
            msgAdmin.folio = idDestino; 
            
            out.writeObject(msgAdmin);
            out.flush();
            
           
            Thread.sleep(500);
            solicitarTopologia();
            
        } catch (Exception e) {
            System.err.println("Error enviando comando.");
        }
    }

    private void agregarCelulAAlaTabla(String idCelula, String estado) {
        SwingUtilities.invokeLater(() -> {
            modeloTabla.addRow(new Object[]{idCelula, estado});
        });
    }

    public static void main(String[] args) {
        int puerto = 8001;
        String strPuerto = JOptionPane.showInputDialog(null, "Puerto del Nodo (Para el Gestor):", "8001");
        if (strPuerto != null && !strPuerto.trim().isEmpty()) {
            puerto = Integer.parseInt(strPuerto.trim());
        } else {
            System.exit(0);
        }
        
        int finalPuerto = puerto;
        SwingUtilities.invokeLater(() -> new GestorServicios("localhost", finalPuerto).setVisible(true));
    }
}