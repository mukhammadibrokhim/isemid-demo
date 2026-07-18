package uz.uzinfocom.app.platform.dashboard.application.query;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.modules.act.application.query.ActStatsQueryService;
import uz.uzinfocom.app.modules.act.application.query.dto.ActStatusCountResponse;
import uz.uzinfocom.app.modules.card.application.query.CardStatsQueryService;
import uz.uzinfocom.app.modules.card.application.query.dto.CardStatusCountResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.CardTypeCountResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.TimeSeriesGranularity;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.TimeSeriesResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.ActStatsResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.CardStatsResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.CaseSummaryResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.DashboardScopeResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.DynamicsPointResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.GeoBreakdownItemResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.HomeDashboardResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.SourceCountResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.TopDiagnosisResponse;
import uz.uzinfocom.app.platform.dashboard.infrastructure.persistence.CaseStatsAggregateRepository;
import uz.uzinfocom.app.platform.dashboard.infrastructure.persistence.dto.CaseSummaryAggregate;
import uz.uzinfocom.app.platform.dashboard.infrastructure.persistence.dto.GeoCodeCount;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.platform.scope.OrganizationScopeMode;
import uz.uzinfocom.app.platform.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.scope.jpa.OrganizationScopeOrganizationIdResolver;
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
 * Assembles the "everyone" home dashboard — the org-scoped counterpart to the
 * unscoped admin dashboards ({@code Form058AdminStatsController} etc.). Case
 * metrics combine Form058 and Form0581 (both are epidemiological case
 * reports); Card/Act metrics are their own totals, since they represent a
 * different kind of workload (investigation, not case reporting).
 * <p>
 * Every case statistic (summary, monthly dynamics, top diagnoses, source
 * breakdown, geo breakdown) is a single native-SQL {@code UNION ALL} query
 * against form058+form058_1, executed by {@link CaseStatsAggregateRepository}
 * — the combining happens once, inside Postgres, via one {@code GROUP BY}.
 * This class never fetches a per-form-type result set and merges it itself
 * ({@code HashMap}/{@code TreeMap} accumulation over two already-grouped
 * lists): at real data volumes that Java-side step was never actually a
 * memory problem, but it was needless duplicate work — two round trips and
 * two partial aggregations where one now suffices — so it is gone.
 * <p>
 * This service depends only on each module's public query-service façade
 * ({@code CardStatsQueryService}, {@code ActStatsQueryService}) for the
 * metrics that are NOT cross-table (Card/Act have no sibling form to merge
 * with), plus platform-level shared infrastructure ({@code
 * OrganizationScopeResolver}, {@code OrganizationRepository}, {@code
 * ReferenceLookupService}, {@code CaseStatsAggregateRepository}) — never
 * another module's repository directly.
 * <p>
 * {@code getHome()} fans out its 9 independent aggregations onto the
 * application's shared task executor instead of running them one after
 * another — on this dataset the sequential version was dominated entirely by
 * round-trip latency, since none of the 9 branches depend on each other's
 * results. Each branch is its own {@code @Transactional} call on its own
 * thread/connection (Spring starts a fresh transaction per thread since
 * transactions are ThreadLocal-bound), so this is safe as long as {@link
 * CurrentOrganizationContext} — also ThreadLocal, and read internally by
 * {@code CardStatsQueryService}/{@code ActStatsQueryService} to resolve the
 * caller's scope — is copied onto each worker thread first; {@link
 * #supplyOrgScoped} does exactly that. The case-statistics branches take the
 * already-resolved {@link ResolvedOrganizationScope} directly instead, since
 * {@link CaseStatsAggregateRepository} does not read the ThreadLocal itself.
 */
@Service
public class HomeDashboardQueryService {

    private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Tashkent");
    private static final int TOP_DIAGNOSIS_RESULT_LIMIT = 5;

    private final CaseStatsAggregateRepository caseStatsAggregateRepository;
    private final CardStatsQueryService cardStatsQueryService;
    private final ActStatsQueryService actStatsQueryService;
    private final OrganizationScopeResolver organizationScopeResolver;
    private final OrganizationScopeOrganizationIdResolver organizationScopeOrganizationIdResolver;
    private final OrganizationRepository organizationRepository;
    private final ReferenceLookupService referenceLookupService;
    private final Executor applicationTaskExecutor;

    /*
     * Explicit constructor (not @RequiredArgsConstructor) solely so
     * @Qualifier can land on the executor constructor parameter - Lombok
     * does not copy arbitrary field annotations onto generated constructor
     * parameters without a project-wide lombok.config entry, and this is
     * the only field in the class that needs one.
     */
    public HomeDashboardQueryService(
            CaseStatsAggregateRepository caseStatsAggregateRepository,
            CardStatsQueryService cardStatsQueryService,
            ActStatsQueryService actStatsQueryService,
            OrganizationScopeResolver organizationScopeResolver,
            OrganizationScopeOrganizationIdResolver organizationScopeOrganizationIdResolver,
            OrganizationRepository organizationRepository,
            ReferenceLookupService referenceLookupService,
            @Qualifier("applicationTaskExecutor") Executor applicationTaskExecutor
    ) {
        this.caseStatsAggregateRepository = caseStatsAggregateRepository;
        this.cardStatsQueryService = cardStatsQueryService;
        this.actStatsQueryService = actStatsQueryService;
        this.organizationScopeResolver = organizationScopeResolver;
        this.organizationScopeOrganizationIdResolver = organizationScopeOrganizationIdResolver;
        this.organizationRepository = organizationRepository;
        this.referenceLookupService = referenceLookupService;
        this.applicationTaskExecutor = applicationTaskExecutor;
    }

    public HomeDashboardResponse getHome() {
        Instant generatedAt = Instant.now();
        Organization currentOrganization = CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
        ResolvedOrganizationScope scope = organizationScopeResolver.resolve(currentOrganization);

        CompletableFuture<DashboardScopeResponse> scopeFuture =
                supplyOrgScoped(currentOrganization, () -> buildScope(scope));
        CompletableFuture<CaseSummaryResponse> caseSummaryFuture =
                supplyOrgScoped(currentOrganization, () -> buildCaseSummary(scope));
        CompletableFuture<TimeSeriesResponse> dynamicsFuture =
                supplyOrgScoped(currentOrganization, () -> buildDynamics(scope));
        CompletableFuture<List<TopDiagnosisResponse>> topDiagnosesFuture =
                supplyOrgScoped(currentOrganization, () -> buildTopDiagnoses(scope));
        CompletableFuture<List<SourceCountResponse>> sourceBreakdownFuture =
                supplyOrgScoped(currentOrganization, () -> buildSourceBreakdown(scope));
        CompletableFuture<List<GeoBreakdownItemResponse>> geoBreakdownFuture =
                supplyOrgScoped(currentOrganization, () -> buildGeoBreakdown(scope));
        CompletableFuture<Long> institutionsFuture =
                supplyOrgScoped(currentOrganization, () -> countMedicalInstitutions(scope));
        CompletableFuture<CardStatsResponse> cardStatsFuture =
                supplyOrgScoped(currentOrganization, this::buildCardStats);
        CompletableFuture<ActStatsResponse> actStatsFuture =
                supplyOrgScoped(currentOrganization, this::buildActStats);

        CompletableFuture.allOf(
                scopeFuture, caseSummaryFuture, dynamicsFuture, topDiagnosesFuture, sourceBreakdownFuture,
                geoBreakdownFuture, institutionsFuture, cardStatsFuture, actStatsFuture
        ).join();

        return new HomeDashboardResponse(
                generatedAt,
                scopeFuture.join(),
                caseSummaryFuture.join(),
                dynamicsFuture.join(),
                topDiagnosesFuture.join(),
                sourceBreakdownFuture.join(),
                geoBreakdownFuture.join(),
                institutionsFuture.join(),
                cardStatsFuture.join(),
                actStatsFuture.join()
        );
    }

    /**
     * The shared window every trend in this dashboard uses — case, card,
     * and act dynamics all cover exactly the same period, so a client can
     * overlay them without checking each one's range separately: from
     * January 1st of the current calendar year through today. As the year
     * progresses the window simply grows month by month (today always
     * moves forward); on January 1st of the next year it starts over from
     * that new year's January, since {@code to} is always "today".
     */
    private DateWindow dynamicsWindow() {
        LocalDate to = LocalDate.now(APPLICATION_ZONE);
        LocalDate from = LocalDate.of(to.getYear(), 1, 1);
        return new DateWindow(from, to);
    }

    private record DateWindow(LocalDate from, LocalDate to) {
    }

    /**
     * Runs {@code supplier} on {@link #applicationTaskExecutor}, copying
     * {@link CurrentOrganizationContext}'s value onto that worker thread
     * first — {@code CardStatsQueryService}/{@code ActStatsQueryService}
     * resolve the caller's organization scope by reading that ThreadLocal
     * internally, so without this each parallel branch would fail with a
     * scope-violation on its worker thread. Exceptions surface through the
     * returned future exactly as they would from a direct (synchronous)
     * call, via {@link CompletableFuture#join()}.
     */
    private <T> CompletableFuture<T> supplyOrgScoped(Organization organization, Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            CurrentOrganizationContext.set(organization);
            try {
                return supplier.get();
            } finally {
                CurrentOrganizationContext.clear();
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

    private CaseSummaryResponse buildCaseSummary(ResolvedOrganizationScope scope) {
        LocalDate today = LocalDate.now(APPLICATION_ZONE);
        CaseSummaryAggregate aggregate = caseStatsAggregateRepository.caseSummary(scope, today);

        return new CaseSummaryResponse(
                aggregate.form058Total() + aggregate.form0581Total(),
                aggregate.activeTotal(),
                aggregate.todayTotal(),
                today,
                aggregate.form058Total(),
                aggregate.form0581Total()
        );
    }

    private TimeSeriesResponse buildDynamics(ResolvedOrganizationScope scope) {
        DateWindow window = dynamicsWindow();
        List<DynamicsPointResponse> points =
                caseStatsAggregateRepository.monthlyDynamics(scope, window.from(), window.to());

        return new TimeSeriesResponse(window.from(), window.to(), TimeSeriesGranularity.MONTH, points);
    }

    /**
     * Top 5 diagnosis codes across both form types, ranked by their true
     * combined count — a single {@code GROUP BY ... ORDER BY ... LIMIT 5}
     * over the union of both tables, rather than taking each table's own
     * top-N candidates and re-ranking the merge (which, at a small enough
     * per-table candidate limit, could in principle exclude a code whose
     * combined rank belongs in the top 5 but whose per-table share never
     * individually cracked either table's candidate cutoff).
     */
    private List<TopDiagnosisResponse> buildTopDiagnoses(ResolvedOrganizationScope scope) {
        return caseStatsAggregateRepository.topDiagnoses(scope, TOP_DIAGNOSIS_RESULT_LIMIT);
    }

    private List<SourceCountResponse> buildSourceBreakdown(ResolvedOrganizationScope scope) {
        return caseStatsAggregateRepository.sourceBreakdown(scope);
    }

    private List<GeoBreakdownItemResponse> buildGeoBreakdown(ResolvedOrganizationScope scope) {
        return switch (scope.mode()) {
            case REGION -> buildDistrictBreakdown(scope.regionCode());
            case ALL -> buildRegionBreakdown();
            case DISTRICT, ORGANIZATION -> List.of();
        };
    }

    private List<GeoBreakdownItemResponse> buildDistrictBreakdown(String regionCode) {
        List<GeoCodeCount> counts = caseStatsAggregateRepository.districtBreakdown(regionCode);

        return counts.stream()
                .map(item -> new GeoBreakdownItemResponse(
                        item.code(),
                        referenceLookupService.getDistrictName(item.code()),
                        item.count()
                ))
                .toList();
    }

    private List<GeoBreakdownItemResponse> buildRegionBreakdown() {
        List<GeoCodeCount> counts = caseStatsAggregateRepository.regionBreakdown();

        return counts.stream()
                .map(item -> new GeoBreakdownItemResponse(
                        item.code(),
                        referenceLookupService.getRegionName(item.code()),
                        item.count()
                ))
                .toList();
    }

    private long countMedicalInstitutions(ResolvedOrganizationScope scope) {
        return switch (scope.mode()) {
            case ALL -> organizationRepository.countByActiveTrue();
            case REGION -> (long) organizationScopeOrganizationIdResolver.resolveScopeOrganizationIds(
                    OrganizationScopeMode.REGION, scope.regionCode(), scope.districtCode()
            ).size();
            case DISTRICT -> (long) organizationScopeOrganizationIdResolver.resolveScopeOrganizationIds(
                    OrganizationScopeMode.DISTRICT, scope.regionCode(), scope.districtCode()
            ).size();
            case ORGANIZATION -> 1L;
        };
    }

    private CardStatsResponse buildCardStats() {
        List<CardStatusCountResponse> byStatus = cardStatsQueryService.countByStatus();
        List<CardTypeCountResponse> byType = cardStatsQueryService.countByType();
        long total = cardStatsQueryService.countTotal();
        long active = cardStatsQueryService.countActive();

        return new CardStatsResponse(total, active, byStatus, byType, buildCardDynamics());
    }

    private TimeSeriesResponse buildCardDynamics() {
        DateWindow window = dynamicsWindow();

        List<DynamicsPointResponse> points = cardStatsQueryService.countByMonth(window.from(), window.to()).stream()
                .map(item -> new DynamicsPointResponse(item.date(), item.count()))
                .toList();

        return new TimeSeriesResponse(window.from(), window.to(), TimeSeriesGranularity.MONTH, points);
    }

    private ActStatsResponse buildActStats() {
        List<ActStatusCountResponse> byStatus = actStatsQueryService.countByStatus();
        long total = actStatsQueryService.countTotal();

        return new ActStatsResponse(total, byStatus, buildActDynamics());
    }

    private TimeSeriesResponse buildActDynamics() {
        DateWindow window = dynamicsWindow();

        List<DynamicsPointResponse> points = actStatsQueryService.countByMonth(window.from(), window.to()).stream()
                .map(item -> new DynamicsPointResponse(item.date(), item.count()))
                .toList();

        return new TimeSeriesResponse(window.from(), window.to(), TimeSeriesGranularity.MONTH, points);
    }
}
