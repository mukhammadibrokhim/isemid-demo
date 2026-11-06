package uz.uzinfocom.app.platform.security.jwt;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.cache.SecurityCacheNames;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Blocks an access/refresh token immediately after logout, independent of
 * the token's own {@code exp} claim. Both SSO and DHP bearer tokens are
 * validated locally by signature only (see
 * {@code ProviderAuthenticationManagerRegistry}/{@code PublicKeyEndpointJwtDecoder}),
 * so calling a provider's own logout/revoke endpoint has no effect on
 * whether this backend still accepts an already-issued token - this
 * blacklist is what actually makes logout take effect here.
 *
 * <p>Tokens are stored by SHA-256 hash, never in cleartext - same reasoning
 * as never logging a raw bearer token.</p>
 */
@Component
public class TokenBlacklistService {

    private final Cache blacklistCache;

    public TokenBlacklistService(@Qualifier("securityCacheManager") CacheManager securityCacheManager) {
        this.blacklistCache = securityCacheManager.getCache(SecurityCacheNames.REVOKED_TOKEN_BLACKLIST);

        if (this.blacklistCache == null) {
            throw new IllegalStateException(
                    "Cache is not configured: " + SecurityCacheNames.REVOKED_TOKEN_BLACKLIST);
        }
    }

    public void revoke(String token) {
        if (!StringUtils.hasText(token)) {
            return;
        }

        blacklistCache.put(hash(token), Boolean.TRUE);
    }

    public boolean isRevoked(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }

        return blacklistCache.get(hash(token)) != null;
    }

    private static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
