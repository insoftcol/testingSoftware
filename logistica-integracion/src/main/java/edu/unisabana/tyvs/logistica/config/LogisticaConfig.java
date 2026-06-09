package edu.unisabana.tyvs.logistica.config;

import edu.unisabana.tyvs.logistica.application.port.out.CotizacionRepositoryPort;
import edu.unisabana.tyvs.logistica.application.usecase.ServicioTarifa;
import edu.unisabana.tyvs.logistica.infrastructure.persistence.CotizacionRepository;
import org.springframework.context.annotation.*;

@Configuration
public class LogisticaConfig {

    @Bean
    public CotizacionRepositoryPort cotizacionRepositoryPort() throws Exception {
        String jdbc = "jdbc:h2:mem:logisticadb;DB_CLOSE_DELAY=-1";
        CotizacionRepository repo = new CotizacionRepository(jdbc);
        repo.initSchema();
        return repo;
    }

    @Bean
    public ServicioTarifa servicioTarifa(CotizacionRepositoryPort port) {
        return new ServicioTarifa(port);
    }
}
