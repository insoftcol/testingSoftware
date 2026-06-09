package edu.unisabana.tyvs.registry.delivery.rest;

import edu.unisabana.tyvs.registry.application.port.out.RegistryRepositoryPort;
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
 * Prueba de SISTEMA (caja negra) para {@link RegistryController}.
 *
 * <p>Levanta el contexto completo de Spring Boot con base de datos H2 en memoria
 * y valida el comportamiento del endpoint REST {@code POST /register}
 * sin conocer la implementación interna.</p>
 *
 * <h3>Cómo funciona</h3>
 * <ul>
 *   <li>{@code @SpringBootTest}: levanta el servidor en un puerto aleatorio.</li>
 *   <li>{@code TestRestTemplate}: cliente HTTP para hacer peticiones reales.</li>
 *   <li>Los beans se proveen por {@code RegistryConfig} (main).</li>
 * </ul>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegistryControllerIT {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private RegistryRepositoryPort repo;

    /** Limpia la base de datos antes de cada prueba para garantizar aislamiento. */
    @Before
    public void setUp() throws Exception {
        repo.deleteAll();
    }

    // =========================================================================
    // Prueba de sistema 1 — Registro válido
    // BDD: Given JSON de persona válida; When POST /register; Then HTTP 200 + "VALID"
    // =========================================================================

    @Test
    public void shouldReturnValidWhenPersonaEsValida() {
        // Arrange
        String json = "{\"name\":\"Ana\",\"id\":100,\"age\":30,\"gender\":\"FEMALE\",\"alive\":true}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Act
        ResponseEntity<String> resp = rest.postForEntity(
            "/register", new HttpEntity<>(json, headers), String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("VALID", resp.getBody());
    }

    // =========================================================================
    // Prueba de sistema 2 — Persona menor de edad
    // BDD: Given edad=15; When POST /register; Then HTTP 200 + "UNDERAGE"
    // =========================================================================

    @Test
    public void shouldReturnUnderageWhenEdadEsMenorDe18() {
        // Arrange
        String json = "{\"name\":\"Joven\",\"id\":101,\"age\":15,\"gender\":\"MALE\",\"alive\":true}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Act
        ResponseEntity<String> resp = rest.postForEntity(
            "/register", new HttpEntity<>(json, headers), String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("UNDERAGE", resp.getBody());
    }

    // =========================================================================
    // Prueba de sistema 3 — Persona muerta
    // BDD: Given alive=false; When POST /register; Then HTTP 200 + "DEAD"
    // =========================================================================

    @Test
    public void shouldReturnDeadWhenPersonaNoEstaViva() {
        // Arrange
        String json = "{\"name\":\"Fallecido\",\"id\":102,\"age\":40,\"gender\":\"MALE\",\"alive\":false}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Act
        ResponseEntity<String> resp = rest.postForEntity(
            "/register", new HttpEntity<>(json, headers), String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("DEAD", resp.getBody());
    }
}
