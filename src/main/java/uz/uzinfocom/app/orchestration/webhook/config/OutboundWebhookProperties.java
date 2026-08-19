package uz.uzinfocom.app.orchestration.webhook.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Connect/read timeouts for {@code outboundWebhookRestClient} - no
 * {@code baseUrl} here, unlike {@code LisProperties}/{@code Api2Properties}:
 * every {@link uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient}
 * has its own callback URL, so the target is per-request, not fixed at
 * startup.
 */
@Validated
@ConfigurationProperties(prefix = "integration.webhook")
public record OutboundWebhookProperties(

        @NotNull Duration connectTimeout,

        @NotNull Duration readTimeout
) {
}
