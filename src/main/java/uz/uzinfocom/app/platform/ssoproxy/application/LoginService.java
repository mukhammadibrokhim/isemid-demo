package uz.uzinfocom.app.platform.ssoproxy.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.platform.ssoproxy.application.exception.RevokedTokenException;
import uz.uzinfocom.app.platform.ssoproxy.web.dto.LoginRequest;
import uz.uzinfocom.app.platform.ssoproxy.web.dto.LoginResponse;
import uz.uzinfocom.app.platform.ssoproxy.web.dto.LogoutRequest;
import uz.uzinfocom.app.platform.ssoproxy.web.dto.RefreshTokenRequest;
import uz.uzinfocom.app.platform.devpanel.application.LoginHistoryRecorder;
import uz.uzinfocom.app.platform.security.jwt.TokenBlacklistService;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final LoginProviderRegistry loginProviderRegistry;
    private final LoginHistoryRecorder loginHistoryRecorder;
    private final TokenBlacklistService tokenBlacklistService;

    public LoginResponse login(String providerKey, LoginRequest request, String clientIp, String userAgent) {
        LoginProvider provider = loginProviderRegistry.resolve(providerKey);

        LoginResult result;
        try {
            result = provider.login(request);
        } catch (RuntimeException failure) {
            loginHistoryRecorder.recordFailure(providerKey, failure, clientIp, userAgent);
            throw failure;
        }

        loginHistoryRecorder.recordSuccess(providerKey, result, clientIp, userAgent);
        return toResponse(result);
    }

    public LoginResponse refresh(String providerKey, RefreshTokenRequest request) {
        // Checked before the provider is even resolved: without this, a
        // logged-out caller could mint a fresh, non-blacklisted access token
        // from the very refresh token /logout was supposed to invalidate -
        // see TokenBlacklistService and RevokedTokenException.
        if (tokenBlacklistService.isRevoked(request.refreshToken())) {
            throw new RevokedTokenException(providerKey);
        }

        LoginProvider provider = loginProviderRegistry.resolve(providerKey);

        return toResponse(provider.refresh(request.refreshToken()));
    }

    public void logout(String providerKey, LogoutRequest request) {
        LoginProvider provider = loginProviderRegistry.resolve(providerKey);

        // The blacklist is what actually enforces logout on this backend (see
        // TokenBlacklistService); provider.logout(...) below is best-effort
        // upstream cleanup on top of that, not a substitute for it.
        tokenBlacklistService.revoke(request.accessToken());
        tokenBlacklistService.revoke(request.refreshToken());

        provider.logout(request.accessToken(), request.refreshToken());
    }

    private LoginResponse toResponse(LoginResult result) {
        return new LoginResponse(
                result.accessToken(),
                result.refreshToken(),
                result.tokenType(),
                result.expiresIn(),
                result.scope()
        );
    }
}
