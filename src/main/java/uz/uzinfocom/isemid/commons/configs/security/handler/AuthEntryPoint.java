package uz.uzinfocom.isemid.commons.configs.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.commons.dtos.ApiErrorResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MessageUtil messageUtil;
    private static final String DEFAULT_AUTH_MESSAGE = "Full authentication is required to access this resource";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        String originalMessage = authException.getMessage();
        String translatedMessage = originalMessage;

        if (DEFAULT_AUTH_MESSAGE.equals(originalMessage)) {
            try {
                translatedMessage = messageUtil.get("auth.required");

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        try {
            String errorJson = objectMapper.writeValueAsString(
                    ApiErrorResponse.builder()
                            .success(false)
                            .statusCode(HttpStatus.UNAUTHORIZED.value())
                            .message(translatedMessage)
                            .errors(List.of(ApiErrorResponse.ErrorDetail.builder()
                                    .field(authException.getClass().getSimpleName())
                                    .errorMessage(authException.getMessage()).build()))
                            .build()
            );

            response.getWriter().write(errorJson);
            response.getWriter().flush();
        } catch (IOException e) {
            log.debug("Client disconnected before response was written", e);
        }
    }
}