package uz.uzinfocom.app.platform.devpanel.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

/**
 * Stateless HMAC-signed access tokens for the dev panel
 * ({@code base64url(uid:exp:mfa:ver).base64url(hmac)}), plus single-use
 * short-lived "tickets" for SSE streams (a browser {@code EventSource}
 * cannot send an {@code Authorization} header).
 *
 * <p>Nothing is stored server-side for tokens: validity is signature + expiry,
 * and revocation rides on {@code ver} (see {@link DevUserPrincipal#getCredentialVersion()}),
 * which the filter compares against the live account on every request.
 * Tickets are in-memory, per instance - fine because the stream request that
 * redeems one reaches the same instance that issued it only when sticky; if
 * the deployment is multi-instance without stickiness the client simply asks
 * for another ticket (they are cheap and expire in {@value #TICKET_TTL_SECONDS}s).
 */
@Service
public class DevTokenService {

    public static final long TICKET_TTL_SECONDS = 30;

    /** How long the user has to type the authenticator code after the password step. */
    static final Duration CHALLENGE_TTL = Duration.ofMinutes(5);
    private static final String CHALLENGE_KIND = "C";

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final SecretKeySpec signingKey;
    private final Duration tokenTtl;
    private final Cache<String, Long> tickets = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(TICKET_TTL_SECONDS))
            .maximumSize(10_000)
            .build();

    public DevTokenService(DevAuthKeys keys, DevAuthProperties properties) {
        this.signingKey = new SecretKeySpec(keys.tokenKey(), "HmacSHA256");
        this.tokenTtl = properties.tokenTtl();
    }

    public IssuedToken issue(DevUserPrincipal principal, boolean mfaVerified) {
        Instant expiresAt = Instant.now().plus(tokenTtl);
        String payload = principal.getDevUserId() + ":" + expiresAt.getEpochSecond()
                + ":" + (mfaVerified ? "1" : "0") + ":" + principal.getCredentialVersion();
        String body = ENCODER.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return new IssuedToken(body + "." + ENCODER.encodeToString(sign(body)), expiresAt);
    }

    /**
     * Step-1 result of a two-step login: proves the password was right, grants
     * NO access (the Bearer filter never accepts it, see {@link #parse}) and is
     * only redeemable at {@code POST /v1/dev/auth/verify-otp}. Short-lived, and
     * bound to the credential version so a password change voids it.
     */
    public IssuedToken issueChallenge(DevUserPrincipal principal) {
        Instant expiresAt = Instant.now().plus(CHALLENGE_TTL);
        String payload = principal.getDevUserId() + ":" + expiresAt.getEpochSecond()
                + ":" + CHALLENGE_KIND + ":" + principal.getCredentialVersion();
        String body = ENCODER.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return new IssuedToken(body + "." + ENCODER.encodeToString(sign(body)), expiresAt);
    }

    /** Access token only - a challenge token is rejected here. */
    public Optional<TokenClaims> parse(String token) {
        return parseKind(token, false);
    }

    /** Challenge token only - an access token is rejected here. */
    public Optional<TokenClaims> parseChallenge(String token) {
        return parseKind(token, true);
    }

    private Optional<TokenClaims> parseKind(String token, boolean challenge) {
        if (token == null) {
            return Optional.empty();
        }
        int dot = token.indexOf('.');
        if (dot <= 0 || dot == token.length() - 1) {
            return Optional.empty();
        }
        String body = token.substring(0, dot);
        try {
            byte[] presented = DECODER.decode(token.substring(dot + 1));
            if (!MessageDigest.isEqual(presented, sign(body))) {
                return Optional.empty();
            }
            String[] parts = new String(DECODER.decode(body), StandardCharsets.UTF_8).split(":", 4);
            if (parts.length != 4) {
                return Optional.empty();
            }
            Instant expiresAt = Instant.ofEpochSecond(Long.parseLong(parts[1]));
            if (!expiresAt.isAfter(Instant.now())) {
                return Optional.empty();
            }
            if (CHALLENGE_KIND.equals(parts[2]) != challenge) {
                return Optional.empty();
            }
            return Optional.of(new TokenClaims(Long.parseLong(parts[0]), "1".equals(parts[2]), parts[3]));
        } catch (IllegalArgumentException malformed) {
            return Optional.empty();
        }
    }

    public String issueTicket(Long devUserId) {
        byte[] bytes = new byte[24];
        SECURE_RANDOM.nextBytes(bytes);
        String ticket = ENCODER.encodeToString(bytes);
        tickets.put(ticket, devUserId);
        return ticket;
    }

    /** Single use: the ticket is gone after the first call, valid or not. */
    public Optional<Long> redeemTicket(String ticket) {
        if (ticket == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(tickets.asMap().remove(ticket));
    }

    private byte[] sign(String body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            return mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException failure) {
            throw new IllegalStateException("HmacSHA256 is not available in this JVM", failure);
        }
    }

    public Duration tokenTtl() {
        return tokenTtl;
    }

    public record IssuedToken(String token, Instant expiresAt) {
    }

    public record TokenClaims(Long devUserId, boolean mfaVerified, String credentialVersion) {
    }
}
