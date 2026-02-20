package uz.uzinfocom.isemid.features.security.integrations;

public interface SsoLogoutClient {
    void logout(String rawAccessToken);
}
