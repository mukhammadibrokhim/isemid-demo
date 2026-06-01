
package uz.uzinfocom.isemid.features.integrations.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uz.uzinfocom.isemid.commons.integration.IntegrationUrlFactory;
import uz.uzinfocom.isemid.features.security.dtos.responses.TokenResponse;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@AllArgsConstructor
@Slf4j
public class OAuthTokenServiceImpl implements OAuthTokenService {
    private static final String CLIENT_ID = "94f83670-f841-4ec5-a593-72d2873f054b";
    private static final String CLIENT_SECRET = "IAIkBdrmSVPWPH5vtvihY4vOfLW3V19oDEC9wHIs";
    private static final String USERNAME = "test-odiljon";
    private static final String PASSWORD = "test-odiljon";
    private final IntegrationUrlFactory integrationUrlFactory;

    private static String accessToken;
    private static long tokenExpiryTime = 0;

    @Override
    public synchronized String getAccessToken() {
        if (accessToken == null || System.currentTimeMillis() >= tokenExpiryTime) {
            log.info("🔄 Access token expired or missing, refreshing token...");
            refreshToken();
        } else {
            log.info("✅ Using cached access token");
        }
        return accessToken;
    }

    private synchronized void refreshToken() {
        try {
            URI uri = integrationUrlFactory.ssoTokenUrl();
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String auth = CLIENT_ID + ":" + CLIENT_SECRET;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            headers.set("Authorization", "Basic " + encodedAuth);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "password");
            params.add("username", USERNAME);
            params.add("password", PASSWORD);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<TokenResponse> response = restTemplate.exchange(
                    uri, HttpMethod.POST, request, TokenResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                TokenResponse tokenResponse = response.getBody();
                accessToken = tokenResponse.getAccess_token();
                tokenExpiryTime = System.currentTimeMillis() + (tokenResponse.getExpires_in() * 1000); // Convert seconds to milliseconds

                log.info("✅ Token successfully obtained! Expires in {} seconds", tokenResponse.getExpires_in());
            } else {
                log.error("❌ Failed to retrieve access token. Response: {}", response);
                throw new RuntimeException("Failed to obtain token: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ Error occurred while fetching the token", e);
            throw new RuntimeException("Error while retrieving token: " + e.getMessage(), e);
        }
    }
}
