package uz.uzinfocom.isemid.configs;


import jakarta.annotation.PostConstruct;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(WebEndpointProperties.class)
public class CustomManagementConfig {

    private final WebEndpointProperties webEndpointProperties;

    public CustomManagementConfig(WebEndpointProperties webEndpointProperties) {
        this.webEndpointProperties = webEndpointProperties;
    }

    @PostConstruct
    public void configureActuator() {
        webEndpointProperties.setBasePath("/actuator");
        webEndpointProperties.getExposure().getInclude().add("*");
    }
}