package uz.uzinfocom.app.platform.security.claims;

import org.springframework.security.oauth2.jwt.Jwt;

public interface IdentityClaimExtractor {

    String providerKey();

    ExternalIdentityPayload extract(Jwt jwt);
}