package uz.uzinfocom.app.platform.auth.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.platform.auth.web.dto.LoginRequest;
import uz.uzinfocom.app.platform.auth.web.dto.LoginResponse;
import uz.uzinfocom.app.platform.auth.web.dto.RefreshTokenRequest;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final LoginProviderRegistry loginProviderRegistry;

    public LoginResponse login(String providerKey, LoginRequest request) {
        LoginProvider provider = loginProviderRegistry.resolve(providerKey);

        return toResponse(provider.login(request));
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
