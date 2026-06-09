package edu.unisabana.tyvs.logistica.domain.model;

/**
 * Representa una solicitud de cotización de envío entre dos ciudades.
 *
 * <p>Es un objeto de valor inmutable del dominio logístico. No conoce
 * bases de datos, HTTP ni ningún framework externo.</p>
 *
 * @param ciudadOrigen  nombre de la ciudad de origen (ej: "BOGOTA")
 * @param ciudadDestino nombre de la ciudad de destino (ej: "MEDELLIN")
 * @param pesoKg        peso del paquete en kilogramos
 * @param tipoEnvio     modalidad de envío (NORMAL, EXPRESO, REFRIGERADO)
 */
public class SolicitudEnvio {

    private final String   ciudadOrigen;
    private final String   ciudadDestino;
    private final double   pesoKg;
    private final TipoEnvio tipoEnvio;

    public SolicitudEnvio(String ciudadOrigen, String ciudadDestino,
                          double pesoKg, TipoEnvio tipoEnvio) {
        this.ciudadOrigen  = ciudadOrigen;
        this.ciudadDestino = ciudadDestino;
        this.pesoKg        = pesoKg;
        this.tipoEnvio     = tipoEnvio;
    }

    public String    getCiudadOrigen()  { return ciudadOrigen;  }
    public String    getCiudadDestino() { return ciudadDestino; }
    public double    getPesoKg()        { return pesoKg;        }
    public TipoEnvio getTipoEnvio()     { return tipoEnvio;     }
}
