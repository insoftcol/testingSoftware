package edu.unisabana.tyvs.pjba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * Punto de entrada del sistema PJBA - Transferencias Bancarias.
 *
 * <p>Se excluyen las autoconfiguraciones de DataSource/JPA porque
 * el proyecto usa JDBC directo via DriverManager (sin Spring DataSource).</p>
 */
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
public class PjbaApplication {
    public static void main(String[] args) {
        SpringApplication.run(PjbaApplication.class, args);
    }
}
