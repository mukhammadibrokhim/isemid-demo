package uz.uzinfocom.app.platform.devpanel.application;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import uz.uzinfocom.app.platform.auth.application.LoginResult;
import uz.uzinfocom.app.platform.devpanel.domain.DevLoginHistory;
import uz.uzinfocom.app.platform.devpanel.repository.DevLoginHistoryRepository;
import uz.uzinfocom.app.platform.security.claims.ExternalIdentityPayload;
import uz.uzinfocom.app.platform.security.claims.IdentityClaimExtractorRegistry;
import uz.uzinfocom.app.platform.security.jwt.ProviderJwtDecoderFactory;
import uz.uzinfocom.app.platform.security.properties.AuthProvidersProperties;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginHistoryRecorderTest {

    private final DevLoginHistoryRepository devLoginHistoryRepository = mock(DevLoginHistoryRepository.class);
    private final AuthProvidersProperties authProvidersProperties = mock(AuthProvidersProperties.class);
    private final ProviderJwtDecoderFactory providerJwtDecoderFactory = mock(ProviderJwtDecoderFactory.class);
    private final IdentityClaimExtractorRegistry identityClaimExtractorRegistry =
            mock(IdentityClaimExtractorRegistry.class);

    private final LoginHistoryRecorder recorder = new LoginHistoryRecorder(
            devLoginHistoryRepository, authProvidersProperties, providerJwtDecoderFactory, identityClaimExtractorRegistry);

    @Test
    void resolvesTheUsernameFromTheIssuedTokenAndPersistsASuccessRow() throws Exception {
        String accessToken = signedJwtWithIssuer("https://test-sso.ssv.uz");
        AuthProvidersProperties.ProviderProperties providerProperties = new AuthProvidersProperties.ProviderProperties();

        when(authProvidersProperties.findProviderKeyByJwtClaims(eq("https://test-sso.ssv.uz"), any()))
                .thenReturn(Optional.of("sso"));
        when(authProvidersProperties.requireProvider("sso")).thenReturn(providerProperties);

        JwtDecoder decoder = mock(JwtDecoder.class);
        Jwt jwt = mock(Jwt.class);
        when(providerJwtDecoderFactory.create("sso", providerProperties)).thenReturn(decoder);
        when(decoder.decode(accessToken)).thenReturn(jwt);
        when(identityClaimExtractorRegistry.extract("sso", jwt)).thenReturn(
                new ExternalIdentityPayload("sso", "sub-1", null, "alice", null, null, null));

        when(devLoginHistoryRepository.save(any(DevLoginHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LoginResult result = new LoginResult(accessToken, "refresh-token", "Bearer", 900L, "openid");
        recorder.recordSuccess("sso-web", result, "127.0.0.1", "junit-agent");

        ArgumentCaptor<DevLoginHistory> captor = ArgumentCaptor.forClass(DevLoginHistory.class);
        verify(devLoginHistoryRepository).save(captor.capture());

        DevLoginHistory saved = captor.getValue();
        assertThat(saved.getProvider()).isEqualTo("sso-web");
        assertThat(saved.getUsername()).isEqualTo("alice");
        assertThat(saved.getSuccess()).isTrue();
        assertThat(saved.getIp()).isEqualTo("127.0.0.1");
        assertThat(saved.getUserAgent()).isEqualTo("junit-agent");
        assertThat(saved.getOccurredAt()).isNotNull();
    }

    @Test
    void recordsAFailureRowWithNoUsernameWhenTheProviderRejectsTheExchange() {
        when(devLoginHistoryRepository.save(any(DevLoginHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        recorder.recordFailure("dhp-web", new RuntimeException("invalid_grant"), "10.0.0.5", "curl/8.0");

        ArgumentCaptor<DevLoginHistory> captor = ArgumentCaptor.forClass(DevLoginHistory.class);
        verify(devLoginHistoryRepository).save(captor.capture());

        DevLoginHistory saved = captor.getValue();
        assertThat(saved.getProvider()).isEqualTo("dhp-web");
        assertThat(saved.getUsername()).isNull();
        assertThat(saved.getSuccess()).isFalse();
        assertThat(saved.getFailureReason()).isEqualTo("invalid_grant");
    }

    private static String signedJwtWithIssuer(String issuer) throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject("sub-1")
                .issueTime(java.util.Date.from(Instant.now()))
                .build();

        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(new MACSigner("0123456789abcdef0123456789abcdef"));
        return jwt.serialize();
    }
}
