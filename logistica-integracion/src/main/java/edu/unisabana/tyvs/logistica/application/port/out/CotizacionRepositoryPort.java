package edu.unisabana.tyvs.logistica.application.port.out;

import edu.unisabana.tyvs.logistica.domain.model.Cotizacion;
import edu.unisabana.tyvs.logistica.domain.model.SolicitudEnvio;
import edu.unisabana.tyvs.logistica.infrastructure.persistence.CotizacionRecord;

import java.util.List;

/**
 * Puerto de salida para persistencia del historial de cotizaciones.
 */
public interface CotizacionRepositoryPort {

    void initSchema() throws Exception;

    void save(SolicitudEnvio solicitud, Cotizacion cotizacion) throws Exception;

    List<CotizacionRecord> findAll() throws Exception;

    void deleteAll() throws Exception;

    long countExitosas() throws Exception;
}
