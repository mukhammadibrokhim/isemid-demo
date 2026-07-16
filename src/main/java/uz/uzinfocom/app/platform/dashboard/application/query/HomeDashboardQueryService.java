package uz.uzinfocom.app.platform.dashboard.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.act.application.query.ActStatsQueryService;
import uz.uzinfocom.app.modules.act.application.query.dto.ActStatusCountResponse;
import uz.uzinfocom.app.modules.card.application.query.CardStatsQueryService;
import uz.uzinfocom.app.modules.card.application.query.dto.CardStatusCountResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.CardTypeCountResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.form058.application.stats.query.Form058StatsQueryService;
import uz.uzinfocom.app.modules.form058.application.stats.query.dto.Form058DailyCountResponse;
import uz.uzinfocom.app.modules.form058.application.stats.query.dto.Form058StatusCountResponse;
import uz.uzinfocom.app.modules.form058.web.dto.request.enums.Form058Direction;
import uz.uzinfocom.app.modules.form0581.application.stats.query.Form0581StatsQueryService;
import uz.uzinfocom.app.modules.form0581.application.stats.query.dto.Form0581DailyCountResponse;
import uz.uzinfocom.app.modules.form0581.application.stats.query.dto.Form0581StatusCountResponse;
import uz.uzinfocom.app.modules.form0581.web.dto.request.enums.Form0581Direction;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.ActStatsResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.CardStatsResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.CaseSummaryResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.DashboardScopeResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.DynamicsPointResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.GeoBreakdownItemResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.HomeDashboardResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.TopDiagnosisResponse;
import uz.uzinfocom.app.platform.iam.application.shared.dto.OrganizationGeoProjection;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.platform.reference.domain.District;
import uz.uzinfocom.app.platform.reference.domain.Region;
import uz.uzinfocom.app.platform.reference.repository.DistrictRepository;
import uz.uzinfocom.app.platform.reference.repository.RegionRepository;
import uz.uzinfocom.app.platform.scope.OrganizationScopeMode;
import uz.uzinfocom.app.platform.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.scope.jpa.OrganizationScopeOrganizationIdResolver;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Assembles the "everyone" home dashboard — the org-scoped counterpart to the
 * unscoped admin dashboards ({@code Form058AdminStatsController} etc.). Case
 * metrics combine Form058 and Form0581 (both are epidemiological case
 * reports); Card/Act metrics are their own totals, since they represent a
 * different kind of workload (investigation, not case reporting).
 * <p>
 * This service depends only on each module's public query-service façade
 * ({@code Form058StatsQueryService}, {@code CardStatsQueryService}, etc.),
 * never on another module's repository — the same {@code Controller ->
 * Service -> Repository} boundary every module in this codebase enforces for
 * itself. {@code OrganizationScopeResolver}/{@code OrganizationRepository}/
 * {@code DistrictRepository}/{@code RegionRepository}/{@code
 * ReferenceLookupService} are platform-level shared infrastructure, not a
 * business module's internals, so depending on them directly here is
 * consistent with how they are already used elsewhere in the platform layer.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeDashboardQueryService {

    private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Tashkent");
    private static final int DYNAMICS_MONTHS = 6;
    private static final int TOP_DIAGNOSIS_CANDIDATE_LIMIT = 20;
    private static final int TOP_DIAGNOSIS_RESULT_LIMIT = 5;

    private final Form058StatsQueryService form058StatsQueryService;
    private final Form0581StatsQueryService form0581StatsQueryService;
    private final CardStatsQueryService cardStatsQueryService;
    private final ActStatsQueryService actStatsQueryService;
    private final OrganizationScopeResolver organizationScopeResolver;
    private final OrganizationScopeOrganizationIdResolver organizationScopeOrganizationIdResolver;
    private final OrganizationRepository organizationRepository;
    private final DistrictRepository districtRepository;
    private final RegionRepository regionRepository;
    private final ReferenceLookupService referenceLookupService;

    public HomeDashboardResponse getHome() {
        Organization currentOrganization = CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
        ResolvedOrganizationScope scope = organizationScopeResolver.resolve(currentOrganization);

        return new HomeDashboardResponse(
                buildScope(scope),
                buildCaseSummary(),
                buildDynamics(),
                buildTopDiagnoses(),
                buildGeoBreakdown(scope),
                countMedicalInstitutions(scope),
                buildCardStats(),
                buildActStats()
        );
    }

    private DashboardScopeResponse buildScope(ResolvedOrganizationScope scope) {
        String regionName = scope.regionCode() != null ? referenceLookupService.getRegionName(scope.regionCode()) : null;
        String districtName = scope.districtCode() != null
                ? referenceLookupService.getDistrictName(scope.districtCode())
                : null;

        return new DashboardScopeResponse(scope.mode(), scope.regionCode(), regionName, scope.districtCode(), districtName);
    }

    private CaseSummaryResponse buildCaseSummary() {
        List<Form058StatusCountResponse> form058Statuses = form058StatsQueryService.countByStatus(Form058Direction.INCOMING);
        List<Form0581StatusCountResponse> form0581Statuses =
                form0581StatsQueryService.countByStatus(Form0581Direction.INCOMING);

        long form058Total = form058Statuses.stream().mapToLong(Form058StatusCountResponse::count).sum();
        long form0581Total = form0581Statuses.stream().mapToLong(Form0581StatusCountResponse::count).sum();

        long form058Active = form058Statuses.stream()
                .filter(item -> item.status().isApprovalDecisionPending())
                .mapToLong(Form058StatusCountResponse::count)
                .sum();
        long form0581Active = form0581Statuses.stream()
                .filter(item -> item.status().isApprovalDecisionPending())
                .mapToLong(Form0581StatusCountResponse::count)
                .sum();

        LocalDate today = LocalDate.now(APPLICATION_ZONE);
        long form058Today = form058StatsQueryService.countByDay(Form058Direction.INCOMING, today, today).stream()
                .mapToLong(Form058DailyCountResponse::count)
                .sum();
        long form0581Today = form0581StatsQueryService.countByDay(Form0581Direction.INCOMING, today, today).stream()
                .mapToLong(Form0581DailyCountResponse::count)
                .sum();

        return new CaseSummaryResponse(
                form058Total + form0581Total,
                form058Active + form0581Active,
                form058Today + form0581Today,
                form058Total,
                form0581Total
        );
    }

    private List<DynamicsPointResponse> buildDynamics() {
        LocalDate to = LocalDate.now(APPLICATION_ZONE);
        LocalDate from = to.minusMonths(DYNAMICS_MONTHS - 1L).withDayOfMonth(1);

        Map<LocalDate, Long> merged = new TreeMap<>();
        form058StatsQueryService.countByMonth(Form058Direction.INCOMING, from, to)
                .forEach(item -> merged.merge(item.date(), item.count(), Long::sum));
        form0581StatsQueryService.countByMonth(Form0581Direction.INCOMING, from, to)
                .forEach(item -> merged.merge(item.date(), item.count(), Long::sum));

        return merged.entrySet().stream()
                .map(entry -> new DynamicsPointResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<TopDiagnosisResponse> buildTopDiagnoses() {
        Map<String, Long> merged = new HashMap<>();
        form058StatsQueryService.topMkb10(Form058Direction.INCOMING, TOP_DIAGNOSIS_CANDIDATE_LIMIT)
                .forEach(item -> merged.merge(item.mkb10Code(), item.count(), Long::sum));
        form0581StatsQueryService.topMkb10(Form0581Direction.INCOMING, TOP_DIAGNOSIS_CANDIDATE_LIMIT)
                .forEach(item -> merged.merge(item.mkb10Code(), item.count(), Long::sum));

        return merged.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(TOP_DIAGNOSIS_RESULT_LIMIT)
                .map(entry -> new TopDiagnosisResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<GeoBreakdownItemResponse> buildGeoBreakdown(ResolvedOrganizationScope scope) {
        return switch (scope.mode()) {
            case REGION -> buildDistrictBreakdown(scope.regionCode());
            case ALL -> buildRegionBreakdown();
            case DISTRICT, ORGANIZATION -> List.of();
        };
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

    private Map<String, Long> countsByGeoCode(List<OrganizationGeoProjection> organizations) {
        Map<Long, String> geoCodeByOrgId = new HashMap<>();
        for (OrganizationGeoProjection organization : organizations) {
            geoCodeByOrgId.put(organization.id(), organization.code());
        }

        List<Long> organizationIds = organizations.stream().map(OrganizationGeoProjection::id).toList();

        Map<Long, Long> countsByOrgId = new HashMap<>();
        form058StatsQueryService.countByReceiverOrganizationWithinIds(organizationIds)
                .forEach(item -> countsByOrgId.merge(item.organizationId(), item.count(), Long::sum));
        form0581StatsQueryService.countByReceiverOrganizationWithinIds(organizationIds)
                .forEach(item -> countsByOrgId.merge(item.organizationId(), item.count(), Long::sum));

        Map<String, Long> countsByGeoCode = new HashMap<>();
        countsByOrgId.forEach((organizationId, count) -> {
            String geoCode = geoCodeByOrgId.get(organizationId);
            if (geoCode != null) {
                countsByGeoCode.merge(geoCode, count, Long::sum);
            }
        });

        return countsByGeoCode;
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

        long total = byStatus.stream().mapToLong(CardStatusCountResponse::count).sum();
        long approved = byStatus.stream()
                .filter(item -> item.status() == CardStatus.APPROVED)
                .mapToLong(CardStatusCountResponse::count)
                .sum();

        return new CardStatsResponse(total, total - approved, byStatus, byType);
    }

    private ActStatsResponse buildActStats() {
        List<ActStatusCountResponse> byStatus = actStatsQueryService.countByStatus();
        long total = byStatus.stream().mapToLong(ActStatusCountResponse::count).sum();

        return new ActStatsResponse(total, byStatus);
    }
}
