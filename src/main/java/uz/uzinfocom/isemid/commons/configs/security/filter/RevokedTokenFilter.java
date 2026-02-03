package uz.uzinfocom.isemid.commons.configs.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.uzinfocom.isemid.features.security.services.TokenRevocationService;
import uz.uzinfocom.isemid.features.security.utils.BearerTokenExtractor;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RevokedTokenFilter extends OncePerRequestFilter {
    private final TokenRevocationService tokenRevocationService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = BearerTokenExtractor.extract(request);

        if (token != null && tokenRevocationService.isRevoked(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"token_revoked\"}");
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
