package uz.uzinfocom.app.modules.reference.application.population.sync;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Config for the stat.uz SDMX population sync. Overridable per environment
 * via {@code app.reference.population.*} (or the matching env vars) so a new
 * dataset id or a stricter {@code min-year} needs no redeploy.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.reference.population")
public class PopulationSyncProperties {

    /** Public SDMX JSON export — dataset 246, "Doimiy aholi soni (jami)". */
    private String sdmxUrl = "https://siat.stat.uz/media/uploads/sdmx/sdmx_data_246.json";

    /** Only import (and keep) figures for this year and later. */
    private int minYear = 2025;
}
