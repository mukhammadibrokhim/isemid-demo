package uz.uzinfocom.app.platform.webhook.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
public class OutboundWebhookSchedulingConfig {
}
