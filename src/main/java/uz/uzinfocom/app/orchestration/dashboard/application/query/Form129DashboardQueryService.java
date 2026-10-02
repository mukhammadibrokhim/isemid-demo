package uz.uzinfocom.app.orchestration.dashboard.application.query;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.modules.form129.application.stats.query.Form129StatsQueryService;
import uz.uzinfocom.app.modules.form129.application.stats.query.dto.Form129DailyCountResponse;
import uz.uzinfocom.app.modules.form129.application.stats.query.dto.Form129DiseaseTypeCountResponse;
import uz.uzinfocom.app.modules.form129.application.stats.query.dto.Form129StatusCountResponse;
import uz.uzinfocom.app.orchestration.dashboard.application.query.dto.DashboardScopeResponse;
import uz.uzinfocom.app.orchestration.dashboard.application.query.dto.DynamicsPointResponse;
import uz.uzinfocom.app.orchestration.dashboard.application.query.dto.Form129DashboardResponse;
import uz.uzinfocom.app.orchestration.dashboard.application.query.dto.GeoBreakdownItemResponse;
import uz.uzinfocom.app.orchestration.dashboard.application.query.dto.SourceCountResponse;
import uz.uzinfocom.app.orchestration.dashboard.application.query.dto.TimeSeriesGranularity;
import uz.uzinfocom.app.orchestration.dashboard.application.query.dto.TimeSeriesResponse;
import uz.uzinfocom.app.modules.iam.application.shared.dto.OrganizationGeoProjection;
import uz.uzinfocom.app.modules.iam.application.shared.dto.OrganizationLocalizedName;
import uz.uzinfocom.app.modules.iam.application.shared.dto.OrganizationNameProjection;
import uz.uzinfocom.app.modules.iam.application.shared.service.OrganizationNameResolver;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.modules.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.modules.reference.domain.District;
import uz.uzinfocom.app.modules.reference.domain.Region;
import uz.uzinfocom.app.modules.reference.repository.DistrictRepository;
import uz.uzinfocom.app.modules.reference.repository.RegionRepository;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.orchestration.scope.ResolvedOrganizationScope;
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
 * The on-demand, form-129-only counterpart to {@code HomeDashboardQueryService}
 * — see {@link Form058DashboardQueryService} for why this is a separate
 * endpoint rather than a field nested inside the combined home dashboard.
 * Unlike {@link Form0581DashboardQueryService}, there are no card/act
 * futures: Form129 is a pure registry with no card-linking (see
 * {@code Form129}'s class doc).
 */
@Service
public class Form129DashboardQueryService {

    private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Tashkent");

    private final Form129StatsQueryService form129StatsQueryService;
    private final OrganizationScopeResolver organizationScopeResolver;
    private final OrganizationRepository organizationRepository;
    private final DistrictRepository districtRepository;
    private final RegionRepository regionRepository;
    private final ReferenceLookupService referenceLookupService;
    private final OrganizationNameResolver organizationNameResolver;
    private final Executor applicationTaskExecutor;

    public Form129DashboardQueryService(
            Form129StatsQueryService form129StatsQueryService,
            OrganizationScopeResolver organizationScopeResolver,
            OrganizationRepository organizationRepository,
            DistrictRepository districtRepository,
            RegionRepository regionRepository,
            ReferenceLookupService referenceLookupService,
            OrganizationNameResolver organizationNameResolver,
            @Qualifier("applicationTaskExecutor") Executor applicationTaskExecutor
    ) {
        this.form129StatsQueryService = form129StatsQueryService;
        this.organizationScopeResolver = organizationScopeResolver;
        this.organizationRepository = organizationRepository;
        this.districtRepository = districtRepository;
        this.regionRepository = regionRepository;
        this.referenceLookupService = referenceLookupService;
        this.organizationNameResolver = organizationNameResolver;
        this.applicationTaskExecutor = applicationTaskExecutor;
    }

    public Form129DashboardResponse getDashboard() {
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
        CompletableFuture<List<Form129DiseaseTypeCountResponse>> byDiseaseTypeFuture =
                supplyOrgScoped(currentOrganization, locale, form129StatsQueryService::countByDiseaseType);
        CompletableFuture<List<SourceCountResponse>> sourceBreakdownFuture =
                supplyOrgScoped(currentOrganization, locale, this::buildSourceBreakdown);
        CompletableFuture<List<GeoBreakdownItemResponse>> geoBreakdownFuture =
                supplyOrgScoped(currentOrganization, locale, () -> buildGeoBreakdown(scope));
        CompletableFuture<List<Form129StatusCountResponse>> byStatusFuture =
                supplyOrgScoped(currentOrganization, locale, form129StatsQueryService::countByStatus);

        CompletableFuture.allOf(
                scopeFuture, summaryFuture, dynamicsFuture, byDiseaseTypeFuture, sourceBreakdownFuture,
                geoBreakdownFuture, byStatusFuture
        ).join();

        SummaryBundle summary = summaryFuture.join();

        return new Form129DashboardResponse(
                generatedAt,
                scopeFuture.join(),
                summary.total(),
                summary.active(),
                summary.today(),
                summary.asOfDate(),
                dynamicsFuture.join(),
                byDiseaseTypeFuture.join(),
                sourceBreakdownFuture.join(),
                geoBreakdownFuture.join(),
                byStatusFuture.join()
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
        long total = form129StatsQueryService.countTotal();
        long active = form129StatsQueryService.countActive();
        long todayCount = form129StatsQueryService.countByDay(today, today).stream()
                .mapToLong(Form129DailyCountResponse::count)
                .sum();

        return new SummaryBundle(total, active, todayCount, today);
    }

    private TimeSeriesResponse buildDynamics() {
        DateWindow window = dynamicsWindow();
        List<DynamicsPointResponse> points = form129StatsQueryService
                .countByMonthWithOutcomes(window.from(), window.to())
                .stream()
                .map(item -> new DynamicsPointResponse(item.periodStart(), item.total(), item.canceledCount(), item.acceptedCount()))
                .toList();

        return new TimeSeriesResponse(window.from(), window.to(), TimeSeriesGranularity.MONTH, points);
    }

    private List<SourceCountResponse> buildSourceBreakdown() {
        return form129StatsQueryService.countBySource().stream()
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
        form129StatsQueryService.countByReceiverOrganizationWithinIds(organizationIds)
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
     * Small, bounded in-memory merge — see {@link Form058DashboardQueryService}
     * for why this is fine here even though the combined home dashboard
     * avoids the equivalent Java-side merge for its own (much larger,
     * per-row) case.
     */
    private Map<String, Long> countsByGeoCode(List<OrganizationGeoProjection> organizations) {
        Map<Long, String> geoCodeByOrgId = new HashMap<>();
        for (OrganizationGeoProjection organization : organizations) {
            geoCodeByOrgId.put(organization.id(), organization.code());
        }

        List<Long> organizationIds = organizations.stream().map(OrganizationGeoProjection::id).toList();

        Map<String, Long> countsByGeoCode = new HashMap<>();
        form129StatsQueryService.countByReceiverOrganizationWithinIds(organizationIds).forEach(item -> {
            String geoCode = geoCodeByOrgId.get(item.organizationId());
            if (geoCode != null) {
                countsByGeoCode.merge(geoCode, item.count(), Long::sum);
            }
        });

        return countsByGeoCode;
    }
}
