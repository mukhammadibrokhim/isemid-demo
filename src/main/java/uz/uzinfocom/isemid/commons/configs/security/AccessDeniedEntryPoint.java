package uz.uzinfocom.isemid.commons.configs.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.commons.dtos.ApiErrorResponse;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class AccessDeniedEntryPoint implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.FORBIDDEN.value());

        try {
            String errorJson = objectMapper.writeValueAsString(
                    ApiErrorResponse.builder()
                            .success(false)
                            .statusCode(HttpStatus.FORBIDDEN.value())
                            .message("Доступ запрещен.")
                            .errors(List.of(ApiErrorResponse.ErrorDetail.builder()
                                    .field(ex.getClass().getSimpleName())
                                    .errorMessage(ex.getMessage()).build()))
                            .build()
            );

            response.getWriter().write(errorJson);
            response.getWriter().flush();
        } catch (IOException e) {
            log.debug("Client disconnected before response was written", e);
        }
    }
}
