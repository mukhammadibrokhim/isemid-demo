package uz.uzinfocom.app.platform.http;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.http")
public class PlatformRestClientProperties {

    private Duration connectTimeout = Duration.ofSeconds(3);

    private Duration readTimeout = Duration.ofSeconds(5);

    private Duration connectionRequestTimeout = Duration.ofSeconds(2);

    private Duration connectionTimeToLive = Duration.ofMinutes(5);

    private Duration evictIdleConnectionsAfter = Duration.ofSeconds(30);

    private int maxConnections = 100;

    private int maxConnectionsPerRoute = 50;

}
