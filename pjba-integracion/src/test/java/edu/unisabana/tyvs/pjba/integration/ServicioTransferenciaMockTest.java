package edu.unisabana.tyvs.pjba.integration;

import edu.unisabana.tyvs.pjba.application.port.out.TransferenciaRepositoryPort;
import edu.unisabana.tyvs.pjba.application.usecase.ServicioTransferencia;
import edu.unisabana.tyvs.pjba.domain.model.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas de INTEGRACIÓN con MOCK — Servicio + Repositorio simulado.
 *
 * <p>Usa Mockito para simular {@link TransferenciaRepositoryPort},
 * aislando el caso de uso de la base de datos real.</p>
 *
 * <p>Útil para: verificar que el servicio llama al repositorio correctamente
 * sin necesitar infraestructura real.</p>
 *
 * <h3>BDD applicado a cada test</h3>
 */
public class ServicioTransferenciaMockTest {

    private TransferenciaRepositoryPort repoMock;
    private ServicioTransferencia       servicio;

    @Before
    public void setUp() {
        // Arrange: crear mock y servicio con dependencia simulada
        repoMock = mock(TransferenciaRepositoryPort.class);
        servicio = new ServicioTransferencia(repoMock);
    }

    // =========================================================================
    // TEST 1 — Transferencia exitosa: el servicio llama a repo.save()
    //
    // BDD: Given transferencia válida;
    //      When procesarTransferencia;
    //      Then repo.save() es invocado exactamente una vez
    // =========================================================================

    @Test
    public void shouldLlamarSaveWhenTransferenciaEsExitosa() throws Exception {
        // Arrange
        Cuenta origen = new Cuenta("1234567890", TipoCuenta.AHORROS, "AV VILLAS", 500_000.0);
        SolicitudTransferencia solicitud = new SolicitudTransferencia(origen, "9876543210", 200_000.0);

        // Act
        TransferenciaResult resultado = servicio.procesarTransferencia(solicitud);

        // Assert — dominio
        assertEquals(TransferenciaResult.EXITOSA, resultado);

        // Assert — verificar interacción con el mock
        verify(repoMock, times(1))
            .save(eq(solicitud), eq(TransferenciaResult.EXITOSA));
    }

    // =========================================================================
    // TEST 2 — Datos inválidos: el servicio llama a repo.save() con DATOS_INVALIDOS
    //
    // BDD: Given solicitud=null;
    //      When procesarTransferencia;
    //      Then repo.save() se llama con DATOS_INVALIDOS (el intento se loguea)
    // =========================================================================

    @Test
    public void shouldLlamarSaveConDatosInvalidosWhenSolicitudEsNula() throws Exception {
        // Arrange
        SolicitudTransferencia solicitud = null;

        // Act
        TransferenciaResult resultado = servicio.procesarTransferencia(solicitud);

        // Assert — dominio
        assertEquals(TransferenciaResult.DATOS_INVALIDOS, resultado);

        // Assert — el intento nulo también se registra
        verify(repoMock, times(1))
            .save(isNull(), eq(TransferenciaResult.DATOS_INVALIDOS));
    }

    // =========================================================================
    // TEST 3 — Saldo insuficiente: save se llama pero con SALDO_INSUFICIENTE
    //
    // BDD: Given saldo insuficiente;
    //      When procesarTransferencia;
    //      Then save invocado, never con EXITOSA
    // =========================================================================

    @Test
    public void shouldLlamarSaveConSaldoInsuficienteYNuncaConExitosa() throws Exception {
        // Arrange
        Cuenta origen = new Cuenta("1234567890", TipoCuenta.AHORROS, "AV VILLAS", 100_000.0);
        SolicitudTransferencia solicitud = new SolicitudTransferencia(origen, "9876543210", 500_001.0);

        // Act
        TransferenciaResult resultado = servicio.procesarTransferencia(solicitud);

        // Assert — dominio
        assertEquals(TransferenciaResult.SALDO_INSUFICIENTE, resultado);

        // Assert — save se llama con el resultado correcto
        verify(repoMock, times(1))
            .save(eq(solicitud), eq(TransferenciaResult.SALDO_INSUFICIENTE));

        // Assert — nunca se llama con EXITOSA
        verify(repoMock, never())
            .save(any(), eq(TransferenciaResult.EXITOSA));
    }
}
