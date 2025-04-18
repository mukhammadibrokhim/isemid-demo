package uz.uzinfocom.isemid.configs.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import uz.uzinfocom.isemid.commons.dtos.ApiErrorResponse;

import java.io.IOException;
import java.util.List;

public class AccessDeniedEntryPoint implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.getOutputStream().println(new ObjectMapper().writeValueAsString(ApiErrorResponse.builder()
                .success(false)
                .statusCode(HttpStatus.FORBIDDEN.value())
                .message("Forbidden")
                .errors(List.of(ApiErrorResponse.ErrorDetail.builder()
                        .field(ex.getClass().getSimpleName())
                        .errorMessage(ex.getMessage()).build())).build()));
    }
}
