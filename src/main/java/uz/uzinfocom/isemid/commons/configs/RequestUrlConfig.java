package uz.uzinfocom.isemid.commons.configs;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import uz.uzinfocom.isemid.commons.configs.properties.IntegrationProperties;
import uz.uzinfocom.isemid.commons.utils.RequestUrlUtils;

@Configuration
@RequiredArgsConstructor
public class RequestUrlConfig {

    private final IntegrationProperties integrationProperties;

    @PostConstruct
    public void initializeRequestUrlBuilder() {
        RequestUrlUtils.setPublicBaseUrl(
                integrationProperties.hosts().isemidPublicApiBaseUrl()
        );
    }
}