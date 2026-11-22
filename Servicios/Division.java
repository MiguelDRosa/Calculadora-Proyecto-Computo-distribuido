import ModuloComun.IMicroservicio;

public class Division implements IMicroservicio {
    
    @Override
    public double calcular(double n1, double n2) {
        if (n2 == 0) {
            System.out.println("    -> Error: División entre cero evitada.");
            return Double.NaN; 
        }
        return n1 / n2;
    }

    @Override
    public String getCodigoRequerimiento() {
        return "DIV_REQ";
    }

    @Override
    public String getCodigoRespuesta() {
        return "DIV_RES";
    }
}