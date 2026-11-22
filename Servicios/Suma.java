import ModuloComun.IMicroservicio;

public class Suma implements IMicroservicio {
    
    @Override
    public double calcular(double n1, double n2) {
        return n1 + n2;
    }

    @Override
    public String getCodigoRequerimiento() {
        return "SUM_REQ";
    }

    @Override
    public String getCodigoRespuesta() {
        return "SUM_RES";
    }
}