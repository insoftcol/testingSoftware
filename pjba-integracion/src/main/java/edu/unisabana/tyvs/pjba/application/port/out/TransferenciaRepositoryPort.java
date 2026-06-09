package edu.unisabana.tyvs.pjba.application.port.out;

import edu.unisabana.tyvs.pjba.domain.model.SolicitudTransferencia;
import edu.unisabana.tyvs.pjba.domain.model.TransferenciaResult;
import edu.unisabana.tyvs.pjba.infrastructure.persistence.TransferenciaRecord;

import java.util.List;

/**
 * Puerto de salida para persistencia de transferencias (audit log).
 * Permite registrar cada intento de transferencia con su resultado.
 */
public interface TransferenciaRepositoryPort {

    /** Crea el esquema de tabla (útil en pruebas con H2). */
    void initSchema() throws Exception;

    /** Registra el resultado de una transferencia. */
    void save(SolicitudTransferencia solicitud, TransferenciaResult resultado) throws Exception;

    /** Retorna todos los registros del audit log. */
    List<TransferenciaRecord> findAll() throws Exception;

    /** Borra todos los registros (limpiar entre pruebas). */
    void deleteAll() throws Exception;

    /** Cuenta el número de transferencias exitosas. */
    long countExitosas() throws Exception;
}
