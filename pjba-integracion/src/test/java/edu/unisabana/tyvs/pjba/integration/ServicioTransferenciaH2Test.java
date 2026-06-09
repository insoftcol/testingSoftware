package edu.unisabana.tyvs.pjba.integration;

import edu.unisabana.tyvs.pjba.application.port.out.TransferenciaRepositoryPort;
import edu.unisabana.tyvs.pjba.application.usecase.ServicioTransferencia;
import edu.unisabana.tyvs.pjba.domain.model.*;
import edu.unisabana.tyvs.pjba.infrastructure.persistence.TransferenciaRecord;
import edu.unisabana.tyvs.pjba.infrastructure.persistence.TransferenciaRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Pruebas de INTEGRACIÓN — Servicio + Repositorio H2.
 *
 * <p>Verifica la colaboración real entre {@link ServicioTransferencia}
 * y {@link TransferenciaRepository} usando H2 en memoria.</p>
 *
 * <p>No usa Spring: instancia directamente los objetos (máxima velocidad).</p>
 *
 * <h3>Patrón AAA aplicado:</h3>
 * <ul>
 *   <li><b>Arrange</b>: preparar H2, esquema, datos y servicio.</li>
 *   <li><b>Act</b>: ejecutar {@code procesarTransferencia}.</li>
 *   <li><b>Assert</b>: verificar tanto el resultado como el estado en BD.</li>
 * </ul>
 */
public class ServicioTransferenciaH2Test {

    private TransferenciaRepositoryPort repo;
    private ServicioTransferencia       servicio;

    @Before
    public void setUp() throws Exception {
        // Arrange compartido: H2 limpia antes de cada test
        String jdbc = "jdbc:h2:mem:pjba_test;DB_CLOSE_DELAY=-1";
        repo = new TransferenciaRepository(jdbc);
        repo.initSchema();
        repo.deleteAll();
        servicio = new ServicioTransferencia(repo);
    }

    // =========================================================================
    // TEST 1 — Transferencia exitosa: se persiste en BD
    // BDD: Given cuenta AV Villas con saldo; When monto válido;
    //      Then EXITOSA y registro guardado
    // =========================================================================

    @Test
    public void shouldPersistirTransferenciaExitosaEnBaseDeDatos() throws Exception {
        // Arrange
        Cuenta origen = new Cuenta("1234567890", TipoCuenta.AHORROS, "AV VILLAS", 500_000.0);
        SolicitudTransferencia solicitud = new SolicitudTransferencia(origen, "9876543210", 200_000.0);

        // Act
        TransferenciaResult resultado = servicio.procesarTransferencia(solicitud);

        // Assert — resultado de dominio
        assertEquals(TransferenciaResult.EXITOSA, resultado);

        // Assert — verificar persistencia en H2
        List<TransferenciaRecord> registros = repo.findAll();
        assertEquals(1, registros.size());
        assertEquals("EXITOSA", registros.get(0).getResultado());
        assertEquals("1234567890", registros.get(0).getCuentaOrigen());
        assertEquals("9876543210", registros.get(0).getCuentaDestino());
        assertEquals(200_000.0, registros.get(0).getMonto(), 0.01);
    }

    // =========================================================================
    // TEST 2 — Saldo insuficiente: se persiste el intento fallido
    // BDD: Given saldo=100K monto=500K; When procesarTransferencia;
    //      Then SALDO_INSUFICIENTE y registro guardado con ese estado
    // =========================================================================

    @Test
    public void shouldPersistirIntentoFallidoPorSaldoInsuficiente() throws Exception {
        // Arrange
        Cuenta origen = new Cuenta("1234567890", TipoCuenta.AHORROS, "AV VILLAS", 100_000.0);
        SolicitudTransferencia solicitud = new SolicitudTransferencia(origen, "9876543210", 500_001.0);

        // Act
        TransferenciaResult resultado = servicio.procesarTransferencia(solicitud);

        // Assert — resultado de dominio
        assertEquals(TransferenciaResult.SALDO_INSUFICIENTE, resultado);

        // Assert — el intento fallido también se persiste
        List<TransferenciaRecord> registros = repo.findAll();
        assertEquals(1, registros.size());
        assertEquals("SALDO_INSUFICIENTE", registros.get(0).getResultado());
    }

    // =========================================================================
    // TEST 3 — Múltiples transferencias: contador de exitosas correcto
    // BDD: Given 2 exitosas + 1 fallida; When countExitosas; Then 2
    // =========================================================================

    @Test
    public void shouldContarCorrectamenteLasTransferenciasExitosas() throws Exception {
        // Arrange
        Cuenta origen1 = new Cuenta("1111111111", TipoCuenta.AHORROS, "AV VILLAS", 500_000.0);
        Cuenta origen2 = new Cuenta("2222222222", TipoCuenta.CORRIENTE, "BANCOLOMBIA", 500_000.0);
        Cuenta origen3 = new Cuenta("3333333333", TipoCuenta.AHORROS, "AV VILLAS", 50_000.0);

        // Act — dos exitosas
        servicio.procesarTransferencia(new SolicitudTransferencia(origen1, "9999999999", 100_000.0));
        servicio.procesarTransferencia(new SolicitudTransferencia(origen2, "8888888888", 200_000.0));
        // una fallida (saldo insuficiente)
        servicio.procesarTransferencia(new SolicitudTransferencia(origen3, "7777777777", 500_000.0));

        // Assert
        assertEquals(3, repo.findAll().size());
        assertEquals(2, repo.countExitosas());
    }
}
