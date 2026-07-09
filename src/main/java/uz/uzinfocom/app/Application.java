package uz.uzinfocom.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
        SpringApplication.run(Application.class, args);
    }
}