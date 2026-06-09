package edu.unisabana.tyvs.logistica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * Punto de entrada del sistema Logístico - Cotizador de Envíos.
 *
 * <p>Se excluyen las autoconfiguraciones de DataSource/JPA porque
 * el proyecto usa JDBC directo via DriverManager (sin Spring DataSource).</p>
 */
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
public class LogisticaApplication {
    public static void main(String[] args) {
        SpringApplication.run(LogisticaApplication.class, args);
    }
}
