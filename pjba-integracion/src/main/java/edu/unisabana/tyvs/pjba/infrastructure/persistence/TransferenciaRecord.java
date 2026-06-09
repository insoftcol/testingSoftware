package edu.unisabana.tyvs.pjba.infrastructure.persistence;

/** Registro de audit log de una transferencia bancaria. */
public class TransferenciaRecord {
    private final long   id;
    private final String cuentaOrigen;
    private final String cuentaDestino;
    private final double monto;
    private final String entidad;
    private final String resultado;

    public TransferenciaRecord(long id, String cuentaOrigen, String cuentaDestino,
                               double monto, String entidad, String resultado) {
        this.id            = id;
        this.cuentaOrigen  = cuentaOrigen;
        this.cuentaDestino = cuentaDestino;
        this.monto         = monto;
        this.entidad       = entidad;
        this.resultado     = resultado;
    }

    public long   getId()            { return id;            }
    public String getCuentaOrigen()  { return cuentaOrigen;  }
    public String getCuentaDestino() { return cuentaDestino; }
    public double getMonto()         { return monto;         }
    public String getEntidad()       { return entidad;       }
    public String getResultado()     { return resultado;     }
}
