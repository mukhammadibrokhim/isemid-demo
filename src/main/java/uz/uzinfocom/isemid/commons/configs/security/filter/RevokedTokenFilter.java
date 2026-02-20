package uz.uzinfocom.isemid.commons.configs.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.uzinfocom.isemid.commons.dtos.ApiErrorResponse;
import uz.uzinfocom.isemid.features.security.services.TokenRevocationService;
import uz.uzinfocom.isemid.features.security.utils.BearerTokenExtractor;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RevokedTokenFilter extends OncePerRequestFilter {

    private final TokenRevocationService tokenRevocationService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = BearerTokenExtractor.extract(request);

        if (token != null && tokenRevocationService.isRevoked(token)) {

            ApiErrorResponse body = ApiErrorResponse.builder()
                    .success(false)
                    .statusCode(HttpServletResponse.SC_UNAUTHORIZED)
                    .message("token_revoked")
                    .errors(List.of(
                            ApiErrorResponse.ErrorDetail.builder()
                                    .field("token")
                                    .errorMessage("JWT token has been revoked")
                                    .build()
                    ))
                    .build();

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(body));
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path != null && (
                path.startsWith("/api/v1/auth/login")
                        || path.startsWith("/api/v1/auth/refresh")
        );
    }
}
