package uz.uzinfocom.app.modules.reference.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import uz.uzinfocom.app.modules.reference.application.population.sync.PopulationSyncProperties;

@Configuration
@EnableConfigurationProperties(PopulationSyncProperties.class)
public class PopulationSyncConfig {
}
