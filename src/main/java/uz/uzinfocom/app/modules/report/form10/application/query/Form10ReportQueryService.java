package uz.uzinfocom.app.modules.report.form10.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
 * root-scope figure. The under-14 intensive uses the <b>same total territory
 * population</b> (there is no separate child-population source). {@code koef}
 * (default 100000) is a flat request parameter. Rate / growth-% / rounding
 * arithmetic lives here in Java, matching {@code Form11ReportQueryService}.
 */
@Service
@RequiredArgsConstructor
public class Form10ReportQueryService implements ReportCountSource<Form10Counts> {

    private static final String TOTAL_ROW_CODE = "TOTAL";

    private final Form10ReportRepository form10ReportRepository;
    private final ReportHierarchyService reportHierarchyService;
    private final ReportPeriodResolver reportPeriodResolver;
    private final PopulationLookupService populationLookupService;

    public List<Form10ReportNodeResponse> getRoot(int year, ReportPeriod period, String diagnosisCode, long koef) {
        Organization currentOrganization = requireCurrentOrganization();
        String rootCode = reportHierarchyService.resolveNode(currentOrganization, null, null).code();

        return build(
                range -> reportHierarchyService.loadRootBreakdown(currentOrganization, this, range, diagnosisCode),
                year, period, koef, rootCode
        );
    }

    public List<Form10ReportNodeResponse> getChildren(
            String regionCode, String districtCode, int year, ReportPeriod period, String diagnosisCode, long koef
    ) {
        Organization currentOrganization = requireCurrentOrganization();
        String rootCode = reportHierarchyService.resolveNode(currentOrganization, regionCode, districtCode).code();

        return build(
                range -> reportHierarchyService.loadChildren(
                        currentOrganization, regionCode, districtCode, this, range, diagnosisCode
                ),
                year, period, koef, rootCode
        );
    }

    private List<Form10ReportNodeResponse> build(
            Function<ReportDateRange, List<ReportHierarchyNode<Form10Counts>>> walk,
            int year,
            ReportPeriod period,
            long koef,
            String rootCode
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
                    boolean useRootPopulation = TOTAL_ROW_CODE.equals(code) || isNumeric(code);
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
                absPrev, absCurr, round2(growthPercent(absPrev, absCurr)),
                round2(intensivePrev), round2(intensiveCurr), round2(growthPercent(intensivePrev, intensiveCurr))
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
     * {@code ((curr - prev) / prev) * 100}. When {@code prev == 0}: {@code 0}
     * if {@code curr == 0}, else {@code 100}. Matches {@code
     * Form11ReportQueryService#growthPercent}.
     */
    private double growthPercent(double prev, double curr) {
        if (prev == 0d) {
            return curr == 0d ? 0d : 100d;
        }
        return ((curr - prev) / prev) * 100d;
    }

    private double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private boolean isNumeric(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        for (int i = 0; i < code.length(); i++) {
            if (!Character.isDigit(code.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}
