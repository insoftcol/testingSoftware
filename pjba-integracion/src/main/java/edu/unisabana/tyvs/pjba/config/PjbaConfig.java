package edu.unisabana.tyvs.pjba.config;

import edu.unisabana.tyvs.pjba.application.port.out.TransferenciaRepositoryPort;
import edu.unisabana.tyvs.pjba.application.usecase.ServicioTransferencia;
import edu.unisabana.tyvs.pjba.infrastructure.persistence.TransferenciaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PjbaConfig {

    @Bean
    public TransferenciaRepositoryPort transferenciaRepositoryPort() throws Exception {
        String jdbc = "jdbc:h2:mem:pjbadb;DB_CLOSE_DELAY=-1";
        TransferenciaRepository repo = new TransferenciaRepository(jdbc);
        repo.initSchema();
        return repo;
    }

    @Bean
    public ServicioTransferencia servicioTransferencia(TransferenciaRepositoryPort port) {
        return new ServicioTransferencia(port);
    }
}
