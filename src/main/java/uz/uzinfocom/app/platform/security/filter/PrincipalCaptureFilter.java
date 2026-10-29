package uz.uzinfocom.app.platform.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.uzinfocom.app.platform.observability.RequestPrincipalContext;

import java.io.IOException;

/**
 * Stashes the authenticated principal's name onto the request as an
 * attribute while {@code SecurityContextHolder} is still populated - see
 * {@link RequestPrincipalContext} for why that outlives the context itself.
 * Must run after the chain's authentication filter (bearer/basic) has
 * resolved {@code Authentication}, so it's wired to the same slot as
 * {@code OrganizationContextFilter}: before {@code AuthorizationFilter}.
 */
@Component
public class PrincipalCaptureFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            RequestPrincipalContext.attach(request, authentication.getName());
        }
        filterChain.doFilter(request, response);
    }
}
