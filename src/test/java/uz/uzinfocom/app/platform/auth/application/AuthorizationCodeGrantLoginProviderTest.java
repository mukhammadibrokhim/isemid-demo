package uz.uzinfocom.app.platform.auth.application;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.platform.auth.application.exception.InvalidLoginCredentialsException;
import uz.uzinfocom.app.platform.auth.application.exception.InvalidLoginRequestException;
import uz.uzinfocom.app.platform.auth.application.exception.LoginProviderMisconfiguredException;
import uz.uzinfocom.app.platform.auth.properties.LoginGrantType;
import uz.uzinfocom.app.platform.auth.properties.LoginProvidersProperties.ProviderProperties;
import uz.uzinfocom.app.platform.auth.web.dto.LoginRequest;
import uz.uzinfocom.app.platform.resilience.CircuitBreakerNames;
import uz.uzinfocom.app.platform.resilience.DynamicCircuitBreakerLookup;
import uz.uzinfocom.app.platform.resilience.TestCircuitBreakerLookups;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

class AuthorizationCodeGrantLoginProviderTest {

    private static final String TOKEN_URL = "https://playground.dhp.uz/sso/oauth/token";

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    private ProviderProperties properties() {
        ProviderProperties properties = new ProviderProperties();
        properties.setEnabled(true);
        properties.setTokenUrl(TOKEN_URL);
        properties.setClientId("emid.conf.web");
        properties.setClientSecret("dhp-secret");
        return properties;
    }

    private final RestClient.Builder builder = RestClient.builder();
    // Mirrors the "oauth2-login" config template registered in
    // application.properties in production - AuthorizationCodeGrantLoginProvider
    // looks it up by that exact name via CircuitBreakerNames.OAUTH2_LOGIN.
    private final DynamicCircuitBreakerLookup circuitBreakerLookup = TestCircuitBreakerLookups.withDefaults(
            CircuitBreakerRegistry.of(Map.of(CircuitBreakerNames.OAUTH2_LOGIN, CircuitBreakerConfig.ofDefaults())));

    @Test
    void reportsAuthorizationCodeAsItsGrantType() {
        AuthorizationCodeGrantLoginProvider provider =
                new AuthorizationCodeGrantLoginProvider("dhp-web", properties(), builder.build(), jsonMapper, circuitBreakerLookup);
        assertThat(provider.grantType()).isEqualTo(LoginGrantType.AUTHORIZATION_CODE);
    }

    @Test
    void exchangesAnAuthorizationCodeForAnAccessToken() {
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        server.expect(once(), requestTo(TOKEN_URL))
                .andExpect(content().contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("grant_type=authorization_code")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("code=auth-code-123")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("code_verifier=verifier-abc")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "redirect_uri=https%3A%2F%2Fapp.example%2Fcallback")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("client_id=emid.conf.web")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("client_secret=dhp-secret")))
                .andRespond(withSuccess(
                        "{\"access_token\":\"dhp-token\",\"token_type\":\"Bearer\",\"expires_in\":300}",
                        MediaType.APPLICATION_JSON
                ));

        AuthorizationCodeGrantLoginProvider provider =
                new AuthorizationCodeGrantLoginProvider("dhp-web", properties(), restClient, jsonMapper, circuitBreakerLookup);

        LoginResult result = provider.login(new LoginRequest(
                "auth-code-123", "verifier-abc", "https://app.example/callback"));

        assertThat(result.accessToken()).isEqualTo("dhp-token");
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.expiresIn()).isEqualTo(300L);
        server.verify();
    }

    @Test
    void rejectsAnUpstream401WithNoBodyAsInvalidCredentials() {
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        server.expect(once(), requestTo(TOKEN_URL))
                .andRespond(withUnauthorizedRequest());

        AuthorizationCodeGrantLoginProvider provider =
                new AuthorizationCodeGrantLoginProvider("dhp-web", properties(), restClient, jsonMapper, circuitBreakerLookup);

        assertThatThrownBy(() -> provider.login(new LoginRequest(
                "expired-or-reused-code", "verifier-abc", "https://app.example/callback")))
                .isInstanceOf(InvalidLoginCredentialsException.class);
        server.verify();
    }

    @Test
    void classifiesAnUpstreamInvalidGrantAsInvalidCredentials() {
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        server.expect(once(), requestTo(TOKEN_URL))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"invalid_grant\",\"error_description\":\"code expired\"}"));

        AuthorizationCodeGrantLoginProvider provider =
                new AuthorizationCodeGrantLoginProvider("dhp-web", properties(), restClient, jsonMapper, circuitBreakerLookup);

        assertThatThrownBy(() -> provider.login(new LoginRequest(
                "expired-code", "verifier-abc", "https://app.example/callback")))
                .isInstanceOf(InvalidLoginCredentialsException.class);
        server.verify();
    }

    @Test
    void classifiesAnUpstreamInvalidClientAsAMisconfiguration() {
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        server.expect(once(), requestTo(TOKEN_URL))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"invalid_client\",\"error_description\":\"Client authentication failed\"}"));

        AuthorizationCodeGrantLoginProvider provider =
                new AuthorizationCodeGrantLoginProvider("dhp-web", properties(), restClient, jsonMapper, circuitBreakerLookup);

        assertThatThrownBy(() -> provider.login(new LoginRequest(
                "code", "verifier-abc", "https://app.example/callback")))
                .isInstanceOf(LoginProviderMisconfiguredException.class);
        server.verify();
    }

    @Test
    void classifiesAnUpstreamInvalidRequestAsAMisconfiguration() {
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        server.expect(once(), requestTo(TOKEN_URL))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"invalid_request\",\"hint\":\"Cannot decrypt the authorization code\"}"));

        AuthorizationCodeGrantLoginProvider provider =
                new AuthorizationCodeGrantLoginProvider("dhp-web", properties(), restClient, jsonMapper, circuitBreakerLookup);

        assertThatThrownBy(() -> provider.login(new LoginRequest(
                "junk-code", "verifier-abc", "https://app.example/callback")))
                .isInstanceOf(LoginProviderMisconfiguredException.class);
        server.verify();
    }

    @Test
    void fallsBackSafelyWhenAGatewayInFrontOfTheProviderReturnsANonOAuthErrorShape() {
        // Observed live: a WAF/gateway in front of the real SSO server sometimes
        // reformats a 4xx as {"success":false,"errors":[...]} instead of the
        // standard OAuth2 {"error": "..."} body - no "error" field to classify
        // on. Must never crash; must still land on the safe default.
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        server.expect(once(), requestTo(TOKEN_URL))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"success\":false,\"errors\":[\"The request is missing a required parameter\"]}"));

        AuthorizationCodeGrantLoginProvider provider =
                new AuthorizationCodeGrantLoginProvider("dhp-web", properties(), restClient, jsonMapper, circuitBreakerLookup);

        assertThatThrownBy(() -> provider.login(new LoginRequest(
                "code", "verifier-abc", "https://app.example/callback")))
                .isInstanceOf(InvalidLoginCredentialsException.class);
        server.verify();
    }

    @Test
    void rejectsAMissingRedirectUriBeforeCallingUpstreamAtAll() {
        AuthorizationCodeGrantLoginProvider provider =
                new AuthorizationCodeGrantLoginProvider("dhp-web", properties(), builder.build(), jsonMapper, circuitBreakerLookup);

        assertThatThrownBy(() -> provider.login(new LoginRequest(
                "code", "verifier", " ")))
                .isInstanceOf(InvalidLoginRequestException.class);
    }

    @Test
    void omitsClientSecretForAPublicPkceClientAndSendsConfiguredExtraParams() {
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        ProviderProperties publicClientProperties = new ProviderProperties();
        publicClientProperties.setEnabled(true);
        publicClientProperties.setTokenUrl(TOKEN_URL);
        publicClientProperties.setClientId("97c3e637-6441-4cbe-95fa-5cbf6fb907a3");
        publicClientProperties.setRequireClientSecret(false);
        publicClientProperties.setExtraParams(java.util.Map.of("claims", "organization"));

        server.expect(once(), requestTo(TOKEN_URL))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "client_id=97c3e637-6441-4cbe-95fa-5cbf6fb907a3")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("claims=organization")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("client_secret"))))
                .andRespond(withSuccess("{\"access_token\":\"sso-web-token\"}", MediaType.APPLICATION_JSON));

        AuthorizationCodeGrantLoginProvider provider =
                new AuthorizationCodeGrantLoginProvider("sso-web", publicClientProperties, restClient, jsonMapper, circuitBreakerLookup);

        LoginResult result = provider.login(new LoginRequest(
                "SSO_CODE_123", "d4e5f6", "http://localhost:3000/auth/callback"));

        assertThat(result.accessToken()).isEqualTo("sso-web-token");
        server.verify();
    }

    @Test
    void requiresAClientSecretByDefaultEvenWhenNoneIsConfigured() {
        ProviderProperties propertiesWithoutSecret = properties();
        propertiesWithoutSecret.setClientSecret(null);

        AuthorizationCodeGrantLoginProvider provider =
                new AuthorizationCodeGrantLoginProvider("dhp-web", propertiesWithoutSecret, builder.build(), jsonMapper, circuitBreakerLookup);

        assertThatThrownBy(() -> provider.login(new LoginRequest(
                "code", "verifier", "https://app.example/callback")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exchangesARefreshTokenForANewAccessToken() {
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        server.expect(once(), requestTo(TOKEN_URL))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("grant_type=refresh_token")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("refresh_token=old-refresh-token")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("client_id=emid.conf.web")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("client_secret=dhp-secret")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("grant_type=authorization_code"))))
                .andRespond(withSuccess(
                        "{\"access_token\":\"new-access-token\",\"refresh_token\":\"new-refresh-token\","
                                + "\"token_type\":\"Bearer\",\"expires_in\":3600}",
                        MediaType.APPLICATION_JSON
                ));

        AuthorizationCodeGrantLoginProvider provider =
                new AuthorizationCodeGrantLoginProvider("dhp-web", properties(), restClient, jsonMapper, circuitBreakerLookup);

        LoginResult result = provider.refresh("old-refresh-token");

        assertThat(result.accessToken()).isEqualTo("new-access-token");
        // DHP rotates the refresh token on every use - the caller must persist
        // the new one, not reuse the one it sent.
        assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
        server.verify();
    }

    @Test
    void classifiesARevokedOrExpiredRefreshTokenAsInvalidCredentials() {
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        server.expect(once(), requestTo(TOKEN_URL))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"invalid_grant\",\"error_description\":\"refresh token revoked\"}"));

        AuthorizationCodeGrantLoginProvider provider =
                new AuthorizationCodeGrantLoginProvider("dhp-web", properties(), restClient, jsonMapper, circuitBreakerLookup);

        assertThatThrownBy(() -> provider.refresh("revoked-refresh-token"))
                .isInstanceOf(InvalidLoginCredentialsException.class);
        server.verify();
    }

    @Test
    void rejectsAMissingRefreshTokenBeforeCallingUpstreamAtAll() {
        AuthorizationCodeGrantLoginProvider provider =
                new AuthorizationCodeGrantLoginProvider("dhp-web", properties(), builder.build(), jsonMapper, circuitBreakerLookup);

        assertThatThrownBy(() -> provider.refresh(" "))
                .isInstanceOf(InvalidLoginRequestException.class);
    }
}
