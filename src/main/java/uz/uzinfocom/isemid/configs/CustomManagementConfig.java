package uz.uzinfocom.isemid.configs;


import jakarta.annotation.PostConstruct;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({WebEndpointProperties.class, PrometheusProperties.class})
public class CustomManagementConfig {

    private final WebEndpointProperties webEndpointProperties;
    private final PrometheusProperties prometheusProperties;

    public CustomManagementConfig(WebEndpointProperties webEndpointProperties,
                                  PrometheusProperties prometheusProperties) {
        this.webEndpointProperties = webEndpointProperties;
        this.prometheusProperties = prometheusProperties;
    }

    @PostConstruct
    public void configureActuatorAndPrometheus() {
        webEndpointProperties.setBasePath("/actuator");
        webEndpointProperties.getExposure().getInclude().add("*");
        prometheusProperties.setEnabled(true);
    }
}