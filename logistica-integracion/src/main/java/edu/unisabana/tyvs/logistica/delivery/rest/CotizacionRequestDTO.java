package edu.unisabana.tyvs.logistica.delivery.rest;
public class CotizacionRequestDTO {
    private String ciudadOrigen; private String ciudadDestino;
    private double pesoKg; private String tipoEnvio;
    public CotizacionRequestDTO() {}
    public String getCiudadOrigen()  { return ciudadOrigen;  }
    public String getCiudadDestino() { return ciudadDestino; }
    public double getPesoKg()        { return pesoKg;        }
    public String getTipoEnvio()     { return tipoEnvio;     }
    public void setCiudadOrigen(String v)  { this.ciudadOrigen  = v; }
    public void setCiudadDestino(String v) { this.ciudadDestino = v; }
    public void setPesoKg(double v)        { this.pesoKg        = v; }
    public void setTipoEnvio(String v)     { this.tipoEnvio     = v; }
}
