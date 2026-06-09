package edu.unisabana.tyvs.logistica.integration;

import edu.unisabana.tyvs.logistica.application.port.out.CotizacionRepositoryPort;
import edu.unisabana.tyvs.logistica.application.usecase.ServicioTarifa;
import edu.unisabana.tyvs.logistica.domain.model.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas de INTEGRACIÓN con MOCK — ServicioTarifa + repositorio simulado.
 */
public class ServicioTarifaMockTest {

    private CotizacionRepositoryPort repoMock;
    private ServicioTarifa           servicio;

    @Before
    public void setUp() {
        repoMock = mock(CotizacionRepositoryPort.class);
        servicio = new ServicioTarifa(repoMock);
    }

    // =========================================================================
    // TEST 1 — Cotización exitosa: repo.save() invocado
    // BDD: Given Bogotá→Medellín válido; When cotizar;
    //      Then EXITOSA y save() llamado exactamente 1 vez
    // =========================================================================

    @Test
    public void shouldLlamarSaveWhenCotizacionEsExitosa() throws Exception {
        // Arrange
        SolicitudEnvio sol = new SolicitudEnvio("BOGOTA","MEDELLIN",10.0, TipoEnvio.NORMAL);

        // Act
        Cotizacion cotizacion = servicio.cotizar(sol);

        // Assert — dominio
        assertEquals(CotizacionEstado.EXITOSA, cotizacion.getEstado());

        // Assert — interacción con mock
        verify(repoMock, times(1)).save(eq(sol), argThat(c -> c.isExitosa()));
    }

    // =========================================================================
    // TEST 2 — Misma ciudad: save llamado con estado MISMA_CIUDAD
    // =========================================================================

    @Test
    public void shouldLlamarSaveConMismaCiudadWhenOrigenIgualDestino() throws Exception {
        // Arrange
        SolicitudEnvio sol = new SolicitudEnvio("CALI","CALI",10.0, TipoEnvio.NORMAL);

        // Act
        Cotizacion cotizacion = servicio.cotizar(sol);

        // Assert — dominio
        assertEquals(CotizacionEstado.MISMA_CIUDAD, cotizacion.getEstado());

        // Assert — mock
        verify(repoMock, times(1))
            .save(eq(sol), argThat(c -> c.getEstado() == CotizacionEstado.MISMA_CIUDAD));
        verify(repoMock, never())
            .save(any(), argThat(c -> c.isExitosa()));
    }
}
