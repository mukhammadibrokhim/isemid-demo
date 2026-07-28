package uz.uzinfocom.app.platform.auth.application;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.platform.auth.web.dto.LoginRequest;
import uz.uzinfocom.app.platform.auth.web.dto.LoginResponse;
import uz.uzinfocom.app.platform.auth.web.dto.RefreshTokenRequest;
import uz.uzinfocom.app.platform.devmonitoring.application.LoginHistoryRecorder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginServiceTest {

    private final LoginProviderRegistry loginProviderRegistry = mock(LoginProviderRegistry.class);
    private final LoginHistoryRecorder loginHistoryRecorder = mock(LoginHistoryRecorder.class);
    private final LoginService loginService = new LoginService(loginProviderRegistry, loginHistoryRecorder);

    @Test
    void resolvesTheProviderAndMapsItsResultToTheResponse() {
        LoginProvider provider = mock(LoginProvider.class);
        LoginRequest request = new LoginRequest("code-1", "verifier-1", "https://app.example/callback");
        when(loginProviderRegistry.resolve("dhp-web")).thenReturn(provider);
        when(provider.login(request)).thenReturn(
                new LoginResult("access-token", "refresh-token", "Bearer", 300L, "openid"));

        LoginResponse response = loginService.login("dhp-web", request, "127.0.0.1", "junit-agent");

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(300L);
        assertThat(response.scope()).isEqualTo("openid");
    }

    @Test
    void resolvesTheProviderAndMapsItsRefreshResultToTheResponse() {
        LoginProvider provider = mock(LoginProvider.class);
        when(loginProviderRegistry.resolve("dhp-web")).thenReturn(provider);
        when(provider.refresh("old-refresh-token")).thenReturn(
                new LoginResult("new-access-token", "new-refresh-token", "Bearer", 3600L, "openid"));

        LoginResponse response = loginService.refresh("dhp-web", new RefreshTokenRequest("old-refresh-token"));

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600L);
    }
}
