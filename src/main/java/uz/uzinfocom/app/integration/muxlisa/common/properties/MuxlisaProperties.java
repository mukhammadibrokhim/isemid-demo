package uz.uzinfocom.app.integration.muxlisa.common.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Muxlisa AI speech-to-text (sync API v2). The API key is deliberately not
 * {@code @NotBlank}: a missing key must not stop the whole application from
 * booting - {@code MuxlisaSttClient} fails the STT call itself with a clear
 * "not configured" error instead.
 */
@Validated
@ConfigurationProperties(prefix = "integration.muxlisa")
public record MuxlisaProperties(
        @DefaultValue("https://service.muxlisa.uz") @NotBlank String baseUrl,
        @DefaultValue("/api/v2/stt") @NotBlank String sttEndpoint,
        String apiKey,
        @DefaultValue("3s") @NotNull Duration connectTimeout,
        /* STT time is proportional to audio length (up to 60 s of audio), so longer than the other integrations. */
        @DefaultValue("30s") @NotNull Duration readTimeout,
        /* Muxlisa's own limit is 5 MB; larger audio is refused before it ever leaves our server. */
        @DefaultValue("5MB") @NotNull DataSize maxAudioSize,
        /* Caps how many STT calls run at once so one burst cannot exhaust the balance or the HTTP pool. */
        @DefaultValue("8") @Min(1) int maxConcurrentRequests
) {
}
