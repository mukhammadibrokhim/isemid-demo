package uz.uzinfocom.isemid.configs.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.commons.dtos.ApiErrorResponse;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class AuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        try {
            String errorJson = objectMapper.writeValueAsString(
                    ApiErrorResponse.builder()
                            .success(false)
                            .statusCode(HttpStatus.UNAUTHORIZED.value())
                            .message("Неавторизованный доступ.")
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