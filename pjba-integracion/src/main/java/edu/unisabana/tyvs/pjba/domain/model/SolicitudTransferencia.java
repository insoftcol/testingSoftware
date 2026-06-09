package edu.unisabana.tyvs.pjba.domain.model;

public class SolicitudTransferencia {
    private final Cuenta cuentaOrigen;
    private final String numeroCuentaDestino;
    private final double monto;

    public SolicitudTransferencia(Cuenta cuentaOrigen,
                                  String numeroCuentaDestino,
                                  double monto) {
        this.cuentaOrigen        = cuentaOrigen;
        this.numeroCuentaDestino = numeroCuentaDestino;
        this.monto               = monto;
    }

    public Cuenta getCuentaOrigen()        { return cuentaOrigen;        }
    public String getNumeroCuentaDestino() { return numeroCuentaDestino; }
    public double getMonto()               { return monto;               }
}
