import ModuloComun.IMicroservicio;

public class Resta implements IMicroservicio {
    
    @Override
    public double calcular(double n1, double n2) {
        return n1 - n2;
    }

    @Override
    public String getCodigoRequerimiento() {
        return "SUB_REQ";
    }

    @Override
    public String getCodigoRespuesta() {
        return "SUB_RES";
    }
}