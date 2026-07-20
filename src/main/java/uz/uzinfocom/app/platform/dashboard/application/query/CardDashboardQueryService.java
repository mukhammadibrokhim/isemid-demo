package uz.uzinfocom.app.platform.dashboard.application.query;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.modules.card.application.query.CardStatsQueryService;
import uz.uzinfocom.app.modules.card.application.query.dto.CardStatusCountResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.CardTypeCountResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.CardDashboardResponse;
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
 * The {@code card} module's own dashboard — {@code /v1/dashboard/home/card}.
 * Card has no sibling form to combine with (it belongs to form058 only), so
 * this is a single-table view via {@link CardStatsQueryService} the same
 * way {@code Form058DashboardQueryService} is for form058 alone.
 */
@Service
public class CardDashboardQueryService {

    private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Tashkent");

    private final CardStatsQueryService cardStatsQueryService;
    private final OrganizationScopeResolver organizationScopeResolver;
    private final ReferenceLookupService referenceLookupService;
    private final Executor applicationTaskExecutor;

    public CardDashboardQueryService(
            CardStatsQueryService cardStatsQueryService,
            OrganizationScopeResolver organizationScopeResolver,
            ReferenceLookupService referenceLookupService,
            @Qualifier("applicationTaskExecutor") Executor applicationTaskExecutor
    ) {
        this.cardStatsQueryService = cardStatsQueryService;
        this.organizationScopeResolver = organizationScopeResolver;
        this.referenceLookupService = referenceLookupService;
        this.applicationTaskExecutor = applicationTaskExecutor;
    }

    public CardDashboardResponse getDashboard() {
        Instant generatedAt = Instant.now();
        Organization currentOrganization = CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
        ResolvedOrganizationScope scope = organizationScopeResolver.resolve(currentOrganization);
        LocaleContext locale = LocaleContextHolder.getLocaleContext();

        CompletableFuture<DashboardScopeResponse> scopeFuture =
                supplyOrgScoped(currentOrganization, locale, () -> buildScope(scope));
        CompletableFuture<List<CardStatusCountResponse>> byStatusFuture =
                supplyOrgScoped(currentOrganization, locale, cardStatsQueryService::countByStatus);
        CompletableFuture<List<CardTypeCountResponse>> byTypeFuture =
                supplyOrgScoped(currentOrganization, locale, cardStatsQueryService::countByType);
        CompletableFuture<Long> totalFuture =
                supplyOrgScoped(currentOrganization, locale, cardStatsQueryService::countTotal);
        CompletableFuture<Long> activeFuture =
                supplyOrgScoped(currentOrganization, locale, cardStatsQueryService::countActive);
        CompletableFuture<TimeSeriesResponse> dynamicsFuture =
                supplyOrgScoped(currentOrganization, locale, this::buildDynamics);

        CompletableFuture.allOf(scopeFuture, byStatusFuture, byTypeFuture, totalFuture, activeFuture, dynamicsFuture).join();

        return new CardDashboardResponse(
                generatedAt,
                scopeFuture.join(),
                totalFuture.join(),
                activeFuture.join(),
                byStatusFuture.join(),
                byTypeFuture.join(),
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

        List<DynamicsPointResponse> points = cardStatsQueryService.countByMonth(from, to).stream()
                .map(item -> new DynamicsPointResponse(item.date(), item.count()))
                .toList();

        return new TimeSeriesResponse(from, to, TimeSeriesGranularity.MONTH, points);
    }
}
