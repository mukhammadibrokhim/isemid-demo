package uz.uzinfocom.app.modules.report.form11.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.report.form11.application.query.dto.Form11Counts;
import uz.uzinfocom.app.modules.report.form11.application.query.dto.Form11OrganizationCountProjection;
import uz.uzinfocom.app.modules.report.form11.application.query.dto.Form11ReportNodeResponse;
import uz.uzinfocom.app.modules.report.form11.infrastructure.persistence.repository.Form11ReportRepository;
import uz.uzinfocom.app.modules.report.shared.ReportCountSource;
import uz.uzinfocom.app.modules.report.shared.ReportDateRange;
import uz.uzinfocom.app.modules.report.shared.ReportDateRangeResolver;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyNode;
import uz.uzinfocom.app.modules.report.shared.ReportHierarchyService;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * "Form 11" — query service for the infectious/parasitic disease
 * morbidity-indicator report ({@code form058} + {@code form058_1} combined,
 * confirmed notifications only — {@code status = 'APPROVED'}). Structurally identical to "Form
 * 9": republic→region→district→organization drill-down is delegated to {@link
 * ReportHierarchyService} (implements {@link ReportCountSource}, backed by
 * {@link Form11ReportRepository}), and every node is shown twice — once for
 * the caller's period, once for the same calendar dates one year earlier
 * ({@link ReportDateRangeResolver}'s {@code yearsAgo} parameter) — zipped by
 * node code into one response row.
 * <p>
 * Differences from "Form 9": no per-node breakdown drill-down (geography
 * only), and each row additionally carries an <b>intensive indicator</b> (a
 * rate per {@code koef} of {@code population}, both caller-supplied and flat
 * across every node — a simplification carried over from legacy {@code
 * Form11ReportController}) plus urban / rural / under-18 cuts of the current
 * period. The intensive-rate, growth-% and share-% arithmetic lives here in
 * Java (see the private helpers), not in SQL.
 */
@Service
@RequiredArgsConstructor
public class Form11ReportQueryService implements ReportCountSource<Form11Counts> {

    private final Form11ReportRepository form11ReportRepository;
    private final ReportHierarchyService reportHierarchyService;
    private final ReportDateRangeResolver reportDateRangeResolver;

    public List<Form11ReportNodeResponse> getRoot(
            LocalDate from, LocalDate to, String diagnosisCode, long koef, long population
    ) {
        Organization currentOrganization = requireCurrentOrganization();

        List<ReportHierarchyNode<Form11Counts>> currentNodes = reportHierarchyService.loadRootBreakdown(
                currentOrganization, this, reportDateRangeResolver.resolve(from, to), diagnosisCode
        );
        List<ReportHierarchyNode<Form11Counts>> previousNodes = reportHierarchyService.loadRootBreakdown(
                currentOrganization, this, reportDateRangeResolver.resolve(from, to, 1), diagnosisCode
        );

        return zip(currentNodes, previousNodes, koef, population);
    }

    public List<Form11ReportNodeResponse> getChildren(
            String regionCode,
            String districtCode,
            LocalDate from,
            LocalDate to,
            String diagnosisCode,
            long koef,
            long population
    ) {
        Organization currentOrganization = requireCurrentOrganization();

        List<ReportHierarchyNode<Form11Counts>> currentNodes = reportHierarchyService.loadChildren(
                currentOrganization, regionCode, districtCode, this,
                reportDateRangeResolver.resolve(from, to), diagnosisCode
        );
        List<ReportHierarchyNode<Form11Counts>> previousNodes = reportHierarchyService.loadChildren(
                currentOrganization, regionCode, districtCode, this,
                reportDateRangeResolver.resolve(from, to, 1), diagnosisCode
        );

        return zip(currentNodes, previousNodes, koef, population);
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

    private List<Form11ReportNodeResponse> zip(
            List<ReportHierarchyNode<Form11Counts>> currentNodes,
            List<ReportHierarchyNode<Form11Counts>> previousNodes,
            long koef,
            long population
    ) {
        Map<String, Form11Counts> previousByCode = previousNodes.stream()
                .collect(Collectors.toMap(ReportHierarchyNode::code, ReportHierarchyNode::counts));

        return currentNodes.stream()
                .map(node -> {
                    Form11Counts current = node.counts();
                    long absPrev = previousByCode.getOrDefault(node.code(), Form11Counts.EMPTY).total();
                    long absCurr = current.total();

                    double intPrev = intensity(absPrev, population, koef);
                    double intCurr = intensity(absCurr, population, koef);

                    return new Form11ReportNodeResponse(
                            node.code(), node.name(), node.hasChildren(),
                            absPrev, absCurr, round2(growthPercent(absPrev, absCurr)),
                            round2(intPrev), round2(intCurr), round2(growthPercent(intPrev, intCurr)),
                            current.city(),
                            round2(intensity(current.city(), population, koef)),
                            round2(sharePercent(absCurr, current.city())),
                            current.rural(),
                            round2(intensity(current.rural(), population, koef)),
                            round2(sharePercent(absCurr, current.rural())),
                            current.child(),
                            round2(intensity(current.child(), population, koef))
                    );
                })
                .toList();
    }

    /** Rate per {@code koef} of {@code population}. Matches legacy {@code Form11ReportHelper#intensity}. */
    private double intensity(long count, long population, long koef) {
        if (population == 0) {
            return 0d;
        }
        return (count * (double) koef) / population;
    }

    /**
     * {@code ((curr - prev) / prev) * 100}. When {@code prev == 0}: {@code 0}
     * if {@code curr == 0}, else {@code 100}. Matches legacy {@code
     * Form11ReportHelper#growthPercent}.
     */
    private double growthPercent(double prev, double curr) {
        if (prev == 0d) {
            return curr == 0d ? 0d : 100d;
        }
        return ((curr - prev) / prev) * 100d;
    }

    /** {@code part * 100 / total} — share of a cut among the current-period total. */
    private double sharePercent(long total, long part) {
        if (total == 0) {
            return 0d;
        }
        return (part * 100.0) / total;
    }

    private double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}
