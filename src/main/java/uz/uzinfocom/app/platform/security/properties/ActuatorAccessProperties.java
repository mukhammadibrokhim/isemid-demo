package uz.uzinfocom.app.platform.security.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Who may scrape {@code /v1/actuator/prometheus}. A Prometheus scraper
 * cannot present an SSO bearer token, so instead of authentication the
 * endpoint is restricted to source networks (IPv4 CIDR CSV, matched by
 * {@code IpAllowlistMatcher}). The default covers loopback and the RFC 1918
 * private ranges; narrow it to the scraper's address in production.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.actuator")
public class ActuatorAccessProperties {

    private String prometheusAllowedIps = "127.0.0.0/8,10.0.0.0/8,172.16.0.0/12,192.168.0.0/16";
}
