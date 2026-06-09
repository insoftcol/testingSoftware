package edu.unisabana.tyvs.logistica.domain.model;

/**
 * Resultado de una solicitud de cotización de envío.
 *
 * <p>Usa métodos de fábrica estáticos para que las pruebas sean
 * expresivas y no dependan del constructor directamente:</p>
 *
 * <pre>
 *   Cotizacion.exitosa(38_000.0)
 *   Cotizacion.invalida(CotizacionEstado.PESO_INVALIDO)
 * </pre>
 */
public class Cotizacion {

    private final CotizacionEstado estado;
    private final double           valorTotal;

    private Cotizacion(CotizacionEstado estado, double valorTotal) {
        this.estado     = estado;
        this.valorTotal = valorTotal;
    }

    // ── Fábricas ────────────────────────────────────────────────────────────

    /** Crea una cotización exitosa con el valor calculado. */
    public static Cotizacion exitosa(double valorTotal) {
        return new Cotizacion(CotizacionEstado.EXITOSA, valorTotal);
    }

    /** Crea una cotización fallida con su estado y valor cero. */
    public static Cotizacion invalida(CotizacionEstado estado) {
        return new Cotizacion(estado, 0.0);
    }

    // ── Accesores ────────────────────────────────────────────────────────────

    public CotizacionEstado getEstado()     { return estado;     }
    public double           getValorTotal() { return valorTotal; }

    public boolean isExitosa() {
        return CotizacionEstado.EXITOSA.equals(estado);
    }
}
