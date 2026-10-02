package uz.uzinfocom.app.modules.report.form10.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.reference.application.lookup.PopulationLookupService;
import uz.uzinfocom.app.modules.report.form10.application.query.dto.Form10Block;
import uz.uzinfocom.app.modules.report.form10.application.query.dto.Form10Counts;
import uz.uzinfocom.app.modules.report.form10.application.query.dto.Form10Metric;
import uz.uzinfocom.app.modules.report.form10.application.query.dto.Form10OrganizationCountProjection;
import uz.uzinfocom.app.modules.report.form10.application.query.dto.Form10ReportNodeResponse;
import uz.uzinfocom.app.modules.report.form10.infrastructure.persistence.repository.Form10ReportRepository;
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
 * "Form 10" — query service for «Respublika bo'yicha ma'muriy hududlar
 * kesimida yuqumli kasalliklar bilan kasallanish to'g'risidagi ma'lumotlar».
 * Confirmed notifications only ({@code form058} + {@code form058_1}, {@code
 * status = 'APPROVED'}); republic→region→district→organization drill-down is
 * delegated to {@link ReportHierarchyService} (this class is the {@link
 * ReportCountSource}, backed by {@link Form10ReportRepository}); no per-node
 * breakdown panel.
 * <p>
 * Structurally a "Form 11" with a richer period model. Instead of one
 * caller-supplied {@code from}/{@code to} it takes a {@code year} + a {@link
 * ReportPeriod}, which {@link ReportPeriodResolver} expands into <b>four</b>
 * spans: "Joriy davr" (the period's month span) and "Yig'ma" (January through
 * the period's end), each for {@code year} and for {@code year - 1}. The
 * hierarchy is therefore walked four times and the results zipped by node
 * code into one {@link Form10ReportNodeResponse} per row — two column blocks,
 * each with a whole-population and an under-14 triple of (prev year | current
 * year | growth %) × (absolute | intensive).
 * <p>
 * The intensive rate divides by that node's own territory population for the
 * relevant year, looked up from {@code ref_population} via {@link
 * PopulationLookupService} — republic root code, region code, or district
 * code; organization rows and the "Jami" row reuse their parent district /
 * root-scope figure. Which of those a given call's nodes are is read off the
 * caller's {@link ResolvedOrganizationScope} / explicit {@code
 * regionCode}/{@code districtCode} the same way {@code
 * ReportHierarchyService#loadChildren} itself branches — <b>not</b> guessed
 * from the node code's shape, since region/district codes (e.g. {@code
 * "1726"}) are themselves plain digit strings indistinguishable that way from
 * an organization id. The under-14 intensive uses the <b>same total territory
 * population</b> (there is no separate child-population source).
 * <p>
 * {@code koef} (default 100000) is a flat request parameter. Rate / rounding
 * arithmetic lives here in Java. Two figures deliberately diverge from
 * {@code Form11ReportQueryService}, both per the published "Shakl 10"
 * template rather than Form 11's plainer conventions: the absolute-count
 * "growth" figure is a plain {@code |curr - prev|} case-count difference,
 * not a percentage; and the intensive-rate growth ({@link #growthDisplay})
 * is a bounded/"X marta" display string, not the plain unbounded percentage
 * Form 11 still returns.
 */
@Service
@RequiredArgsConstructor
public class Form10ReportQueryService implements ReportCountSource<Form10Counts> {

    private static final String TOTAL_ROW_CODE = "TOTAL";

    private final Form10ReportRepository form10ReportRepository;
    private final ReportHierarchyService reportHierarchyService;
    private final ReportPeriodResolver reportPeriodResolver;
    private final PopulationLookupService populationLookupService;
    private final OrganizationScopeResolver organizationScopeResolver;

    public List<Form10ReportNodeResponse> getRoot(int year, ReportPeriod period, String diagnosisCode, long koef) {
        Organization currentOrganization = requireCurrentOrganization();
        String rootCode = reportHierarchyService.resolveNode(currentOrganization, null, null).code();
        boolean organizationLevel = isOrganizationLevel(currentOrganization);

        return build(
                range -> reportHierarchyService.loadRootBreakdown(currentOrganization, this, range, diagnosisCode),
                year, period, koef, rootCode, organizationLevel
        );
    }

    public List<Form10ReportNodeResponse> getChildren(
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
     * own {@code ref_population} row and must never reuse the root figure.
     */
    private boolean isOrganizationLevel(Organization currentOrganization) {
        ResolvedOrganizationScope scope = organizationScopeResolver.resolve(currentOrganization);
        return scope.mode() == OrganizationScopeMode.DISTRICT || scope.mode() == OrganizationScopeMode.ORGANIZATION;
    }

    private List<Form10ReportNodeResponse> build(
            Function<ReportDateRange, List<ReportHierarchyNode<Form10Counts>>> walk,
            int year,
            ReportPeriod period,
            long koef,
            String rootCode,
            boolean organizationLevel
    ) {
        int previousYear = year - 1;

        List<ReportHierarchyNode<Form10Counts>> currentNodes = walk.apply(reportPeriodResolver.current(year, period));
        Map<String, Form10Counts> currentPrevYear = index(walk.apply(reportPeriodResolver.current(previousYear, period)));
        Map<String, Form10Counts> cumulativeCurrent = index(walk.apply(reportPeriodResolver.cumulative(year, period)));
        Map<String, Form10Counts> cumulativePrevYear =
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

                    Form10Counts currentPeriodCurrentYear = node.counts();
                    Form10Counts currentPeriodPrevYear = currentPrevYear.getOrDefault(code, Form10Counts.EMPTY);
                    Form10Counts cumulativeCurrentYear = cumulativeCurrent.getOrDefault(code, Form10Counts.EMPTY);
                    Form10Counts cumulativePrevYearCounts = cumulativePrevYear.getOrDefault(code, Form10Counts.EMPTY);

                    return new Form10ReportNodeResponse(
                            code, node.name(), node.hasChildren(),
                            block(currentPeriodPrevYear, currentPeriodCurrentYear,
                                    populationPrevious, populationCurrent, koef),
                            block(cumulativePrevYearCounts, cumulativeCurrentYear,
                                    populationPrevious, populationCurrent, koef)
                    );
                })
                .toList();
    }

    private Form10Block block(Form10Counts previous, Form10Counts current, long popPrev, long popCurr, long koef) {
        return new Form10Block(
                metric(previous.total(), current.total(), popPrev, popCurr, koef),
                metric(previous.child(), current.child(), popPrev, popCurr, koef)
        );
    }

    private Form10Metric metric(long absPrev, long absCurr, long popPrev, long popCurr, long koef) {
        double intensivePrev = intensity(absPrev, popPrev, koef);
        double intensiveCurr = intensity(absCurr, popCurr, koef);
        return new Form10Metric(
                absPrev, absCurr, Math.abs(absCurr - absPrev),
                round2(intensivePrev), round2(intensiveCurr), growthDisplay(intensivePrev, intensiveCurr)
        );
    }

    @Override
    public Form10Counts total(List<Long> organizationIds, ReportDateRange range, String diagnosisCode) {
        return form10ReportRepository.countTotal(
                organizationIds, range.fromInclusive(), range.toExclusive(), diagnosisCode
        );
    }

    @Override
    public Map<Long, Form10Counts> groupedByOrganization(
            List<Long> organizationIds, ReportDateRange range, String diagnosisCode
    ) {
        return form10ReportRepository
                .countGroupedByOrganization(organizationIds, range.fromInclusive(), range.toExclusive(), diagnosisCode)
                .stream()
                .collect(Collectors.toMap(
                        Form10OrganizationCountProjection::organizationId,
                        p -> new Form10Counts(p.total(), p.child())
                ));
    }

    @Override
    public Form10Counts empty() {
        return Form10Counts.EMPTY;
    }

    @Override
    public Form10Counts merge(Form10Counts a, Form10Counts b) {
        return a.plus(b);
    }

    private Map<String, Form10Counts> index(List<ReportHierarchyNode<Form10Counts>> nodes) {
        return nodes.stream()
                .collect(Collectors.toMap(ReportHierarchyNode::code, ReportHierarchyNode::counts));
    }

    /** Rate per {@code koef} of {@code population}. Matches {@code Form11ReportQueryService#intensity}. */
    private double intensity(long count, long population, long koef) {
        if (population == 0) {
            return 0d;
        }
        return (count * (double) koef) / population;
    }

    /**
     * Reproduces the published "Shakl 10" template's growth-display formula (the {@code IFS(...)}
     * behind the sample workbook's "o'sish/pasayish %" column) rather than a plain {@code
     * ((curr - prev) / prev) * 100} — that plain formula is unbounded on the increase side (a
     * 10x jump reads "900%"), so the template instead switches to a "X marta" ("X-fold") text
     * once either direction's ratio reaches 2, and otherwise reports a percentage bounded under
     * 100% by dividing against whichever of {@code prev}/{@code curr} is larger — not always
     * {@code prev} as a plain formula would. Only diverges from the plain formula on increases;
     * decreases (and the zero/equal edge cases) come out numerically identical either way, since
     * {@code prev} is already the larger value there.
     * <p>
     * Branches, in order: equal → {@code "0"}; {@code prev == 0} → {@code "100"}; {@code curr ==
     * 0} → {@code "-100"}; ratio ≥ 2 either direction → {@code "X marta"} / {@code "-X marta"}
     * (one decimal, per the template); otherwise a plain-formatted percentage (two decimals).
     * Form10-only — does <b>not</b> match {@code Form11ReportQueryService#growthPercent}, which
     * still returns the plain unbounded percentage.
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
