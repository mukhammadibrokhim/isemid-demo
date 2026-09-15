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
 * Blocks every {@code /v1/dev/**} request from a {@link DevUserPrincipal}
 * whose {@link DevUserPrincipal#isMustChangePassword()} is still true -
 * except its own {@code GET .../dev-users/me} (so a client can tell who's
 * logged in and that a change is required) and
 * {@code PATCH .../dev-users/me/password} (the one way out of this block).
 *
 * <p>Wired into {@code DevPanelSecurityConfig} right after
 * {@code AuthorizationFilter}, so it only ever runs for a request that has
 * already passed authentication - an anonymous/rejected request never
 * reaches here.
 */
@Component
@RequiredArgsConstructor
public class DevPasswordChangeGuardFilter extends OncePerRequestFilter {

    private static final RequestMatcher EXEMPT = new OrRequestMatcher(
            PathPatternRequestMatcher.pathPattern(ApiPaths.Dev.ROOT + ApiPaths.Dev.DEV_USER_ME),
            PathPatternRequestMatcher.pathPattern(ApiPaths.Dev.ROOT + ApiPaths.Dev.DEV_USER_ME_PASSWORD)
    );

    private final ErrorResponseWriter errorResponseWriter;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.getPrincipal() instanceof DevUserPrincipal principal
                && principal.isMustChangePassword()
                && !EXEMPT.matches(request)) {
            errorResponseWriter.write(
                    request,
                    response,
                    HttpStatus.FORBIDDEN,
                    ErrorCode.DEV_USER_PASSWORD_CHANGE_REQUIRED
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}
