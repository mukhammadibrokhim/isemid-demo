package uz.uzinfocom.app.platform.devpanel.security;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.platform.devpanel.domain.DevUser;
import uz.uzinfocom.app.platform.devpanel.domain.DevUserRole;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class DevTokenServiceTest {

    private static final String BASE_SECRET = "dGVzdC1iYXNlLXNlY3JldC1mb3ItZGV2LXBhbmVsLXRlc3Rz";

    private final DevTokenService tokenService = service(Duration.ofHours(8), BASE_SECRET);

    @Test
    void issuedTokenParsesBackToItsClaims() {
        DevUserPrincipal principal = principal(7L, "hash-a", true);

        DevTokenService.IssuedToken issued = tokenService.issue(principal, true);
        DevTokenService.TokenClaims claims = tokenService.parse(issued.token()).orElseThrow();

        assertThat(claims.devUserId()).isEqualTo(7L);
        assertThat(claims.mfaVerified()).isTrue();
        assertThat(claims.credentialVersion()).isEqualTo(principal.getCredentialVersion());
        assertThat(tokenService.parse(tokenService.issue(principal, false).token()).orElseThrow().mfaVerified())
                .isFalse();
    }

    @Test
    void rejectsATamperedPayloadOrSignature() {
        String token = tokenService.issue(principal(7L, "hash-a", true), false).token();
        String other = tokenService.issue(principal(8L, "hash-a", true), true).token();

        // payload of one token + signature of another
        String spliced = other.substring(0, other.indexOf('.')) + token.substring(token.indexOf('.'));

        assertThat(tokenService.parse(spliced)).isEmpty();
        assertThat(tokenService.parse(token + "x")).isEmpty();
        assertThat(tokenService.parse("not-a-token")).isEmpty();
        assertThat(tokenService.parse("a.b.c")).isEmpty();
        assertThat(tokenService.parse("")).isEmpty();
        assertThat(tokenService.parse(null)).isEmpty();
    }

    @Test
    void rejectsAnExpiredToken() {
        DevTokenService shortLived = service(Duration.ofSeconds(-5), BASE_SECRET);

        assertThat(shortLived.parse(shortLived.issue(principal(7L, "hash-a", true), true).token())).isEmpty();
    }

    @Test
    void rejectsATokenSignedWithAnotherKey() {
        DevTokenService foreign = service(Duration.ofHours(8), "b3RoZXItYmFzZS1zZWNyZXQtZm9yLWRldi1wYW5lbC10ZXN0cw==");

        assertThat(tokenService.parse(foreign.issue(principal(7L, "hash-a", true), true).token())).isEmpty();
    }

    @Test
    void credentialVersionChangesWithPasswordEnabledAndTwoFactorState() {
        String base = principal(7L, "hash-a", false).getCredentialVersion();

        assertThat(principal(7L, "hash-b", false).getCredentialVersion()).isNotEqualTo(base);
        assertThat(principal(7L, "hash-a", true).getCredentialVersion()).isNotEqualTo(base);
        assertThat(principal(7L, "hash-a", false).getCredentialVersion()).isEqualTo(base);
    }

    @Test
    void ticketIsSingleUse() {
        String ticket = tokenService.issueTicket(7L);

        assertThat(tokenService.redeemTicket(ticket)).contains(7L);
        assertThat(tokenService.redeemTicket(ticket)).isEmpty();
        assertThat(tokenService.redeemTicket("unknown")).isEmpty();
        assertThat(tokenService.redeemTicket(null)).isEmpty();
    }

    private static DevTokenService service(Duration ttl, String baseSecret) {
        DevAuthProperties properties = new DevAuthProperties(null, null, ttl, true, null);
        return new DevTokenService(new DevAuthKeys(properties, baseSecret), properties);
    }

    private static DevUserPrincipal principal(Long id, String passwordHash, boolean totpEnabled) {
        DevUser devUser = DevUser.builder()
                .username("dev-admin")
                .passwordHash(passwordHash)
                .enabled(true)
                .role(DevUserRole.ADMIN)
                .totpEnabled(totpEnabled)
                .build();
        devUser.setId(id);
        return new DevUserPrincipal(devUser);
    }
}
