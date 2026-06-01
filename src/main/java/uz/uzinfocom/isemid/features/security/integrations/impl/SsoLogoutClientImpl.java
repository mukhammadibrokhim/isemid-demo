package uz.uzinfocom.isemid.features.security.integrations.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uz.uzinfocom.isemid.commons.integration.IntegrationUrlFactory;
import uz.uzinfocom.isemid.features.security.integrations.SsoLogoutClient;

import java.net.URI;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@RequiredArgsConstructor
public class SsoLogoutClientImpl implements SsoLogoutClient {
    private final RestClient restClient;
    private final IntegrationUrlFactory integrationUrlFactory;

    @Override
    public void logout(String rawAccessToken) {
        URI uri = integrationUrlFactory.ssoLogoutUrl();
        restClient.post()
                .uri(uri)
                .header(AUTHORIZATION, "Bearer " + rawAccessToken)
                .retrieve()
                .toBodilessEntity();
    }
}
