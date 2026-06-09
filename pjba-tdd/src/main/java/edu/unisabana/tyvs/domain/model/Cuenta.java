package edu.unisabana.tyvs.domain.model;

/**
 * Representa una cuenta bancaria en el dominio del sistema PJBA.
 */
public class Cuenta {

    private final String numeroCuenta;
    private final TipoCuenta tipoCuenta;
    private final String entidadBancaria;
    private double saldo;

    public Cuenta(String numeroCuenta, TipoCuenta tipoCuenta,
                  String entidadBancaria, double saldo) {
        this.numeroCuenta     = numeroCuenta;
        this.tipoCuenta       = tipoCuenta;
        this.entidadBancaria  = entidadBancaria;
        this.saldo            = saldo;
    }

    public String getNumeroCuenta()    { return numeroCuenta; }
    public TipoCuenta getTipoCuenta()  { return tipoCuenta; }
    public String getEntidadBancaria() { return entidadBancaria; }
    public double getSaldo()           { return saldo; }
    public void setSaldo(double saldo) { this.saldo = saldo; }
}
