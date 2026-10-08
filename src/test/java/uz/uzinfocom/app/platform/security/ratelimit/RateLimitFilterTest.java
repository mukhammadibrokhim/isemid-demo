package uz.uzinfocom.app.platform.security.ratelimit;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import uz.uzinfocom.app.platform.web.response.ErrorResponseWriter;
import uz.uzinfocom.app.shared.exception.ErrorCode;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RateLimitFilterTest {

    private final ErrorResponseWriter errorResponseWriter = mock(ErrorResponseWriter.class);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsAuthRequestsFromOneIpOnceTheBudgetIsSpent() throws Exception {
        RateLimitFilter filter = filter(2);
        FilterChain chain = mock(FilterChain.class);

        MockHttpServletResponse last = null;
        for (int i = 0; i < 3; i++) {
            last = new MockHttpServletResponse();
            filter.doFilter(request("/v1/auth/login", "203.0.113.7"), last, chain);
        }

        verify(chain, times(2)).doFilter(any(), any());
        verify(errorResponseWriter).write(any(), any(), eq(HttpStatus.TOO_MANY_REQUESTS), eq(ErrorCode.TOO_MANY_REQUESTS));
        assertThat(last.getHeader("Retry-After")).isEqualTo("60");
    }

    @Test
    void countsEachIpSeparately() throws Exception {
        RateLimitFilter filter = filter(1);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request("/v1/auth/login", "203.0.113.7"), new MockHttpServletResponse(), chain);
        filter.doFilter(request("/v1/auth/login", "203.0.113.8"), new MockHttpServletResponse(), chain);

        verify(chain, times(2)).doFilter(any(), any());
        verify(errorResponseWriter, never()).write(any(), any(), any(HttpStatus.class), any(ErrorCode.class));
    }

    @Test
    void chargesIdentityLookupsToTheAuthenticatedUserNotTheIp() throws Exception {
        RateLimitFilter filter = filter(1);
        FilterChain chain = mock(FilterChain.class);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alice", null, List.of()));
        filter.doFilter(request("/v1/citizen", "10.0.0.1"), new MockHttpServletResponse(), chain);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("bob", null, List.of()));
        filter.doFilter(request("/v1/citizen", "10.0.0.1"), new MockHttpServletResponse(), chain);
        filter.doFilter(request("/v1/citizen", "10.0.0.1"), new MockHttpServletResponse(), chain);

        verify(chain, times(2)).doFilter(any(), any());
        verify(errorResponseWriter, times(1))
                .write(any(), any(), eq(HttpStatus.TOO_MANY_REQUESTS), eq(ErrorCode.TOO_MANY_REQUESTS));
    }

    @Test
    void leavesOtherPathsUnlimited() throws Exception {
        RateLimitFilter filter = filter(1);
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 5; i++) {
            filter.doFilter(request("/v1/form-058", "203.0.113.7"), new MockHttpServletResponse(), chain);
        }

        verify(chain, times(5)).doFilter(any(), any());
    }

    private RateLimitFilter filter(int limit) {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setAuth(new RateLimitProperties.Rule(limit, Duration.ofMinutes(1)));
        properties.setIntegrationToken(new RateLimitProperties.Rule(limit, Duration.ofMinutes(1)));
        properties.setIdentityLookup(new RateLimitProperties.Rule(limit, Duration.ofMinutes(1)));
        return new RateLimitFilter(properties, errorResponseWriter);
    }

    private static MockHttpServletRequest request(String path, String remoteAddr) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setRemoteAddr(remoteAddr);
        return request;
    }
}
