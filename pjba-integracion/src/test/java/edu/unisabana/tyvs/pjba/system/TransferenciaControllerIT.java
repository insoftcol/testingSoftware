package edu.unisabana.tyvs.pjba.system;

import edu.unisabana.tyvs.pjba.application.port.out.TransferenciaRepositoryPort;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * Prueba de SISTEMA (caja negra) para {@code POST /transferencia}.
 *
 * <p>Levanta el contexto completo de Spring Boot con servidor real
 * en puerto aleatorio. Valida el endpoint REST sin conocer la
 * implementación interna: solo HTTP in → HTTP out.</p>
 *
 * <h3>Equivalencia con pruebas de integración</h3>
 * <table>
 *   <tr><th>Tipo</th><th>Capa probada</th><th>Herramienta</th></tr>
 *   <tr><td>H2Test</td><td>Service + Repository</td><td>H2 directo</td></tr>
 *   <tr><td>MockTest</td><td>Service (aislado)</td><td>Mockito</td></tr>
 *   <tr><td>IT (este)</td><td>Stack completo</td><td>TestRestTemplate</td></tr>
 * </table>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransferenciaControllerIT {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private TransferenciaRepositoryPort repo;

    @Before
    public void setUp() throws Exception {
        repo.deleteAll();
    }

    private ResponseEntity<String> post(String json) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return rest.postForEntity("/transferencia", new HttpEntity<>(json, h), String.class);
    }

    // =========================================================================
    // Sistema 1 — Transferencia válida AV Villas → HTTP 200 EXITOSA
    // =========================================================================

    @Test
    public void shouldReturn200ExitosaWhenTransferenciaEsValida() {
        // Arrange
        String json = "{" +
            "\"numeroCuentaOrigen\":\"1234567890\"," +
            "\"tipoCuenta\":\"AHORROS\"," +
            "\"entidadBancaria\":\"AV VILLAS\"," +
            "\"saldoOrigen\":500000.0," +
            "\"numeroCuentaDestino\":\"9876543210\"," +
            "\"monto\":200000.0" +
        "}";

        // Act
        ResponseEntity<String> resp = post(json);

        // Assert
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("EXITOSA", resp.getBody());
    }

    // =========================================================================
    // Sistema 2 — Monto cero → HTTP 400 MONTO_INVALIDO
    // =========================================================================

    @Test
    public void shouldReturn400WhenMontoEsCero() {
        // Arrange
        String json = "{" +
            "\"numeroCuentaOrigen\":\"1234567890\"," +
            "\"tipoCuenta\":\"AHORROS\"," +
            "\"entidadBancaria\":\"AV VILLAS\"," +
            "\"saldoOrigen\":500000.0," +
            "\"numeroCuentaDestino\":\"9876543210\"," +
            "\"monto\":0.0" +
        "}";

        // Act
        ResponseEntity<String> resp = post(json);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("MONTO_INVALIDO", resp.getBody());
    }

    // =========================================================================
    // Sistema 3 — Saldo insuficiente → HTTP 400 SALDO_INSUFICIENTE
    // =========================================================================

    @Test
    public void shouldReturn400WhenSaldoEsInsuficiente() {
        // Arrange
        String json = "{" +
            "\"numeroCuentaOrigen\":\"1234567890\"," +
            "\"tipoCuenta\":\"AHORROS\"," +
            "\"entidadBancaria\":\"AV VILLAS\"," +
            "\"saldoOrigen\":100000.0," +
            "\"numeroCuentaDestino\":\"9876543210\"," +
            "\"monto\":500001.0" +
        "}";

        // Act
        ResponseEntity<String> resp = post(json);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("SALDO_INSUFICIENTE", resp.getBody());
    }
}
