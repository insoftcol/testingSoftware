package edu.unisabana.tyvs.pjba.application.usecase;

import edu.unisabana.tyvs.pjba.application.port.out.TransferenciaRepositoryPort;
import edu.unisabana.tyvs.pjba.domain.model.SolicitudTransferencia;
import edu.unisabana.tyvs.pjba.domain.model.TransferenciaResult;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Caso de uso: valida y procesa una transferencia bancaria.
 *
 * <p>Aplica todas las reglas de dominio y persiste el resultado
 * a través del puerto de salida {@link TransferenciaRepositoryPort}.</p>
 */
public class ServicioTransferencia {

    public static final double MONTO_MINIMO  =       0.0;
    public static final double MONTO_MAXIMO  = 10_000_000.0;

    private static final Set<String> ENTIDADES_SOPORTADAS = new HashSet<>(Arrays.asList(
        "AV VILLAS", "BANCOLOMBIA", "DAVIVIENDA",
        "BOGOTA", "OCCIDENTE", "NEQUI"
    ));

    private final TransferenciaRepositoryPort repo;

    /** Constructor con inyección de dependencia (integración y producción). */
    public ServicioTransferencia(TransferenciaRepositoryPort repo) {
        this.repo = repo;
    }

    /** Constructor sin repositorio (compatibilidad con pruebas unitarias puras). */
    public ServicioTransferencia() {
        this.repo = null;
    }

    public TransferenciaResult procesarTransferencia(SolicitudTransferencia solicitud) {
        TransferenciaResult resultado = validarYProcesar(solicitud);

        // Persistir audit log si hay repositorio disponible
        if (repo != null) {
            try {
                repo.save(solicitud, resultado);
            } catch (Exception e) {
                throw new IllegalStateException("Error al persistir transferencia: " + e.getMessage(), e);
            }
        }

        return resultado;
    }

    private TransferenciaResult validarYProcesar(SolicitudTransferencia solicitud) {
        if (solicitud == null)
            return TransferenciaResult.DATOS_INVALIDOS;

        if (solicitud.getCuentaOrigen() == null)
            return TransferenciaResult.CUENTA_ORIGEN_INVALIDA;

        if (solicitud.getCuentaOrigen().getNumeroCuenta() == null
                || solicitud.getCuentaOrigen().getNumeroCuenta().isEmpty())
            return TransferenciaResult.CUENTA_ORIGEN_INVALIDA;

        if (solicitud.getNumeroCuentaDestino() == null
                || solicitud.getNumeroCuentaDestino().isEmpty())
            return TransferenciaResult.CUENTA_DESTINO_INVALIDA;

        if (solicitud.getMonto() <= MONTO_MINIMO || solicitud.getMonto() > MONTO_MAXIMO)
            return TransferenciaResult.MONTO_INVALIDO;

        if (solicitud.getCuentaOrigen().getSaldo() < solicitud.getMonto())
            return TransferenciaResult.SALDO_INSUFICIENTE;

        if (!ENTIDADES_SOPORTADAS.contains(
                solicitud.getCuentaOrigen().getEntidadBancaria().toUpperCase()))
            return TransferenciaResult.ENTIDAD_NO_SOPORTADA;

        return TransferenciaResult.EXITOSA;
    }
}
