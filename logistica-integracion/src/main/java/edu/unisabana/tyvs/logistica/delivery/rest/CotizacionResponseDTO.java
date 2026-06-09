package edu.unisabana.tyvs.logistica.delivery.rest;
public class CotizacionResponseDTO {
    private String estado; private double valorTotal;
    public CotizacionResponseDTO() {}
    public CotizacionResponseDTO(String estado, double valorTotal) {
        this.estado = estado; this.valorTotal = valorTotal;
    }
    public String getEstado()     { return estado;      }
    public double getValorTotal() { return valorTotal;  }
}
