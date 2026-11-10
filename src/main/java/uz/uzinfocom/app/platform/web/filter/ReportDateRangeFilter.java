package uz.uzinfocom.app.platform.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.uzinfocom.app.platform.web.response.ErrorResponseWriter;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.exception.ErrorCode;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Caps the {@code from}/{@code to} (and statistics' {@code fromA}/{@code toA},
 * {@code fromB}/{@code toB}) span on the aggregate endpoints - reports, form
 * stats and the dashboard. Those queries scan every notification inside the
 * range, so an unbounded {@code from=0001-01-01} turned one cheap GET into a
 * full-table aggregate holding a pooled connection. Checked once here, in front
 * of ~55 endpoints, instead of in every report service. Unparseable values pass
 * through untouched - the controllers' own binding/validation still owns those.
 */
@Component
public class ReportDateRangeFilter extends OncePerRequestFilter {

    private static final List<String> GUARDED_ROOTS = List.of(
            ApiPaths.Report.ROOT,
            ApiPaths.Dashboard.ROOT,
            ApiPaths.Form058Stats.ROOT,
            ApiPaths.Form058AdminStats.ROOT,
            ApiPaths.Form0581Stats.ROOT,
            ApiPaths.Form0581AdminStats.ROOT
    );

    private static final List<String[]> RANGE_PARAMS = List.of(
            new String[]{"from", "to"},
            new String[]{"fromA", "toA"},
            new String[]{"fromB", "toB"}
    );

    private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Tashkent");

    private final ErrorResponseWriter errorResponseWriter;
    private final int maxYears;

    public ReportDateRangeFilter(
            ErrorResponseWriter errorResponseWriter,
            @Value("${app.web.report-date-range.max-years:10}") int maxYears
    ) {
        this.errorResponseWriter = errorResponseWriter;
        this.maxYears = maxYears;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return GUARDED_ROOTS.stream().noneMatch(root -> path.equals(root) || path.startsWith(root + "/"));
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        for (String[] pair : RANGE_PARAMS) {
            LocalDate from = parse(request.getParameter(pair[0]));
            LocalDate to = parse(request.getParameter(pair[1]));
            if (from != null && to == null) {
                // services default a missing end to "today", so from=0001-01-01 alone is just as wide
                to = LocalDate.now(APPLICATION_ZONE);
            }
            if (from != null && to != null && from.plusYears(maxYears).isBefore(to)) {
                errorResponseWriter.write(request, response, HttpStatus.BAD_REQUEST,
                        ErrorCode.VALIDATION_FAILED, "error.report.date-range.too-wide", maxYears);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private static LocalDate parse(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }
}
