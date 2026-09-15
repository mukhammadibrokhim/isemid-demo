package uz.uzinfocom.app.modules.report.analytic.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.report.analytic.infrastructure.persistence.repository.dto.AnalyticReportDiagnosisCountProjection;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Confirmed-case count per ICD-10 code for the "Analitik hisobot" preview —
 * structurally a trimmed-down clone of {@code Form281ReportRepository}: the
 * same {@code form058}/{@code form058_1} union, confirmed only ({@code status
 * = 'APPROVED'}, {@code deleted = false}), the diagnosis is the confirmed
 * final code alone ({@code final_icd10_code}, no fallback to the initial
 * {@code icd10_code} — the repo-wide confirmed-count rule), organization ids
 * passed as an inlined {@code VALUES} join rather than a bound collection
 * (see {@code Form1ReportRepository} for why). Unlike Form 28.1 there is no
 * age/gender/rural breakdown here — just one total per code, restricted to
 * the caller's selected code set.
 */
@Repository
@RequiredArgsConstructor
public class AnalyticReportCountRepository {

    private static final String UNION_SOURCE_TEMPLATE = """
            select upper(f.final_icd10_code) as diagnosis_code
            from form058 f
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status = 'APPROVED'
              and f.final_icd10_code is not null
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            union all
            select upper(f.final_icd10_code)
            from form058_1 f
            join (values %1$s) as scope_org(id) on scope_org.id = f.sender_organization_id
            where f.deleted = false
              and f.status = 'APPROVED'
              and f.final_icd10_code is not null
              and f.created_at >= (:fromInclusive)::timestamptz and f.created_at < (:toExclusive)::timestamptz
            """;

    private final EntityManager entityManager;

    /** One count row per selected ICD-10 code, restricted to {@code organizationIds} (one report region's scope). */
    public List<AnalyticReportDiagnosisCountProjection> countGroupedByDiagnosisCodeForCodes(
            List<Long> organizationIds, Instant fromInclusive, Instant toExclusive, Collection<String> codes
    ) {
        if (organizationIds == null || organizationIds.isEmpty() || codes == null || codes.isEmpty()) {
            return List.of();
        }

        String sql = "select t.diagnosis_code as diagnosis_code, count(*) as cnt "
                + "from (" + UNION_SOURCE_TEMPLATE.formatted(valuesList(organizationIds)) + ") t "
                + "where t.diagnosis_code in (:codes) group by t.diagnosis_code";

        List<?> rows = entityManager.createNativeQuery(sql)
                .setParameter("fromInclusive", fromInclusive)
                .setParameter("toExclusive", toExclusive)
                .setParameter("codes", codes)
                .getResultList();

        return rows.stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new AnalyticReportDiagnosisCountProjection((String) r[0], ((Number) r[1]).longValue());
                })
                .toList();
    }

    private String valuesList(List<Long> organizationIds) {
        return organizationIds.stream().map(id -> "(" + id + ")").collect(Collectors.joining(","));
    }
}
