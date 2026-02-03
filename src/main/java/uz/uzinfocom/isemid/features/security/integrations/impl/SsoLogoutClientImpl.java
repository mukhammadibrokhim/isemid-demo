package uz.uzinfocom.isemid.features.security.integrations.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.features.security.integrations.SsoLogoutClient;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@RequiredArgsConstructor
public class SsoLogoutClientImpl implements SsoLogoutClient {
    private final RestClient restClient;

    @Override
    public void logout(String rawAccessToken) {

        restClient.post()
                .uri(ApiPaths.ExternalApi.SSO_LOGOUT)
                .header(AUTHORIZATION, "Bearer " + rawAccessToken)
                .retrieve()
                .toBodilessEntity();
    }
}
