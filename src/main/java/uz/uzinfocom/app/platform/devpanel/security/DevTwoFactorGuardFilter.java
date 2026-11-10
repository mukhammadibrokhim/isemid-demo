package uz.uzinfocom.app.platform.devpanel.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.uzinfocom.app.platform.web.response.ErrorResponseWriter;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.exception.ErrorCode;

import java.io.IOException;

/**
 * Confines a "limited" token (issued while the account has not enrolled an
 * authenticator yet, so {@link DevAuthenticationToken#isMfaVerified()} is
 * false) to the few endpoints it needs to finish enrolment: who-am-i, change
 * password and the 2FA setup/confirm pair. Everything else answers a distinct
 * {@code DEV_USER_TWO_FACTOR_REQUIRED} code so the panel frontend can route to
 * the enrolment screen. Same shape as {@link DevPasswordChangeGuardFilter}.
 */
@Component
@RequiredArgsConstructor
public class DevTwoFactorGuardFilter extends OncePerRequestFilter {

    private static final RequestMatcher EXEMPT = new OrRequestMatcher(
            PathPatternRequestMatcher.pathPattern(ApiPaths.Dev.ROOT + ApiPaths.Dev.DEV_USER_ME),
            PathPatternRequestMatcher.pathPattern(ApiPaths.Dev.ROOT + ApiPaths.Dev.DEV_USER_ME_PASSWORD),
            PathPatternRequestMatcher.pathPattern(ApiPaths.Dev.ROOT + ApiPaths.Dev.DEV_USER_ME_2FA_SETUP),
            PathPatternRequestMatcher.pathPattern(ApiPaths.Dev.ROOT + ApiPaths.Dev.DEV_USER_ME_2FA_CONFIRM)
    );

    private final ErrorResponseWriter errorResponseWriter;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof DevAuthenticationToken token
                && !token.isMfaVerified()
                && !EXEMPT.matches(request)) {
            errorResponseWriter.write(
                    request,
                    response,
                    HttpStatus.FORBIDDEN,
                    ErrorCode.DEV_USER_TWO_FACTOR_REQUIRED
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}
