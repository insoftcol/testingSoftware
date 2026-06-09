package edu.unisabana.tyvs.logistica.system;

import edu.unisabana.tyvs.logistica.application.port.out.CotizacionRepositoryPort;
import edu.unisabana.tyvs.logistica.delivery.rest.CotizacionResponseDTO;
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
 * Prueba de SISTEMA — endpoint REST {@code POST /cotizacion}.
 *
 * <p>Valida el comportamiento de caja negra del sistema logístico completo:
 * Controller → Service → Repository → HTTP response.</p>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CotizacionControllerIT {

    @Autowired private TestRestTemplate rest;
    @Autowired private CotizacionRepositoryPort repo;

    @Before
    public void setUp() throws Exception { repo.deleteAll(); }

    private ResponseEntity<CotizacionResponseDTO> post(String json) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return rest.postForEntity("/cotizacion",
            new HttpEntity<>(json, h), CotizacionResponseDTO.class);
    }

    // =========================================================================
    // Sistema 1 — Ruta válida MEDIA → HTTP 200, valor $38.000
    // =========================================================================

    @Test
    public void shouldReturn200ExitosaWhenRutaValidaBogotaMedellin() {
        // Arrange
        String json = "{\"ciudadOrigen\":\"BOGOTA\",\"ciudadDestino\":\"MEDELLIN\"," +
                      "\"pesoKg\":10.0,\"tipoEnvio\":\"NORMAL\"}";
        // Act
        ResponseEntity<CotizacionResponseDTO> resp = post(json);

        // Assert
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("EXITOSA",  resp.getBody().getEstado());
        assertEquals(38_000.0,   resp.getBody().getValorTotal(), 0.01);
    }

    // =========================================================================
    // Sistema 2 — Misma ciudad → HTTP 400, MISMA_CIUDAD
    // =========================================================================

    @Test
    public void shouldReturn400WhenCiudadOrigenIgualDestino() {
        // Arrange
        String json = "{\"ciudadOrigen\":\"CALI\",\"ciudadDestino\":\"CALI\"," +
                      "\"pesoKg\":10.0,\"tipoEnvio\":\"NORMAL\"}";
        // Act
        ResponseEntity<CotizacionResponseDTO> resp = post(json);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("MISMA_CIUDAD", resp.getBody().getEstado());
    }

    // =========================================================================
    // Sistema 3 — Peso cero → HTTP 400, PESO_INVALIDO
    // =========================================================================

    @Test
    public void shouldReturn400WhenPesoEsCero() {
        // Arrange
        String json = "{\"ciudadOrigen\":\"BOGOTA\",\"ciudadDestino\":\"MEDELLIN\"," +
                      "\"pesoKg\":0.0,\"tipoEnvio\":\"NORMAL\"}";
        // Act
        ResponseEntity<CotizacionResponseDTO> resp = post(json);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("PESO_INVALIDO", resp.getBody().getEstado());
    }
}
