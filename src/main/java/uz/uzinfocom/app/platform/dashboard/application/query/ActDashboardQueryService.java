package uz.uzinfocom.app.platform.dashboard.application.query;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.modules.act.application.query.ActStatsQueryService;
import uz.uzinfocom.app.modules.act.application.query.dto.ActStatusCountResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.ActDashboardResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.DashboardScopeResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.DynamicsPointResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.TimeSeriesGranularity;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.TimeSeriesResponse;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.platform.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * The {@code act} module's own dashboard — {@code /v1/dashboard/home/act}.
 * See {@link CardDashboardQueryService} for why this is single-table and
 * separate from the general home dashboard.
 */
@Service
public class ActDashboardQueryService {

    private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Tashkent");

    private final ActStatsQueryService actStatsQueryService;
    private final OrganizationScopeResolver organizationScopeResolver;
    private final ReferenceLookupService referenceLookupService;
    private final Executor applicationTaskExecutor;

    public ActDashboardQueryService(
            ActStatsQueryService actStatsQueryService,
            OrganizationScopeResolver organizationScopeResolver,
            ReferenceLookupService referenceLookupService,
            @Qualifier("applicationTaskExecutor") Executor applicationTaskExecutor
    ) {
        this.actStatsQueryService = actStatsQueryService;
        this.organizationScopeResolver = organizationScopeResolver;
        this.referenceLookupService = referenceLookupService;
        this.applicationTaskExecutor = applicationTaskExecutor;
    }

    public ActDashboardResponse getDashboard() {
        Instant generatedAt = Instant.now();
        Organization currentOrganization = CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
        ResolvedOrganizationScope scope = organizationScopeResolver.resolve(currentOrganization);
        LocaleContext locale = LocaleContextHolder.getLocaleContext();

        CompletableFuture<DashboardScopeResponse> scopeFuture =
                supplyOrgScoped(currentOrganization, locale, () -> buildScope(scope));
        CompletableFuture<List<ActStatusCountResponse>> byStatusFuture =
                supplyOrgScoped(currentOrganization, locale, actStatsQueryService::countByStatus);
        CompletableFuture<Long> totalFuture =
                supplyOrgScoped(currentOrganization, locale, actStatsQueryService::countTotal);
        CompletableFuture<TimeSeriesResponse> dynamicsFuture =
                supplyOrgScoped(currentOrganization, locale, this::buildDynamics);

        CompletableFuture.allOf(scopeFuture, byStatusFuture, totalFuture, dynamicsFuture).join();

        return new ActDashboardResponse(
                generatedAt,
                scopeFuture.join(),
                totalFuture.join(),
                byStatusFuture.join(),
                dynamicsFuture.join()
        );
    }

    private <T> CompletableFuture<T> supplyOrgScoped(Organization organization, LocaleContext locale, Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            CurrentOrganizationContext.set(organization);
            LocaleContextHolder.setLocaleContext(locale);
            try {
                return supplier.get();
            } finally {
                CurrentOrganizationContext.clear();
                LocaleContextHolder.resetLocaleContext();
            }
        }, applicationTaskExecutor);
    }

    private DashboardScopeResponse buildScope(ResolvedOrganizationScope scope) {
        String regionName = scope.regionCode() != null ? referenceLookupService.getRegionName(scope.regionCode()) : null;
        String districtName = scope.districtCode() != null
                ? referenceLookupService.getDistrictName(scope.districtCode())
                : null;

        return new DashboardScopeResponse(scope.mode(), scope.regionCode(), regionName, scope.districtCode(), districtName);
    }

    private TimeSeriesResponse buildDynamics() {
        LocalDate to = LocalDate.now(APPLICATION_ZONE);
        LocalDate from = LocalDate.of(to.getYear(), 1, 1);

        List<DynamicsPointResponse> points = actStatsQueryService.countByMonth(from, to).stream()
                .map(item -> new DynamicsPointResponse(item.date(), item.count()))
                .toList();

        return new TimeSeriesResponse(from, to, TimeSeriesGranularity.MONTH, points);
    }
}
