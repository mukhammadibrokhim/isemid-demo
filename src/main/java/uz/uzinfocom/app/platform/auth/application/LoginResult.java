package uz.uzinfocom.app.platform.auth.application;

public record LoginResult(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        String scope
) {
}
