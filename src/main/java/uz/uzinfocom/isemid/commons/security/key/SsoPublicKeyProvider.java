package uz.uzinfocom.isemid.commons.security.key;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uz.uzinfocom.isemid.commons.cache.CacheNames;
import uz.uzinfocom.isemid.commons.integration.IntegrationUrlFactory;

import java.security.interfaces.RSAPublicKey;

@Slf4j
@Service
@RequiredArgsConstructor
public class SsoPublicKeyProvider implements BeanPostProcessor {

    private final RestClient restClient;
    private final IntegrationUrlFactory integrationUrlFactory;
    private final PemPublicKeyParser pemPublicKeyParser;

    @Cacheable(
            cacheNames = CacheNames.SSO_PUBLIC_KEY,
            sync = true
    )
    public RSAPublicKey getPublicKey() {
        var publicKeyUrl = integrationUrlFactory.ssoPublicKeyUrl();

        try {
            log.info("Fetching SSO public key from endpoint: {}", publicKeyUrl);

            ResponseEntity<String> response = restClient
                    .get()
                    .uri(publicKeyUrl)
                    .accept(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.ALL)
                    .retrieve()
                    .toEntity(String.class);

            String responseBody = response.getBody();

            if (responseBody == null || responseBody.isBlank()) {
                throw new IllegalStateException(
                        "SSO public key response body is empty. url=" + publicKeyUrl
                                + ", status=" + response.getStatusCode()
                                + ", location=" + response.getHeaders().getLocation()
                );
            }

            RSAPublicKey publicKey = pemPublicKeyParser.parseRsaPublicKey(
                    extractPublicKeyValue(responseBody)
            );

            log.info("SSO public key fetched successfully");

            return publicKey;
        } catch (Exception ex) {
            log.error("Failed to fetch SSO public key. url={}", publicKeyUrl, ex);
            throw new IllegalStateException("SSO public key is unavailable", ex);
        }
    }

    private String extractPublicKeyValue(String responseBody) {
        String body = responseBody.trim();

        if (body.contains("-----BEGIN PUBLIC KEY-----")) {
            return body;
        }

        if (body.startsWith("MI")) {
            return body;
        }

        throw new IllegalStateException("Unsupported SSO public key response format");
    }


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
        throws BeansException {
        var publicKeyUrl = integrationUrlFactory.ssoPublicKeyUrl();

        try {
            log.info("Fetching SSO public key from endpoint: {}", publicKeyUrl);

            ResponseEntity<String> response = restClient
                .get()
                .uri(publicKeyUrl)
                .accept(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.ALL)
                .retrieve()
                .toEntity(String.class);

            String responseBody = response.getBody();

            if (responseBody == null || responseBody.isBlank()) {
                throw new IllegalStateException(
                    "SSO public key response body is empty. url=" + publicKeyUrl
                        + ", status=" + response.getStatusCode()
                        + ", location=" + response.getHeaders().getLocation()
                );
            }

            RSAPublicKey publicKey = pemPublicKeyParser.parseRsaPublicKey(
                extractPublicKeyValue(responseBody)
            );

            log.info("SSO public key fetched successfully");

        } catch (Exception ex) {
            log.error("Failed to fetch SSO public key. url={}", publicKeyUrl, ex);
            throw new IllegalStateException("SSO public key is unavailable", ex);
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

}