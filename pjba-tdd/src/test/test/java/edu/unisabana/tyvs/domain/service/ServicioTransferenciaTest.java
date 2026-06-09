package edu.unisabana.tyvs.domain.service;

import edu.unisabana.tyvs.domain.model.Cuenta;
import edu.unisabana.tyvs.domain.model.SolicitudTransferencia;
import edu.unisabana.tyvs.domain.model.TipoCuenta;
import edu.unisabana.tyvs.domain.model.TransferenciaResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Pruebas unitarias del dominio de transferencias — PJBA / AV Villas.
 *
 * <p>Aplica TDD (Red → Green → Refactor), patrón AAA y escenarios BDD
 * cubriendo todas las clases de equivalencia y valores límite definidos
 * para {@link ServicioTransferencia#procesarTransferencia}.</p>
 *
 * <h3>Clases de equivalencia</h3>
 * <pre>
 * Monto     : inválido (≤ 0), válido (0 < m ≤ 10.000.000), excede límite (> 10.000.000)
 * Saldo     : insuficiente (saldo < monto), suficiente (saldo ≥ monto)
 * Solicitud : nula, datos_origen_nulos, destino_vacio, entidad_no_soportada, completa
 * </pre>
 *
 * Integrantes del equipo:
 * - Fredy Orlando Pulido Quintero
 * - Nicolás Torres
 * - Myriam Martínez
 * - Juan Francisco Pérez
 */
public class ServicioTransferenciaTest {

    // -------------------------------------------------------------------------
    // Objetos reutilizables (inicializados en @Before para bajo acoplamiento)
    // -------------------------------------------------------------------------
    private ServicioTransferencia servicio;
    private Cuenta cuentaOrigenValida;

    @Before
    public void setUp() {
        // Arrange compartido: servicio limpio antes de cada prueba
        servicio = new ServicioTransferencia();
        cuentaOrigenValida = new Cuenta(
            "1234567890",          // número de cuenta
            TipoCuenta.AHORROS,    // tipo de cuenta
            "AV VILLAS",           // entidad bancaria origen
            500_000.0              // saldo disponible
        );
    }

    // =========================================================================
    // CICLO TDD 1 — Camino feliz: transferencia válida
    // BDD: Given cuenta con saldo; When monto válido; Then EXITOSA
    // =========================================================================

    /**
     * RED → GREEN: primera prueba, verifica el camino feliz.
     *
     * Given: cuenta de ahorros AV Villas con saldo $500.000
     * When:  se solicita transferir $200.000 a una cuenta Bancolombia válida
     * Then:  el resultado debe ser EXITOSA
     */
    @Test
    public void shouldReturnExitosaWhenTransferenciaEsValida() {
        // Arrange
        SolicitudTransferencia solicitud = new SolicitudTransferencia(
            cuentaOrigenValida,
            "9876543210",   // cuenta destino válida
            200_000.0       // monto dentro del rango permitido
        );

        // Act
        TransferenciaResult resultado = servicio.procesarTransferencia(solicitud);

        // Assert
        Assert.assertEquals(TransferenciaResult.EXITOSA, resultado);
    }

    // =========================================================================
    // CICLO TDD 2 — Solicitud nula
    // BDD: Given solicitud null; When se procesa; Then DATOS_INVALIDOS
    // =========================================================================

    /**
     * RED: solicitud completamente nula → debe retornar DATOS_INVALIDOS.
     *
     * Given: no hay datos de transferencia (null)
     * When:  se intenta procesar la transferencia
     * Then:  el resultado debe ser DATOS_INVALIDOS
     */
    @Test
    public void shouldReturnDatosInvalidosWhenSolicitudEsNula() {
        // Arrange
        SolicitudTransferencia solicitud = null;

        // Act
        TransferenciaResult resultado = servicio.procesarTransferencia(solicitud);

        // Assert
        Assert.assertEquals(TransferenciaResult.DATOS_INVALIDOS, resultado);
    }

    // =========================================================================
    // CICLO TDD 3 — Cuenta origen inválida
    // =========================================================================

    /**
     * RED: cuenta origen nula → CUENTA_ORIGEN_INVALIDA.
     *
     * Given: la cuenta origen es null
     * When:  se solicita la transferencia
     * Then:  el resultado debe ser CUENTA_ORIGEN_INVALIDA
     */
    @Test
    public void shouldReturnCuentaOrigenInvalidaWhenCuentaOrigenEsNula() {
        // Arrange
        SolicitudTransferencia solicitud = new SolicitudTransferencia(
            null,           // cuenta origen nula
            "9876543210",
            100_000.0
        );

        // Act
        TransferenciaResult resultado = servicio.procesarTransferencia(solicitud);

        // Assert
        Assert.assertEquals(TransferenciaResult.CUENTA_ORIGEN_INVALIDA, resultado);
    }

    /**
     * RED: número de cuenta origen vacío → CUENTA_ORIGEN_INVALIDA.
     *
     * Given: la cuenta origen existe pero su número es una cadena vacía
     * When:  se solicita la transferencia
     * Then:  el resultado debe ser CUENTA_ORIGEN_INVALIDA
     */
    @Test
    public void shouldReturnCuentaOrigenInvalidaWhenNumeroCuentaEsVacio() {
        // Arrange
        Cuenta cuentaConNumeroVacio = new Cuenta(
            "",             // número de cuenta vacío (caso límite)
            TipoCuenta.AHORROS,
            "AV VILLAS",
            500_000.0
        );
        SolicitudTransferencia solicitud = new SolicitudTransferencia(
            cuentaConNumeroVacio, "9876543210", 100_000.0
        );

        // Act
        TransferenciaResult resultado = servicio.procesarTransferencia(solicitud);

        // Assert
        Assert.assertEquals(TransferenciaResult.CUENTA_ORIGEN_INVALIDA, resultado);
    }

    // =========================================================================
    // CICLO TDD 4 — Cuenta destino inválida
    // =========================================================================

    /**
     * RED: número de cuenta destino vacío → CUENTA_DESTINO_INVALIDA.
     *
     * Given: la cuenta destino tiene número vacío
     * When:  se procesa la transferencia
     * Then:  el resultado debe ser CUENTA_DESTINO_INVALIDA
     */
    @Test
    public void shouldReturnCuentaDestinoInvalidaWhenNumeroCuentaDestinoEsVacio() {
        // Arrange
        SolicitudTransferencia solicitud = new SolicitudTransferencia(
            cuentaOrigenValida,
            "",             // destino vacío
            100_000.0
        );

        // Act
        TransferenciaResult resultado = servicio.procesarTransferencia(solicitud);

        // Assert
        Assert.assertEquals(TransferenciaResult.CUENTA_DESTINO_INVALIDA, resultado);
    }

    /**
     * RED: número de cuenta destino nulo → CUENTA_DESTINO_INVALIDA.
     *
     * Given: la cuenta destino es null
     * When:  se procesa la transferencia
     * Then:  el resultado debe ser CUENTA_DESTINO_INVALIDA
     */
    @Test
    public void shouldReturnCuentaDestinoInvalidaWhenNumeroCuentaDestinoEsNulo() {
        // Arrange
        SolicitudTransferencia solicitud = new SolicitudTransferencia(
            cuentaOrigenValida,
            null,           // destino nulo
            100_000.0
        );

        // Act
        TransferenciaResult resultado = servicio.procesarTransferencia(solicitud);

        // Assert
        Assert.assertEquals(TransferenciaResult.CUENTA_DESTINO_INVALIDA, resultado);
    }

    // =========================================================================
    // CICLO TDD 5 — Validación de monto (clases de equivalencia y valores límite)
    // =========================================================================

    /**
     * RED: monto cero → MONTO_INVALIDO.
     * Valor límite inferior: monto = 0 (borde inferior, inválido)
     *
     * Given: monto igual a $0
     * When:  se procesa la transferencia
     * Then:  el resultado debe ser MONTO_INVALIDO
     */
    @Test
    public void shouldReturnMontoInvalidoWhenMontoEsCero() {
        // Arrange
        SolicitudTransferencia solicitud = new SolicitudTransferencia(
            cuentaOrigenValida, "9876543210", 0.0   // límite inferior
        );

        // Act
        TransferenciaResult resultado = servicio.procesarTransferencia(solicitud);

        // Assert
        Assert.assertEquals(TransferenciaResult.MONTO_INVALIDO, resultado);
    }

    /**
     * RED: monto negativo → MONTO_INVALIDO.
     * Clase inválida: monto < 0
     *
     * Given: monto de -$1.000 (negativo)
     * When:  se procesa la transferencia
     * Then:  el resultado debe ser MONTO_INVALIDO
     */
    @Test
    public void shouldReturnMontoInvalidoWhenMontoEsNegativo() {
        // Arrange
        SolicitudTransferencia solicitud = new SolicitudTransferencia(
            cuentaOrigenValida, "9876543210", -1_000.0
        );

        // Act
        TransferenciaResult resultado = servicio.procesarTransferencia(solicitud);

        // Assert
        Assert.assertEquals(TransferenciaResult.MONTO_INVALIDO, resultado);
    }

    /**
     * RED: monto excede límite diario ($10.000.001) → MONTO_INVALIDO.
     * Valor límite superior: monto > MONTO_MAXIMO
     *
     * Given: monto de $10.000.001 (supera el límite diario)
     * When:  se procesa la transferencia
     * Then:  el resultado debe ser MONTO_INVALIDO
     */
    @Test
    public void shouldReturnMontoInvalidoWhenMontoExcedeLimiteDiario() {
        // Arrange — monto superior al límite: 10.000.000 + 1
        Cuenta cuentaConSaldoAlto = new Cuenta(
            "1234567890", TipoCuenta.AHORROS, "AV VILLAS", 20_000_000.0
        );
        SolicitudTransferencia solicitud = new SolicitudTransferencia(
            cuentaConSaldoAlto, "9876543210", 10_000_001.0
        );

        // Act
        TransferenciaResult resultado = servicio.procesarTransferencia(solicitud);

        // Assert
        Assert.assertEquals(TransferenciaResult.MONTO_INVALIDO, resultado);
    }

    /**
     * GREEN: monto exactamente en el límite máximo permitido → EXITOSA.
     * Valor límite: monto = MONTO_MAXIMO (borde superior válido)
     *
     * Given: cuenta con $10.000.000 de saldo y monto = $10.000.000
     * When:  se procesa la transferencia
     * Then:  el resultado debe ser EXITOSA
     */
    @Test
    public void shouldReturnExitosaWhenMontoEsExactamenteElLimiteMaximo() {
        // Arrange
        Cuenta cuentaLimite = new Cuenta(
            "1234567890", TipoCuenta.AHORROS, "AV VILLAS", 10_000_000.0
        );
        SolicitudTransferencia solicitud = new SolicitudTransferencia(
            cuentaLimite, "9876543210", 10_000_000.0   // límite superior exacto
        );

        // Act
        TransferenciaResult resultado = servicio.procesarTransferencia(solicitud);

        // Assert
        Assert.assertEquals(TransferenciaResult.EXITOSA, resultado);
    }

    // =========================================================================
    // CICLO TDD 6 — Saldo insuficiente
    // =========================================================================

    /**
     * RED: saldo < monto → SALDO_INSUFICIENTE.
     *
     * Given: cuenta con saldo $100.000 y monto solicitado $500.001
     * When:  se procesa la transferencia
     * Then:  el resultado debe ser SALDO_INSUFICIENTE
     */
    @Test
    public void shouldReturnSaldoInsuficienteWhenSaldoMenorQueMonto() {
        // Arrange
        Cuenta cuentaBajoSaldo = new Cuenta(
            "1234567890", TipoCuenta.AHORROS, "AV VILLAS", 100_000.0
        );
        SolicitudTransferencia solicitud = new SolicitudTransferencia(
            cuentaBajoSaldo, "9876543210", 500_001.0
        );

        // Act
        TransferenciaResult resultado = servicio.procesarTransferencia(solicitud);

        // Assert
        Assert.assertEquals(TransferenciaResult.SALDO_INSUFICIENTE, resultado);
    }

    /**
     * GREEN: saldo = monto exactamente → EXITOSA (valor límite de saldo).
     *
     * Given: cuenta con saldo exactamente igual al monto a transferir
     * When:  se procesa la transferencia
     * Then:  el resultado debe ser EXITOSA
     */
    @Test
    public void shouldReturnExitosaWhenSaldoEsExactamenteIgualAlMonto() {
        // Arrange
        Cuenta cuentaSaldoJusto = new Cuenta(
            "1234567890", TipoCuenta.AHORROS, "AV VILLAS", 300_000.0
        );
        SolicitudTransferencia solicitud = new SolicitudTransferencia(
            cuentaSaldoJusto, "9876543210", 300_000.0   // saldo = monto (límite)
        );

        // Act
        TransferenciaResult resultado = servicio.procesarTransferencia(solicitud);

        // Assert
        Assert.assertEquals(TransferenciaResult.EXITOSA, resultado);
    }

    // =========================================================================
    // CICLO TDD 7 — Entidad bancaria no soportada
    // =========================================================================

    /**
     * RED: cuenta origen con entidad no soportada → ENTIDAD_NO_SOPORTADA.
     *
     * Given: cuenta origen pertenece a "BANCO FICTICIO" (no en la lista)
     * When:  se procesa la transferencia
     * Then:  el resultado debe ser ENTIDAD_NO_SOPORTADA
     */
    @Test
    public void shouldReturnEntidadNoSoportadaWhenEntidadOrigenNoEstaEnLaLista() {
        // Arrange
        Cuenta cuentaEntidadDesconocida = new Cuenta(
            "1234567890", TipoCuenta.AHORROS, "BANCO FICTICIO", 500_000.0
        );
        SolicitudTransferencia solicitud = new SolicitudTransferencia(
            cuentaEntidadDesconocida, "9876543210", 100_000.0
        );

        // Act
        TransferenciaResult resultado = servicio.procesarTransferencia(solicitud);

        // Assert
        Assert.assertEquals(TransferenciaResult.ENTIDAD_NO_SOPORTADA, resultado);
    }

    /**
     * GREEN: entidades bancarias soportadas — prueba con cuenta corriente Davivienda.
     *
     * Given: cuenta corriente Davivienda con saldo suficiente
     * When:  se solicita una transferencia válida
     * Then:  el resultado debe ser EXITOSA
     */
    @Test
    public void shouldReturnExitosaWhenEntidadEsDaviviendaCuentaCorriente() {
        // Arrange
        Cuenta cuentaDavivienda = new Cuenta(
            "5555555555", TipoCuenta.CORRIENTE, "DAVIVIENDA", 1_000_000.0
        );
        SolicitudTransferencia solicitud = new SolicitudTransferencia(
            cuentaDavivienda, "6666666666", 500_000.0
        );

        // Act
        TransferenciaResult resultado = servicio.procesarTransferencia(solicitud);

        // Assert
        Assert.assertEquals(TransferenciaResult.EXITOSA, resultado);
    }
}
