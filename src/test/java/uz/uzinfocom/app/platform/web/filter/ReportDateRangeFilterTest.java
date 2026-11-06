package uz.uzinfocom.app.platform.web.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import uz.uzinfocom.app.platform.web.response.ErrorResponseWriter;
import uz.uzinfocom.app.shared.exception.ErrorCode;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ReportDateRangeFilterTest {

    private final ErrorResponseWriter errorResponseWriter = mock(ErrorResponseWriter.class);
    private final ReportDateRangeFilter filter = new ReportDateRangeFilter(errorResponseWriter, 10);
    private final FilterChain chain = mock(FilterChain.class);

    @Test
    void passesARangeWithinTheLimit() throws Exception {
        filter.doFilter(request("/v1/reports/form-1/root", "from", "2020-01-01", "to", "2026-01-01"),
                new MockHttpServletResponse(), chain);

        verify(chain).doFilter(any(), any());
    }

    @Test
    void rejectsARangeWiderThanTheLimit() throws Exception {
        filter.doFilter(request("/v1/reports/form-1/root", "from", "2000-01-01", "to", "2026-01-01"),
                new MockHttpServletResponse(), chain);

        verify(chain, never()).doFilter(any(), any());
        verify(errorResponseWriter).write(any(), any(), eq(HttpStatus.BAD_REQUEST), eq(ErrorCode.VALIDATION_FAILED),
                eq("error.report.date-range.too-wide"), eq(10));
    }

    @Test
    void treatsAMissingEndAsToday() throws Exception {
        filter.doFilter(request("/v1/reports/statistics/root", "fromA", "0001-01-01"),
                new MockHttpServletResponse(), chain);

        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void ignoresPathsOutsideTheAggregateEndpoints() throws Exception {
        filter.doFilter(request("/v1/form-058", "from", "2000-01-01", "to", "2026-01-01"),
                new MockHttpServletResponse(), chain);

        verify(chain).doFilter(any(), any());
    }

    private static MockHttpServletRequest request(String path, String... params) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        for (int i = 0; i < params.length; i += 2) {
            request.setParameter(params[i], params[i + 1]);
        }
        return request;
    }
}
