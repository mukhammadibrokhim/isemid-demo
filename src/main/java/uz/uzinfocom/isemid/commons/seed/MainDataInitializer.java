package uz.uzinfocom.isemid.commons.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
//@ConditionalOnProperty(name = "runner.main.init", havingValue = "true")
public class MainDataInitializer implements CommandLineRunner {
    private final List<ModuleDataInitializer> initializers;

    @Override
    public void run(String... args) {
        log.info("🚀 Running MainDataInitializer...");

        for (ModuleDataInitializer initializer : initializers) {
            log.info("▶️ Initializing: {}", initializer.getClass().getSimpleName());
            try {
//                initializer.init();
            } catch (Exception e) {
                log.error("❌ Failed to initialize module {}: {}", initializer.getClass().getSimpleName(), e.getMessage(), e);
                throw e; // Fail fast if one of them is critical
            }
        }

        log.info("✅ All enabled data initializers completed successfully.");
    }
}
