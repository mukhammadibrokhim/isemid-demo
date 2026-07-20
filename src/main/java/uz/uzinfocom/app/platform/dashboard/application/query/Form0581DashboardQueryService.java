package uz.uzinfocom.app.platform.dashboard.application.query;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.modules.form0581.application.stats.query.Form0581StatsQueryService;
import uz.uzinfocom.app.modules.form0581.application.stats.query.dto.Form0581DailyCountResponse;
import uz.uzinfocom.app.modules.form0581.web.dto.request.enums.Form0581Direction;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.DashboardScopeResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.DynamicsPointResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.Form0581DashboardResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.GeoBreakdownItemResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.SourceCountResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.TimeSeriesGranularity;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.TimeSeriesResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.TopDiagnosisResponse;
import uz.uzinfocom.app.platform.iam.application.shared.dto.OrganizationGeoProjection;
import uz.uzinfocom.app.platform.iam.application.shared.dto.OrganizationLocalizedName;
import uz.uzinfocom.app.platform.iam.application.shared.dto.OrganizationNameProjection;
import uz.uzinfocom.app.platform.iam.application.shared.service.OrganizationNameResolver;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.platform.reference.domain.District;
import uz.uzinfocom.app.platform.reference.domain.Region;
import uz.uzinfocom.app.platform.reference.repository.DistrictRepository;
import uz.uzinfocom.app.platform.reference.repository.RegionRepository;
import uz.uzinfocom.app.platform.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * The on-demand, form-058-1-only counterpart to {@code HomeDashboardQueryService}
 * — see {@link Form058DashboardQueryService} for why this is a separate
 * endpoint rather than a field nested inside the combined home dashboard.
 */
@Service
public class Form0581DashboardQueryService {

    private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Tashkent");
    private static final int TOP_DIAGNOSIS_RESULT_LIMIT = 5;

    private final Form0581StatsQueryService form0581StatsQueryService;
    private final OrganizationScopeResolver organizationScopeResolver;
    private final OrganizationRepository organizationRepository;
    private final DistrictRepository districtRepository;
    private final RegionRepository regionRepository;
    private final ReferenceLookupService referenceLookupService;
    private final OrganizationNameResolver organizationNameResolver;
    private final Executor applicationTaskExecutor;

    public Form0581DashboardQueryService(
            Form0581StatsQueryService form0581StatsQueryService,
            OrganizationScopeResolver organizationScopeResolver,
            OrganizationRepository organizationRepository,
            DistrictRepository districtRepository,
            RegionRepository regionRepository,
            ReferenceLookupService referenceLookupService,
            OrganizationNameResolver organizationNameResolver,
            @Qualifier("applicationTaskExecutor") Executor applicationTaskExecutor
    ) {
        this.form0581StatsQueryService = form0581StatsQueryService;
        this.organizationScopeResolver = organizationScopeResolver;
        this.organizationRepository = organizationRepository;
        this.districtRepository = districtRepository;
        this.regionRepository = regionRepository;
        this.referenceLookupService = referenceLookupService;
        this.organizationNameResolver = organizationNameResolver;
        this.applicationTaskExecutor = applicationTaskExecutor;
    }

    public Form0581DashboardResponse getDashboard() {
        Instant generatedAt = Instant.now();
        Organization currentOrganization = CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
        ResolvedOrganizationScope scope = organizationScopeResolver.resolve(currentOrganization);
        LocaleContext locale = LocaleContextHolder.getLocaleContext();

        CompletableFuture<DashboardScopeResponse> scopeFuture =
                supplyOrgScoped(currentOrganization, locale, () -> buildScope(scope));
        CompletableFuture<SummaryBundle> summaryFuture =
                supplyOrgScoped(currentOrganization, locale, this::buildSummary);
        CompletableFuture<TimeSeriesResponse> dynamicsFuture =
                supplyOrgScoped(currentOrganization, locale, this::buildDynamics);
        CompletableFuture<List<TopDiagnosisResponse>> topDiagnosesFuture =
                supplyOrgScoped(currentOrganization, locale, this::buildTopDiagnoses);
        CompletableFuture<List<SourceCountResponse>> sourceBreakdownFuture =
                supplyOrgScoped(currentOrganization, locale, this::buildSourceBreakdown);
        CompletableFuture<List<GeoBreakdownItemResponse>> geoBreakdownFuture =
                supplyOrgScoped(currentOrganization, locale, () -> buildGeoBreakdown(scope));

        CompletableFuture.allOf(
                scopeFuture, summaryFuture, dynamicsFuture, topDiagnosesFuture, sourceBreakdownFuture, geoBreakdownFuture
        ).join();

        SummaryBundle summary = summaryFuture.join();

        return new Form0581DashboardResponse(
                generatedAt,
                scopeFuture.join(),
                summary.total(),
                summary.active(),
                summary.today(),
                summary.asOfDate(),
                dynamicsFuture.join(),
                topDiagnosesFuture.join(),
                sourceBreakdownFuture.join(),
                geoBreakdownFuture.join()
        );
    }

    private record DateWindow(LocalDate from, LocalDate to) {
    }

    private record SummaryBundle(long total, long active, long today, LocalDate asOfDate) {
    }

    private DateWindow dynamicsWindow() {
        LocalDate to = LocalDate.now(APPLICATION_ZONE);
        LocalDate from = LocalDate.of(to.getYear(), 1, 1);
        return new DateWindow(from, to);
    }

    /** Mirrors {@code HomeDashboardQueryService#supplyOrgScoped} - see there for why both propagations are needed. */
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

    private SummaryBundle buildSummary() {
        LocalDate today = LocalDate.now(APPLICATION_ZONE);
        long total = form0581StatsQueryService.countTotal(Form0581Direction.INCOMING);
        long active = form0581StatsQueryService.countActive(Form0581Direction.INCOMING);
        long todayCount = form0581StatsQueryService.countByDay(Form0581Direction.INCOMING, today, today).stream()
                .mapToLong(Form0581DailyCountResponse::count)
                .sum();

        return new SummaryBundle(total, active, todayCount, today);
    }

    private TimeSeriesResponse buildDynamics() {
        DateWindow window = dynamicsWindow();
        List<DynamicsPointResponse> points = form0581StatsQueryService.countByMonth(Form0581Direction.INCOMING, window.from(), window.to())
                .stream()
                .map(item -> new DynamicsPointResponse(item.date(), item.count()))
                .toList();

        return new TimeSeriesResponse(window.from(), window.to(), TimeSeriesGranularity.MONTH, points);
    }

    private List<TopDiagnosisResponse> buildTopDiagnoses() {
        return form0581StatsQueryService.topMkb10(Form0581Direction.INCOMING, TOP_DIAGNOSIS_RESULT_LIMIT).stream()
                .map(item -> new TopDiagnosisResponse(item.mkb10Code(), item.count()))
                .toList();
    }

    private List<SourceCountResponse> buildSourceBreakdown() {
        return form0581StatsQueryService.countBySource(Form0581Direction.INCOMING).stream()
                .sorted((a, b) -> Long.compare(b.count(), a.count()))
                .map(item -> new SourceCountResponse(item.source(), item.count()))
                .toList();
    }

    private List<GeoBreakdownItemResponse> buildGeoBreakdown(ResolvedOrganizationScope scope) {
        return switch (scope.mode()) {
            case REGION -> buildDistrictBreakdown(scope.regionCode());
            case ALL -> buildRegionBreakdown();
            case DISTRICT -> buildOrganizationBreakdown(scope.districtCode());
            case ORGANIZATION -> List.of();
        };
    }

    /**
     * DISTRICT-scope callers have no sub-district geography left to break
     * down further, so this lists the district's own organizations instead
     * — the unit a district-scoped caller actually cares about next.
     */
    private List<GeoBreakdownItemResponse> buildOrganizationBreakdown(String districtCode) {
        List<OrganizationNameProjection> organizations = organizationRepository.findActiveByDistrictCode(districtCode);
        List<Long> organizationIds = organizations.stream().map(OrganizationNameProjection::id).toList();

        Map<Long, Long> countsByOrgId = new HashMap<>();
        form0581StatsQueryService.countByReceiverOrganizationWithinIds(organizationIds)
                .forEach(item -> countsByOrgId.merge(item.organizationId(), item.count(), Long::sum));

        return organizations.stream()
                .map(org -> new GeoBreakdownItemResponse(
                        String.valueOf(org.id()),
                        organizationNameResolver.resolve(new OrganizationLocalizedName(
                                org.name(), org.nameUz(), org.nameUzCyril(), org.nameRu(), org.nameKaa()
                        )),
                        countsByOrgId.getOrDefault(org.id(), 0L)
                ))
                .toList();
    }

    private List<GeoBreakdownItemResponse> buildDistrictBreakdown(String regionCode) {
        List<District> districts = districtRepository.findAllByParentCodeAndDeletedFalseOrderByNameUzAsc(regionCode);
        List<OrganizationGeoProjection> organizations =
                organizationRepository.findActiveIdAndDistrictCodeByRegionCode(regionCode);

        Map<String, Long> countsByDistrict = countsByGeoCode(organizations);

        return districts.stream()
                .map(district -> new GeoBreakdownItemResponse(
                        district.getCode(),
                        referenceLookupService.getDistrictName(district.getCode()),
                        countsByDistrict.getOrDefault(district.getCode(), 0L)
                ))
                .toList();
    }

    private List<GeoBreakdownItemResponse> buildRegionBreakdown() {
        List<Region> regions = regionRepository.findAllByDeletedFalseOrderByNameUzAsc();
        List<OrganizationGeoProjection> organizations = organizationRepository.findActiveIdAndRegionCode();

        Map<String, Long> countsByRegion = countsByGeoCode(organizations);

        return regions.stream()
                .map(region -> new GeoBreakdownItemResponse(
                        region.getCode(),
                        referenceLookupService.getRegionName(region.getCode()),
                        countsByRegion.getOrDefault(region.getCode(), 0L)
                ))
                .toList();
    }

    /**
     * Small, bounded in-memory merge (at most a few hundred organizations
     * for a region, at most ~1000 countrywide) — see {@link
     * Form058DashboardQueryService} for why this is fine
     * here even though the combined home dashboard avoids the equivalent
     * Java-side merge for its own (much larger, per-row) case.
     */
    private Map<String, Long> countsByGeoCode(List<OrganizationGeoProjection> organizations) {
        Map<Long, String> geoCodeByOrgId = new HashMap<>();
        for (OrganizationGeoProjection organization : organizations) {
            geoCodeByOrgId.put(organization.id(), organization.code());
        }

        List<Long> organizationIds = organizations.stream().map(OrganizationGeoProjection::id).toList();

        Map<String, Long> countsByGeoCode = new HashMap<>();
        form0581StatsQueryService.countByReceiverOrganizationWithinIds(organizationIds).forEach(item -> {
            String geoCode = geoCodeByOrgId.get(item.organizationId());
            if (geoCode != null) {
                countsByGeoCode.merge(geoCode, item.count(), Long::sum);
            }
        });

        return countsByGeoCode;
    }
}
