package uz.uzinfocom.app.platform.web.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.web.pagination")
public class WebPaginationProperties {

    private int defaultPage = 1;
    private int defaultSize = 20;
    private int maxSize = 200;
}
