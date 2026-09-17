package uz.uzinfocom.app.modules.report.form11.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.reference.application.lookup.PopulationLookupService;
import uz.uzinfocom.app.modules.report.form11.application.query.dto.Form11Block;
import uz.uzinfocom.app.modules.report.form11.application.query.dto.Form11Counts;
import uz.uzinfocom.app.modules.report.form11.application.query.dto.Form11Metric;
import uz.uzinfocom.app.modules.report.form11.application.query.dto.Form11OrganizationCountProjection;
import uz.uzinfocom.app.modules.report.form11.application.query.dto.Form11ReportNodeResponse;
import uz.uzinfocom.app.modules.report.form11.infrastructure.persistence.repository.Form11ReportRepository;
import uz.uzinfocom.app.modules.report.shared.ReportCountSource;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyNode;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyService;
import uz.uzinfocom.app.modules.report.shared.ReportPeriod;
import uz.uzinfocom.app.modules.report.shared.ReportPeriodResolver;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeMode;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.orchestration.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * "Form 11" — query service for «Yuqumli va parazitar kasalliklar bilan
 * kasallanish ko'rsatkichlari». Confirmed notifications only ({@code
 * form058} + {@code form058_1}, {@code status = 'APPROVED'});
 * republic→region→district→organization drill-down is delegated to {@link
 * ReportHierarchyService} (this class is the {@link ReportCountSource},
 * backed by {@link Form11ReportRepository}); no per-node breakdown panel.
 * <p>
 * Structurally identical to {@code Form10ReportQueryService}: instead of one
 * caller-supplied {@code from}/{@code to} it takes a {@code year} + a {@link
 * ReportPeriod}, which {@link ReportPeriodResolver} expands into <b>four</b>
 * spans — "Joriy davr" (the period's month span) and "Yig'ma" (January
 * through the period's end), each for {@code year} and for {@code year - 1}.
 * The hierarchy is therefore walked four times and the results zipped by
 * node code into one {@link Form11ReportNodeResponse} per row — two column
 * blocks, each with a whole-population, urban, rural and under-18 quadruple
 * of (previous year | current year | growth) × (absolute | intensive).
 * <p>
 * The intensive rate divides by that node's own territory population for the
 * relevant year, looked up from {@code ref_population} via {@link
 * PopulationLookupService} — same republic/region/district/organization-level
 * resolution {@code Form10ReportQueryService} uses. Every metric in a block
 * (total, city, rural, child) shares that same territory-population
 * denominator — there is no separate urban/rural/age-segmented population
 * source, matching {@code Form10ReportQueryService}'s under-14 cut.
 * <p>
 * {@code koef} (default 100000) is a flat request parameter. Rate / rounding
 * arithmetic lives here in Java, matching {@code Form10ReportQueryService}
 * exactly (including its "X marta" bounded intensive-growth display and
 * plain {@code |curr - prev|} absolute-growth figure).
 */
@Service
@RequiredArgsConstructor
public class Form11ReportQueryService implements ReportCountSource<Form11Counts> {

    private static final String TOTAL_ROW_CODE = "TOTAL";

    private final Form11ReportRepository form11ReportRepository;
    private final ReportHierarchyService reportHierarchyService;
    private final ReportPeriodResolver reportPeriodResolver;
    private final PopulationLookupService populationLookupService;
    private final OrganizationScopeResolver organizationScopeResolver;

    public List<Form11ReportNodeResponse> getRoot(int year, ReportPeriod period, String diagnosisCode, long koef) {
        Organization currentOrganization = requireCurrentOrganization();
        String rootCode = reportHierarchyService.resolveNode(currentOrganization, null, null).code();
        boolean organizationLevel = isOrganizationLevel(currentOrganization);

        return build(
                range -> reportHierarchyService.loadRootBreakdown(currentOrganization, this, range, diagnosisCode),
                year, period, koef, rootCode, organizationLevel
        );
    }

    public List<Form11ReportNodeResponse> getChildren(
            String regionCode, String districtCode, int year, ReportPeriod period, String diagnosisCode, long koef
    ) {
        Organization currentOrganization = requireCurrentOrganization();
        String rootCode = reportHierarchyService.resolveNode(currentOrganization, regionCode, districtCode).code();
        boolean organizationLevel = StringUtils.hasText(districtCode)
                || (!StringUtils.hasText(regionCode) && isOrganizationLevel(currentOrganization));

        return build(
                range -> reportHierarchyService.loadChildren(
                        currentOrganization, regionCode, districtCode, this, range, diagnosisCode
                ),
                year, period, koef, rootCode, organizationLevel
        );
    }

    /**
     * Whether a scope-driven (no explicit {@code regionCode}/{@code districtCode}) call returns
     * organization-level nodes — mirrors {@code ReportHierarchyService#loadChildren}'s own scope
     * switch: DISTRICT scope lists organizations, ORGANIZATION scope's single fallback row is
     * the caller's own organization; ALL/REGION scope list regions/districts, which have their
     * own {@code ref_population} row and must never reuse the root figure. Matches {@code
     * Form10ReportQueryService#isOrganizationLevel} exactly.
     */
    private boolean isOrganizationLevel(Organization currentOrganization) {
        ResolvedOrganizationScope scope = organizationScopeResolver.resolve(currentOrganization);
        return scope.mode() == OrganizationScopeMode.DISTRICT || scope.mode() == OrganizationScopeMode.ORGANIZATION;
    }

    private List<Form11ReportNodeResponse> build(
            Function<ReportDateRange, List<ReportHierarchyNode<Form11Counts>>> walk,
            int year,
            ReportPeriod period,
            long koef,
            String rootCode,
            boolean organizationLevel
    ) {
        int previousYear = year - 1;

        List<ReportHierarchyNode<Form11Counts>> currentNodes = walk.apply(reportPeriodResolver.current(year, period));
        Map<String, Form11Counts> currentPrevYear = index(walk.apply(reportPeriodResolver.current(previousYear, period)));
        Map<String, Form11Counts> cumulativeCurrent = index(walk.apply(reportPeriodResolver.cumulative(year, period)));
        Map<String, Form11Counts> cumulativePrevYear =
                index(walk.apply(reportPeriodResolver.cumulative(previousYear, period)));

        long rootPopulationCurrent = populationLookupService.resolveByNodeCode(rootCode, year);
        long rootPopulationPrevious = populationLookupService.resolveByNodeCode(rootCode, previousYear);

        return currentNodes.stream()
                .map(node -> {
                    String code = node.code();
                    boolean useRootPopulation = TOTAL_ROW_CODE.equals(code) || organizationLevel;
                    long populationCurrent = useRootPopulation
                            ? rootPopulationCurrent
                            : populationLookupService.resolveByNodeCode(code, year);
                    long populationPrevious = useRootPopulation
                            ? rootPopulationPrevious
                            : populationLookupService.resolveByNodeCode(code, previousYear);

                    Form11Counts currentPeriodCurrentYear = node.counts();
                    Form11Counts currentPeriodPrevYear = currentPrevYear.getOrDefault(code, Form11Counts.EMPTY);
                    Form11Counts cumulativeCurrentYear = cumulativeCurrent.getOrDefault(code, Form11Counts.EMPTY);
                    Form11Counts cumulativePrevYearCounts = cumulativePrevYear.getOrDefault(code, Form11Counts.EMPTY);

                    return new Form11ReportNodeResponse(
                            code, node.name(), node.hasChildren(),
                            block(currentPeriodPrevYear, currentPeriodCurrentYear,
                                    populationPrevious, populationCurrent, koef),
                            block(cumulativePrevYearCounts, cumulativeCurrentYear,
                                    populationPrevious, populationCurrent, koef)
                    );
                })
                .toList();
    }

    private Form11Block block(Form11Counts previous, Form11Counts current, long popPrev, long popCurr, long koef) {
        return new Form11Block(
                metric(previous.total(), current.total(), popPrev, popCurr, koef),
                metric(previous.city(), current.city(), popPrev, popCurr, koef),
                metric(previous.rural(), current.rural(), popPrev, popCurr, koef),
                metric(previous.child(), current.child(), popPrev, popCurr, koef)
        );
    }

    private Form11Metric metric(long absPrev, long absCurr, long popPrev, long popCurr, long koef) {
        double intensivePrev = intensity(absPrev, popPrev, koef);
        double intensiveCurr = intensity(absCurr, popCurr, koef);
        return new Form11Metric(
                absPrev, absCurr, Math.abs(absCurr - absPrev),
                round2(intensivePrev), round2(intensiveCurr), growthDisplay(intensivePrev, intensiveCurr)
        );
    }

    @Override
    public Form11Counts total(List<Long> organizationIds, ReportDateRange range, String diagnosisCode) {
        return form11ReportRepository.countTotal(
                organizationIds, range.fromInclusive(), range.toExclusive(), diagnosisCode
        );
    }

    @Override
    public Map<Long, Form11Counts> groupedByOrganization(
            List<Long> organizationIds, ReportDateRange range, String diagnosisCode
    ) {
        return form11ReportRepository
                .countGroupedByOrganization(organizationIds, range.fromInclusive(), range.toExclusive(), diagnosisCode)
                .stream()
                .collect(Collectors.toMap(
                        Form11OrganizationCountProjection::organizationId,
                        p -> new Form11Counts(p.total(), p.city(), p.rural(), p.child())
                ));
    }

    @Override
    public Form11Counts empty() {
        return Form11Counts.EMPTY;
    }

    @Override
    public Form11Counts merge(Form11Counts a, Form11Counts b) {
        return a.plus(b);
    }

    private Map<String, Form11Counts> index(List<ReportHierarchyNode<Form11Counts>> nodes) {
        return nodes.stream()
                .collect(Collectors.toMap(ReportHierarchyNode::code, ReportHierarchyNode::counts));
    }

    /** Rate per {@code koef} of {@code population}. Matches {@code Form10ReportQueryService#intensity}. */
    private double intensity(long count, long population, long koef) {
        if (population == 0) {
            return 0d;
        }
        return (count * (double) koef) / population;
    }

    /**
     * Reproduces the published "Shakl 10"/"Shakl 11" template's growth-display formula. Matches
     * {@code Form10ReportQueryService#growthDisplay} exactly — see there for the full rationale.
     * Branches, in order: equal → {@code "0"}; {@code prev == 0} → {@code "100"}; {@code curr ==
     * 0} → {@code "-100"}; ratio ≥ 2 either direction → {@code "X marta"} / {@code "-X marta"}
     * (one decimal); otherwise a plain-formatted percentage (two decimals), bounded under 100% by
     * dividing against whichever of {@code prev}/{@code curr} is larger.
     */
    private String growthDisplay(double prev, double curr) {
        if (prev == curr) {
            return formatNumber(0d, 2);
        }
        if (prev == 0d) {
            return formatNumber(100d, 2);
        }
        if (curr == 0d) {
            return formatNumber(-100d, 2);
        }
        if (prev / curr >= 2d) {
            return "-" + formatNumber(prev / curr, 1) + " marta";
        }
        if (curr / prev >= 2d) {
            return formatNumber(curr / prev, 1) + " marta";
        }
        if (prev > curr) {
            return formatNumber((100d - (curr * 100d / prev)) * -1d, 2);
        }
        return formatNumber(100d - (prev * 100d / curr), 2);
    }

    /** Fixed-scale decimal formatting with trailing zeros stripped (e.g. {@code -12.22}, {@code 4}, not {@code 4.0}). */
    private String formatNumber(double value, int scale) {
        BigDecimal rounded = BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).stripTrailingZeros();
        return rounded.scale() < 0 ? rounded.toBigInteger().toString() : rounded.toPlainString();
    }

    private double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}
