package edu.unisabana.tyvs.logistica.infrastructure.persistence;

public class CotizacionRecord {
    private final long   id;
    private final String ciudadOrigen;
    private final String ciudadDestino;
    private final double pesoKg;
    private final String tipoEnvio;
    private final String estado;
    private final double valorTotal;

    public CotizacionRecord(long id, String ciudadOrigen, String ciudadDestino,
                            double pesoKg, String tipoEnvio, String estado, double valorTotal) {
        this.id = id; this.ciudadOrigen = ciudadOrigen; this.ciudadDestino = ciudadDestino;
        this.pesoKg = pesoKg; this.tipoEnvio = tipoEnvio;
        this.estado = estado; this.valorTotal = valorTotal;
    }

    public long   getId()            { return id;            }
    public String getCiudadOrigen()  { return ciudadOrigen;  }
    public String getCiudadDestino() { return ciudadDestino; }
    public double getPesoKg()        { return pesoKg;        }
    public String getTipoEnvio()     { return tipoEnvio;     }
    public String getEstado()        { return estado;        }
    public double getValorTotal()    { return valorTotal;    }
}
