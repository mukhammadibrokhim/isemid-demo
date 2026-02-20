package uz.uzinfocom.isemid.features.security.services;

public interface TokenRevocationService {
    void revoke(String rawAccessToken);

    boolean isRevoked(String rawAccessToken);
}
