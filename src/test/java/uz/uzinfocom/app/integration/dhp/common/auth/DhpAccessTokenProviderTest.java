package uz.uzinfocom.app.integration.dhp.common.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import uz.uzinfocom.app.integration.dhp.common.DhpTestProperties;
import uz.uzinfocom.app.integration.dhp.common.exception.DhpIntegrationException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.ExpectedCount.never;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class DhpAccessTokenProviderTest {

    private static final String TOKEN_BODY = "{\"access_token\":\"%s\",\"expires_in\":3599,\"scope\":\"\",\"token_type\":\"bearer\"}";

    private MockRestServiceServer server;
    private RestClient restClient;
    private MutableClock clock;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        restClient = builder.build();
        clock = new MutableClock(Instant.parse("2026-09-21T10:00:00Z"));
    }

    @Test
    void postsClientCredentialsInTheFormBody() {
        server.expect(once(), requestTo(DhpTestProperties.TOKEN_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("grant_type=client_credentials"),
                        org.hamcrest.Matchers.containsString("client_id=client-id"),
                        org.hamcrest.Matchers.containsString("client_secret=client-secret"))))
                .andRespond(withSuccess(TOKEN_BODY.formatted("tok-1"), MediaType.APPLICATION_JSON));

        assertThat(provider().getAccessToken()).isEqualTo("tok-1");
        server.verify();
    }

    @Test
    void reusesTheCachedTokenUntilItNearsExpiry() {
        server.expect(once(), requestTo(DhpTestProperties.TOKEN_URL))
                .andRespond(withSuccess(TOKEN_BODY.formatted("tok-1"), MediaType.APPLICATION_JSON));
        DhpAccessTokenProvider provider = provider();

        assertThat(provider.getAccessToken()).isEqualTo("tok-1");
        clock.advance(Duration.ofSeconds(3000)); // still inside 3599s - 60s skew
        assertThat(provider.getAccessToken()).isEqualTo("tok-1");

        server.verify();
    }

    @Test
    void refreshesOnceTheSkewWindowIsReached() {
        server.expect(once(), requestTo(DhpTestProperties.TOKEN_URL))
                .andRespond(withSuccess(TOKEN_BODY.formatted("tok-1"), MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo(DhpTestProperties.TOKEN_URL))
                .andRespond(withSuccess(TOKEN_BODY.formatted("tok-2"), MediaType.APPLICATION_JSON));
        DhpAccessTokenProvider provider = provider();

        assertThat(provider.getAccessToken()).isEqualTo("tok-1");
        clock.advance(Duration.ofSeconds(3540)); // 3599 - 60 = 3539 usable
        assertThat(provider.getAccessToken()).isEqualTo("tok-2");

        server.verify();
    }

    @Test
    void invalidateForcesAFreshTokenButOnlyForTheRejectedOne() {
        server.expect(once(), requestTo(DhpTestProperties.TOKEN_URL))
                .andRespond(withSuccess(TOKEN_BODY.formatted("tok-1"), MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo(DhpTestProperties.TOKEN_URL))
                .andRespond(withSuccess(TOKEN_BODY.formatted("tok-2"), MediaType.APPLICATION_JSON));
        DhpAccessTokenProvider provider = provider();

        assertThat(provider.getAccessToken()).isEqualTo("tok-1");
        provider.invalidate("some-other-token");
        assertThat(provider.getAccessToken()).isEqualTo("tok-1");
        provider.invalidate("tok-1");
        assertThat(provider.getAccessToken()).isEqualTo("tok-2");

        server.verify();
    }

    @Test
    void missingCredentialsFailWithoutCallingDhp() {
        server.expect(never(), requestTo(DhpTestProperties.TOKEN_URL));
        DhpAccessTokenProvider provider =
                new DhpAccessTokenProvider(restClient, DhpTestProperties.with("", null, null), clock);

        assertThatThrownBy(provider::getAccessToken)
                .isInstanceOf(DhpIntegrationException.class)
                .hasMessage("dhp.error.not_configured");
        server.verify();
    }

    @Test
    void rejectedCredentialsAreReportedAsNotConfigured() {
        server.expect(once(), requestTo(DhpTestProperties.TOKEN_URL))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"invalid_client\"}"));

        assertThatThrownBy(() -> provider().getAccessToken())
                .isInstanceOf(DhpIntegrationException.class)
                .hasMessage("dhp.error.not_configured");
    }

    @Test
    void upstreamServerErrorIsAnUpstreamFailure() {
        server.expect(once(), requestTo(DhpTestProperties.TOKEN_URL))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThatThrownBy(() -> provider().getAccessToken())
                .isInstanceOf(DhpIntegrationException.class)
                .hasMessage("dhp.error.unavailable");
    }

    @Test
    void responseWithoutAnAccessTokenIsMalformed() {
        server.expect(once(), requestTo(DhpTestProperties.TOKEN_URL))
                .andRespond(withSuccess("{\"expires_in\":3599}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> provider().getAccessToken())
                .isInstanceOf(DhpIntegrationException.class)
                .hasMessage("dhp.error.malformed_response");
    }

    private DhpAccessTokenProvider provider() {
        return new DhpAccessTokenProvider(restClient, DhpTestProperties.configured(), clock);
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant start) {
            this.now = start;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
