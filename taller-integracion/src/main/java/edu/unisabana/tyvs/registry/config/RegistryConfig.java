package edu.unisabana.tyvs.registry.config;

import edu.unisabana.tyvs.registry.application.port.out.RegistryRepositoryPort;
import edu.unisabana.tyvs.registry.application.usecase.Registry;
import edu.unisabana.tyvs.registry.infrastructure.persistence.RegistryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración principal de Spring:
 * define los beans de la capa de aplicación e infraestructura.
 *
 * Para pruebas de integración (RegistryControllerIT) los beans
 * son sobreescritos por @TestConfiguration con una URL H2 limpia.
 */
@Configuration
public class RegistryConfig {

    @Bean
    public RegistryRepositoryPort registryRepositoryPort() throws Exception {
        String jdbc = "jdbc:h2:mem:regdb;DB_CLOSE_DELAY=-1";
        RegistryRepository repo = new RegistryRepository(jdbc);
        repo.initSchema();
        return repo;
    }

    @Bean
    public Registry registry(RegistryRepositoryPort port) {
        return new Registry(port);
    }
}
