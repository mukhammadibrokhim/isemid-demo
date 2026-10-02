package uz.uzinfocom.app.orchestration.webhook.crypto;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(WebhookCryptoProperties.class)
public class WebhookCryptoConfig {
}
