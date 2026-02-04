package uz.uzinfocom.isemid.features.security.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.uzinfocom.isemid.features.security.model.TokenRevocation;
import uz.uzinfocom.isemid.features.security.repository.TokenRevocationRepository;
import uz.uzinfocom.isemid.features.security.services.TokenRevocationService;
import uz.uzinfocom.isemid.features.security.utils.TokenHashUtils;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenRevocationServiceImpl implements TokenRevocationService {
    private final TokenRevocationRepository repository;

    @Lazy
    private final JwtDecoder jwtDecoder;

    @Override
    @Transactional
    public void revoke(String rawAccessToken) {
        if (!StringUtils.hasText(rawAccessToken)) return;

        String hash = TokenHashUtils.sha256Hex(rawAccessToken);

        if (repository.existsByTokenHash(hash)) return;

        Jwt jwt = jwtDecoder.decode(rawAccessToken);

        Instant exp = jwt.getExpiresAt();
        if (exp == null) exp = Instant.now().plusSeconds(60);

        String jti = jwt.getId();

        repository.save(
                TokenRevocation.builder()
                        .tokenHash(hash)
                        .jti(jti)
                        .expiresAt(exp)
                        .revokedAt(Instant.now())
                        .build()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRevoked(String rawAccessToken) {
        if (!StringUtils.hasText(rawAccessToken)) return false;
        String hash = TokenHashUtils.sha256Hex(rawAccessToken);
        return repository.existsByTokenHash(hash);
    }
}
