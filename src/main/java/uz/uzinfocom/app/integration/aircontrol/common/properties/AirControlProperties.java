package uz.uzinfocom.app.integration.aircontrol.common.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * AIRCONTROL (Gidrometeorologiya agentligi, atrof muhit ifloslanishi monitoringi)
 * read API, reached through the IIP/IMUT network with HTTP basic auth. Credentials
 * are deliberately not {@code @NotBlank}: a missing login must not stop the whole
 * application from booting - {@code AirControlClient} fails the call itself with a
 * clear "not configured" error instead.
 */
@Validated
@ConfigurationProperties(prefix = "integration.aircontrol")
public record AirControlProperties(
        @DefaultValue("https://meteoapi.meteo.uz") @NotBlank String baseUrl,
        /* Method 1: list of monitoring stations. */
        @DefaultValue("/api/atmosphere/horiba/") @NotBlank String stationsPath,
        /* Method 2: latest measurement of one station, the station id is appended. */
        @DefaultValue("/api/atmosphere/horiba/") @NotBlank String measurementPath,
        String username,
        String password,
        @DefaultValue("3s") @NotNull Duration connectTimeout,
        /* The agreement caps an answer at 5 s on average 3 s, so 8 s leaves headroom without hanging a sync. */
        @DefaultValue("8s") @NotNull Duration readTimeout
) {
}
