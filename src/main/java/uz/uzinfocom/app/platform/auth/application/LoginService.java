package uz.uzinfocom.app.platform.auth.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.platform.auth.web.dto.LoginRequest;
import uz.uzinfocom.app.platform.auth.web.dto.LoginResponse;
import uz.uzinfocom.app.platform.auth.web.dto.RefreshTokenRequest;
import uz.uzinfocom.app.platform.devmonitoring.application.LoginHistoryRecorder;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final LoginProviderRegistry loginProviderRegistry;
    private final LoginHistoryRecorder loginHistoryRecorder;

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
        LoginProvider provider = loginProviderRegistry.resolve(providerKey);

        return toResponse(provider.refresh(request.refreshToken()));
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
