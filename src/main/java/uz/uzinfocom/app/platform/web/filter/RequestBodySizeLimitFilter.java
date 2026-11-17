package uz.uzinfocom.app.platform.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.uzinfocom.app.platform.web.response.ErrorResponseWriter;
import uz.uzinfocom.app.shared.exception.ErrorCode;

import java.io.IOException;

/**
 * Rejects a request whose declared {@code Content-Length} exceeds
 * {@code app.web.max-request-body-size} with 413, before Jackson buffers it.
 * Tomcat's {@code max-http-form-post-size} only covers form posts, so without
 * this a JSON body of any size would be read into memory. A chunked body with
 * no Content-Length is left to the reverse proxy's own body limit.
 */
@Component
public class RequestBodySizeLimitFilter extends OncePerRequestFilter {

    private final ErrorResponseWriter errorResponseWriter;
    private final DataSize maxBodySize;

    public RequestBodySizeLimitFilter(
            ErrorResponseWriter errorResponseWriter,
            @Value("${app.web.max-request-body-size:5MB}") DataSize maxBodySize
    ) {
        this.errorResponseWriter = errorResponseWriter;
        this.maxBodySize = maxBodySize;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (request.getContentLengthLong() > maxBodySize.toBytes()) {
            errorResponseWriter.write(request, response, HttpStatus.PAYLOAD_TOO_LARGE,
                    ErrorCode.BAD_REQUEST, "error.request.too-large", maxBodySize.toMegabytes());
            return;
        }
        filterChain.doFilter(request, response);
    }
}
