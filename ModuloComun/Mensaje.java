package ModuloComun;

import java.io.Serializable;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

public class Mensaje implements Serializable {
    private static final long serialVersionUID = 1L;

    
    public String idMensaje;
    public String folio;               
    public List<String> huella;       
    public String tipoMensaje;        
    
  
    public String codigoContenido; 
    public double num1;
    public double num2;
    public double resultado;
    public String idRemitente;


    public Mensaje(String tipoMensaje, String codigoContenido, double num1, double num2, String idRemitente) {
        this.idMensaje = "MSG-" + UUID.randomUUID().toString().substring(0, 6);
        this.folio = "FOLIO-" + UUID.randomUUID().toString().substring(0, 6);
        this.tipoMensaje = tipoMensaje;
        this.huella = new ArrayList<>();
        this.huella.add(idRemitente);
        
        this.codigoContenido = codigoContenido;
        this.num1 = num1;
        this.num2 = num2;
        this.resultado = 0.0;
        this.idRemitente = idRemitente;
    }

    
    public Mensaje(String tipoMensaje, String codigoContenido, String folio, double resultado, String idRemitente, List<String> huellaOriginal) {
        this.idMensaje = "MSG-" + UUID.randomUUID().toString().substring(0, 6);
        this.folio = folio;
        this.tipoMensaje = tipoMensaje;
        
      
        this.huella = new ArrayList<>(huellaOriginal);
        this.huella.add(idRemitente);
        
        this.codigoContenido = codigoContenido;
        this.num1 = 0.0;
        this.num2 = 0.0;
        this.resultado = resultado;
        this.idRemitente = idRemitente;
    }


    public void registrarHuella(String idEntidad) {
        if (!this.huella.contains(idEntidad)) {
            this.huella.add(idEntidad);
        }
    }

    @Override
    public String toString() {
        return "Mensaje[" + idMensaje + "] | Tipo: " + tipoMensaje + " | Folio: " + folio + 
               " | Codigo: " + codigoContenido + " | Remitente: " + idRemitente;
    }
}