package uz.uzinfocom.app.platform.observability;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.observability")
public class ObservabilityProperties {

    private String traceIdHeader = "X-Trace-Id";
}
