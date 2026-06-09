package edu.unisabana.tyvs.pjba.delivery.rest;

/** DTO de entrada para el endpoint REST POST /transferencia. */
public class TransferenciaDTO {
    private String numeroCuentaOrigen;
    private String tipoCuenta;
    private String entidadBancaria;
    private double saldoOrigen;
    private String numeroCuentaDestino;
    private double monto;

    public TransferenciaDTO() {}

    public String getNumeroCuentaOrigen()  { return numeroCuentaOrigen;  }
    public String getTipoCuenta()          { return tipoCuenta;          }
    public String getEntidadBancaria()     { return entidadBancaria;     }
    public double getSaldoOrigen()         { return saldoOrigen;         }
    public String getNumeroCuentaDestino() { return numeroCuentaDestino; }
    public double getMonto()               { return monto;               }

    public void setNumeroCuentaOrigen(String v)  { this.numeroCuentaOrigen  = v; }
    public void setTipoCuenta(String v)          { this.tipoCuenta          = v; }
    public void setEntidadBancaria(String v)     { this.entidadBancaria     = v; }
    public void setSaldoOrigen(double v)         { this.saldoOrigen         = v; }
    public void setNumeroCuentaDestino(String v) { this.numeroCuentaDestino = v; }
    public void setMonto(double v)               { this.monto               = v; }
}
