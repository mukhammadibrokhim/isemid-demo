package uz.uzinfocom.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point of the ISEMID backend application.
 *
 * <p>
 * This class bootstraps the Spring Boot application context and initializes
 * all configured infrastructure, security, persistence, integration, and
 * domain-related components required by the ISEMID system.
 * </p>
 *
 * <p>
 * The application serves as the backend foundation for epidemiological
 * registration, case management, organization-scoped access control,
 * reporting, monitoring, and external system integrations.
 * </p>
 *
 * @author Muhammadibrohim Tursunboyev
 */
@SpringBootApplication
public class Application {

    /**
     * Starts the ISEMID backend application.
     *
     * @param args command-line arguments passed during application startup
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}