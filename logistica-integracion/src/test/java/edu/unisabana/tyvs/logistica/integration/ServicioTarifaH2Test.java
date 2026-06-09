package edu.unisabana.tyvs.logistica.integration;

import edu.unisabana.tyvs.logistica.application.port.out.CotizacionRepositoryPort;
import edu.unisabana.tyvs.logistica.application.usecase.ServicioTarifa;
import edu.unisabana.tyvs.logistica.domain.model.*;
import edu.unisabana.tyvs.logistica.infrastructure.persistence.CotizacionRecord;
import edu.unisabana.tyvs.logistica.infrastructure.persistence.CotizacionRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Pruebas de INTEGRACIÓN — ServicioTarifa + CotizacionRepository H2.
 *
 * <p>Verifica la interacción real entre el caso de uso y la capa
 * de persistencia usando H2 en memoria, sin Spring.</p>
 */
public class ServicioTarifaH2Test {

    private CotizacionRepositoryPort repo;
    private ServicioTarifa           servicio;

    @Before
    public void setUp() throws Exception {
        String jdbc = "jdbc:h2:mem:logistica_test;DB_CLOSE_DELAY=-1";
        repo = new CotizacionRepository(jdbc);
        repo.initSchema();
        repo.deleteAll();
        servicio = new ServicioTarifa(repo);
    }

    // =========================================================================
    // TEST 1 — Cotización exitosa persiste en BD con valor correcto
    // BDD: Given Bogotá→Medellín 10kg NORMAL;
    //      When cotizar; Then EXITOSA persiste con valor $38.000
    // =========================================================================

    @Test
    public void shouldPersistirCotizacionExitosaConValorCorrecto() throws Exception {
        // Arrange
        SolicitudEnvio sol = new SolicitudEnvio("BOGOTA","MEDELLIN",10.0, TipoEnvio.NORMAL);

        // Act
        Cotizacion cotizacion = servicio.cotizar(sol);

        // Assert — dominio
        assertEquals(CotizacionEstado.EXITOSA, cotizacion.getEstado());
        assertEquals(38_000.0, cotizacion.getValorTotal(), 0.01);

        // Assert — persistencia
        List<CotizacionRecord> registros = repo.findAll();
        assertEquals(1, registros.size());
        assertEquals("EXITOSA", registros.get(0).getEstado());
        assertEquals("BOGOTA",   registros.get(0).getCiudadOrigen());
        assertEquals("MEDELLIN", registros.get(0).getCiudadDestino());
        assertEquals(38_000.0,   registros.get(0).getValorTotal(), 0.01);
    }

    // =========================================================================
    // TEST 2 — Peso inválido: se persiste con estado PESO_INVALIDO
    // BDD: Given pesoKg=0; When cotizar; Then PESO_INVALIDO guardado en BD
    // =========================================================================

    @Test
    public void shouldPersistirCotizacionInvalidaPorPesoInvalido() throws Exception {
        // Arrange
        SolicitudEnvio sol = new SolicitudEnvio("BOGOTA","MEDELLIN",0.0, TipoEnvio.NORMAL);

        // Act
        Cotizacion cotizacion = servicio.cotizar(sol);

        // Assert — dominio
        assertEquals(CotizacionEstado.PESO_INVALIDO, cotizacion.getEstado());
        assertEquals(0.0, cotizacion.getValorTotal(), 0.01);

        // Assert — persistencia
        List<CotizacionRecord> registros = repo.findAll();
        assertEquals(1, registros.size());
        assertEquals("PESO_INVALIDO", registros.get(0).getEstado());
    }

    // =========================================================================
    // TEST 3 — Múltiples cotizaciones: contador de exitosas
    // =========================================================================

    @Test
    public void shouldContarCotizacionesExitosasCorrectamente() throws Exception {
        // Arrange + Act
        servicio.cotizar(new SolicitudEnvio("BOGOTA","MEDELLIN",10.0, TipoEnvio.NORMAL));    // exitosa
        servicio.cotizar(new SolicitudEnvio("MANIZALES","PEREIRA",5.0, TipoEnvio.EXPRESO));   // exitosa
        servicio.cotizar(new SolicitudEnvio("BOGOTA","BOGOTA",10.0, TipoEnvio.NORMAL));       // misma ciudad

        // Assert
        assertEquals(3, repo.findAll().size());
        assertEquals(2, repo.countExitosas());
    }
}
