package uz.uzinfocom.app.platform.http;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.security.http")
public class PlatformRestClientProperties {

    @NotNull
    private Duration connectTimeout = Duration.ofSeconds(3);

    @NotNull
    private Duration readTimeout = Duration.ofSeconds(5);

    @NotNull
    private Duration connectionRequestTimeout = Duration.ofSeconds(2);

    @NotNull
    private Duration connectionTimeToLive = Duration.ofMinutes(5);

    @NotNull
    private Duration evictIdleConnectionsAfter = Duration.ofSeconds(30);

    @Min(1)
    private int maxConnections = 100;

    @Min(1)
    private int maxConnectionsPerRoute = 50;

}
