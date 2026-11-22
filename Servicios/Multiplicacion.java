import ModuloComun.IMicroservicio;

public class Multiplicacion implements IMicroservicio {
    
    @Override
    public double calcular(double n1, double n2) {
        return n1 * n2;
    }

    @Override
    public String getCodigoRequerimiento() {
        return "MUL_REQ";
    }

    @Override
    public String getCodigoRespuesta() {
        return "MUL_RES";
    }
}