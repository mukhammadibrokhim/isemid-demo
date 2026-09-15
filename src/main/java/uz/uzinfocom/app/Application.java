package uz.uzinfocom.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

/**
 * Entry point of the ISEMID backend — the epidemiological surveillance and
 * case-management platform covering Form No. 058 notifications, the five
 * epidemiological investigation card types, patient registration, and the
 * lab/procedure ("act") workflow attached to a card.
 *
 * <p>This class must stay at the {@code uz.uzinfocom.app} root package:
 * {@link SpringBootApplication} implies component scanning rooted at this
 * class's package, so every feature module ({@code modules.*}), shared
 * infrastructure ({@code platform.*}), and integration ({@code
 * integration.*}) is only wired into the context because this class is
 * their common ancestor. Moving it into a subpackage would silently stop
 * anything outside that subpackage from being picked up.
 *
 * @author Muhammadibrohim Tursunboyev
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        // Must run before SpringApplication.run() — pinning the JVM default
        // timezone via a @PostConstruct bean fires too late for beans that
        // initialize early in context refresh (e.g. the DataSource, Liquibase),
        // leaving a window where LocalDateTime.now()/new Date() would fall back
        // to the host's system timezone instead of Asia/Tashkent.
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tashkent"));
        SpringApplication.run(Application.class, args);
    }
}