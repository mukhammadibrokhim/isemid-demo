package uz.uzinfocom.isemid.exceptioins;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class AccessDeniedEntryPoint implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.getOutputStream().println(new ObjectMapper().writeValueAsString(ErrorResponse.builder()
                .success(false)
                .statusCode(HttpStatus.FORBIDDEN.value())
                .message("Forbidden")
                .errors(List.of(ErrorDetail.builder()
                        .field(accessDeniedException.getClass().getSimpleName())
                        .errorMessage(accessDeniedException.getMessage()).build())).build()));
    }
}
