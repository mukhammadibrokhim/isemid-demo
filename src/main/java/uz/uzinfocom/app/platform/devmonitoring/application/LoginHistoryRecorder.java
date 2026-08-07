package uz.uzinfocom.app.platform.devmonitoring.application;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.auth.application.LoginResult;
import uz.uzinfocom.app.platform.devmonitoring.domain.DevLoginHistory;
import uz.uzinfocom.app.platform.devmonitoring.repository.DevLoginHistoryRepository;
import uz.uzinfocom.app.platform.observability.TraceContext;
import uz.uzinfocom.app.platform.security.claims.ExternalIdentityPayload;
import uz.uzinfocom.app.platform.security.claims.IdentityClaimExtractorRegistry;
import uz.uzinfocom.app.platform.security.jwt.ProviderJwtDecoderFactory;
import uz.uzinfocom.app.platform.security.properties.AuthProvidersProperties;

import java.time.Instant;
import java.util.UUID;

/**
 * Records login attempts (success and failure) against {@code /v1/auth/login/{provider}}
 * into {@code dev_login_history}, for the developer monitoring panel.
 *
 * <p>{@code LoginProviderRegistry} (platform.auth) uses login-side provider keys
 * ({@code sso-web}/{@code dhp-web}), which are deliberately kept distinct from the
 * inbound/resource-server provider keys ({@code sso}/{@code dhp}) that
 * {@link IdentityClaimExtractorRegistry} is keyed by. To learn "who" logged in, the
 * freshly-issued access token is decoded using the same issuer/marker-claim resolution
 * {@code ProviderAuthenticationManagerRegistry} already trusts on the inbound side
 * ({@link AuthProvidersProperties#findProviderKeyByJwtClaims}), bridging the two
 * vocabularies rather than guessing a string transform between them.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginHistoryRecorder {

    private final DevLoginHistoryRepository devLoginHistoryRepository;
    private final AuthProvidersProperties authProvidersProperties;
    private final ProviderJwtDecoderFactory providerJwtDecoderFactory;
    private final IdentityClaimExtractorRegistry identityClaimExtractorRegistry;

    @Async("applicationTaskExecutor")
    @Transactional
    public void recordSuccess(String loginProviderKey, LoginResult result, String ip, String userAgent) {
        String traceId = TraceContext.currentTraceId();
        try {
            ExternalIdentityPayload identity = resolveIdentity(result.accessToken());
            save(loginProviderKey, identity.username(), identity.practitionerUuid(), true, null, ip, userAgent, traceId);
        } catch (RuntimeException resolutionFailure) {
            log.warn("event=dev_login_history_username_resolution_failure traceId={} provider={} failureType={}",
                    traceId, loginProviderKey, resolutionFailure.getClass().getName());
            save(loginProviderKey, null, null, true, null, ip, userAgent, traceId);
        }
    }

    @Async("applicationTaskExecutor")
    @Transactional
    public void recordFailure(String loginProviderKey, Throwable failure, String ip, String userAgent) {
        String traceId = TraceContext.currentTraceId();
        String reason = failure.getMessage() != null ? failure.getMessage() : failure.getClass().getSimpleName();
        save(loginProviderKey, null, null, false, reason, ip, userAgent, traceId);
    }

    private ExternalIdentityPayload resolveIdentity(String accessToken) {
        JWTClaimsSet claims;
        try {
            claims = SignedJWT.parse(accessToken).getJWTClaimsSet();
        } catch (java.text.ParseException parseException) {
            throw new IllegalStateException("Issued access token is not a parseable JWT", parseException);
        }

        String inboundProviderKey = authProvidersProperties
                .findProviderKeyByJwtClaims(claims.getIssuer(), claims.getClaims())
                .orElseThrow(() -> new IllegalStateException("Unable to resolve inbound provider for issued token"));

        JwtDecoder decoder = providerJwtDecoderFactory.create(
                inboundProviderKey,
                authProvidersProperties.requireProvider(inboundProviderKey)
        );
        Jwt jwt = decoder.decode(accessToken);
        return identityClaimExtractorRegistry.extract(inboundProviderKey, jwt);
    }

    private void save(
            String provider,
            String username,
            UUID userId,
            boolean success,
            String failureReason,
            String ip,
            String userAgent,
            String traceId
    ) {
        try {
            DevLoginHistory entry = DevLoginHistory.builder()
                    .provider(provider)
                    .username(username)
                    .userId(userId)
                    .success(success)
                    .failureReason(failureReason)
                    .ip(ip)
                    .userAgent(userAgent)
                    .traceId(traceId)
                    .occurredAt(Instant.now())
                    .build();

            devLoginHistoryRepository.save(entry);
        } catch (RuntimeException persistenceFailure) {
            log.warn("event=dev_login_history_write_failure traceId={} provider={} failureType={}",
                    traceId, provider, persistenceFailure.getClass().getName());
        }
    }
}
