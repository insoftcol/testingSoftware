package edu.unisabana.tyvs.domain.service;

import edu.unisabana.tyvs.domain.model.SolicitudTransferencia;
import edu.unisabana.tyvs.domain.model.TransferenciaResult;

/**
 * Servicio de dominio: procesa solicitudes de transferencia bancaria.
 *
 * <p>Implementa las reglas de negocio del sistema PJBA / INSOFTCOL
 * en Java puro, permitiendo pruebas unitarias aisladas (arquitectura limpia).</p>
 *
 * <h3>Reglas de negocio</h3>
 * <ol>
 *   <li>La solicitud no puede ser {@code null}.</li>
 *   <li>La cuenta origen no puede ser {@code null} ni tener número vacío.</li>
 *   <li>El número de cuenta destino no puede ser {@code null} ni vacío.</li>
 *   <li>El monto debe ser mayor que {@value #MONTO_MINIMO} y menor o igual a {@value #MONTO_MAXIMO}.</li>
 *   <li>La cuenta origen debe tener saldo suficiente para cubrir el monto.</li>
 *   <li>La entidad bancaria destino debe estar en la lista de entidades soportadas.</li>
 * </ol>
 */
public class ServicioTransferencia {

    /** Monto mínimo permitido por transferencia (exclusivo). */
    public static final double MONTO_MINIMO  = 0.0;

    /** Monto máximo permitido por transferencia diaria. */
    public static final double MONTO_MAXIMO  = 10_000_000.0;

    /** Entidades bancarias soportadas en el sistema PJBA. */
    private static final java.util.Set<String> ENTIDADES_SOPORTADAS =
        new java.util.HashSet<>(java.util.Arrays.asList(
            "AV VILLAS", "BANCOLOMBIA", "DAVIVIENDA",
            "BOGOTA", "OCCIDENTE", "NEQUI"
        ));

    /**
     * Procesa una solicitud de transferencia aplicando todas las reglas
     * de negocio del dominio bancario PJBA.
     *
     * @param solicitud datos de la transferencia a procesar
     * @return {@link TransferenciaResult} con el resultado de la operación
     */
    public TransferenciaResult procesarTransferencia(SolicitudTransferencia solicitud) {
        // CICLO 1 GREEN — solicitud nula
        if (solicitud == null) {
            return TransferenciaResult.DATOS_INVALIDOS;
        }
        // CICLO 2 GREEN — cuenta origen válida
        if (solicitud.getCuentaOrigen() == null) {
            return TransferenciaResult.CUENTA_ORIGEN_INVALIDA;
        }
        if (solicitud.getCuentaOrigen().getNumeroCuenta() == null
                || solicitud.getCuentaOrigen().getNumeroCuenta().isEmpty()) {
            return TransferenciaResult.CUENTA_ORIGEN_INVALIDA;
        }
        // CICLO 3 GREEN — cuenta destino válida
        if (solicitud.getNumeroCuentaDestino() == null
                || solicitud.getNumeroCuentaDestino().isEmpty()) {
            return TransferenciaResult.CUENTA_DESTINO_INVALIDA;
        }
        // CICLO 4 GREEN — monto válido (0 < monto <= MONTO_MAXIMO)
        if (solicitud.getMonto() <= MONTO_MINIMO || solicitud.getMonto() > MONTO_MAXIMO) {
            return TransferenciaResult.MONTO_INVALIDO;
        }
        // CICLO 5 GREEN — saldo suficiente
        if (solicitud.getCuentaOrigen().getSaldo() < solicitud.getMonto()) {
            return TransferenciaResult.SALDO_INSUFICIENTE;
        }
        // CICLO 6 GREEN — entidad bancaria soportada
        if (!ENTIDADES_SOPORTADAS.contains(
                solicitud.getCuentaOrigen().getEntidadBancaria().toUpperCase())) {
            return TransferenciaResult.ENTIDAD_NO_SOPORTADA;
        }
        return TransferenciaResult.EXITOSA;
    }
}
