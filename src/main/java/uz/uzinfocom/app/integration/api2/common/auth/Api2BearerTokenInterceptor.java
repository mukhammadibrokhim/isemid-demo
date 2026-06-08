package uz.uzinfocom.app.integration.api2.common.auth;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class Api2BearerTokenInterceptor implements ClientHttpRequestInterceptor {

    private final CurrentBearerTokenProvider tokenProvider;

    @Override
    public @NonNull ClientHttpResponse intercept(
            HttpRequest request,
            @Nullable byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        request.getHeaders().setBearerAuth(tokenProvider.getRequiredToken());
        return execution.execute(request, body);
    }
}
