package uz.uzinfocom.app.modules.report.shared;

import jakarta.persistence.Query;
import uz.uzinfocom.app.modules.reference.application.manualreport.query.dto.ManualReportResponse;
import uz.uzinfocom.app.modules.reference.domain.ManualReportAgeUnit;
import uz.uzinfocom.app.modules.reference.domain.ManualReportGender;
import uz.uzinfocom.app.modules.reference.domain.ManualReportCaseSource;
import uz.uzinfocom.app.modules.reference.domain.ManualReportPopulationType;

import java.util.Set;

/**
 * The restriction of one manual-report catalog row (gender, age range
 * {@code [ageFrom, ageTo)}, source form, lab confirmation, urban/rural residence,
 * hospitalization, disease place), applied inside the native SQL of the catalog-driven
 * reports (Form 12/13/28.1/28.2). It has to run in SQL: two rows may share
 * ICD-10 codes yet restrict them differently, so counts already grouped by code
 * cannot be filtered afterwards. Rows with an equal condition share one query.
 * <p>
 * The native queries must alias the case table {@code f} and the patient table
 * {@code p}. Age is measured on the case's {@code created_at} date. A restricted
 * row skips patients whose gender / birth date it needs is unknown. Only fixed
 * SQL text is spliced in; every value is a bound parameter.
 */
public record ReportCaseCondition(
        ManualReportGender gender,
        Integer ageFrom,
        ManualReportAgeUnit ageFromUnit,
        Integer ageTo,
        ManualReportAgeUnit ageToUnit,
        ManualReportCaseSource caseSource,
        Boolean labConfirmation,
        ManualReportPopulationType populationType,
        Boolean hospitalized,
        Set<String> diseasePlaceCodes
) {

    /** An empty set means "no restriction", the same as {@code null} - they must share one query. */
    public ReportCaseCondition {
        diseasePlaceCodes = diseasePlaceCodes == null || diseasePlaceCodes.isEmpty()
                ? null
                : Set.copyOf(diseasePlaceCodes);
    }

    /** No restriction - the SQL stays exactly as it is without a condition. */
    public static final ReportCaseCondition NONE = new ReportCaseCondition(
            null, null, null, null, null, null, null, null, null, null
    );

    public static ReportCaseCondition of(ManualReportResponse entry) {
        return new ReportCaseCondition(
                entry.gender(), entry.ageFrom(), entry.ageFromUnit(), entry.ageTo(), entry.ageToUnit(),
                entry.caseSource(), entry.labConfirmation(), entry.populationType(), entry.hospitalized(),
                entry.diseasePlaceCodes()
        );
    }

    /**
     * {@code and ...} lines to append to the {@code where} block that reads {@code table};
     * empty for {@link #NONE}. A table the row excludes gets {@code and false} on top of the
     * other lines, so every bound parameter stays present in the statement.
     */
    public String sqlClause(ManualReportCaseSource table) {
        StringBuilder sql = new StringBuilder();
        if (caseSource != null && caseSource != table) {
            sql.append(" and false\n");
        }
        if (labConfirmation != null) {
            // Only form058 has the column.
            sql.append(table == ManualReportCaseSource.FORM_058
                    ? " and f.lab_confirmation = :conditionLabConfirmation\n"
                    : " and false\n");
        }
        if (hospitalized != null) {
            // The same "hospitalized" definition Form 9 uses for each table.
            sql.append(table == ManualReportCaseSource.FORM_058 ? " and f.hospital_place_id" : " and f.hospitalized_at")
                    .append(hospitalized ? " is not null\n" : " is null\n");
        }
        if (diseasePlaceCodes != null) {
            // Only form058 has the column.
            sql.append(table == ManualReportCaseSource.FORM_058
                    ? " and f.disease_place_code in (:conditionDiseasePlaces)\n"
                    : " and false\n");
        }
        if (populationType != null) {
            sql.append(" and upper(trim(p.population_type_code)) = :conditionPopulationType\n");
        }
        if (gender != null) {
            sql.append(" and upper(trim(p.gender_code)) = :conditionGender\n");
        }
        if (ageFrom != null || ageTo != null) {
            sql.append(" and p.birth_date <= f.created_at::date\n");
        }
        if (ageFrom != null) {
            sql.append(" and f.created_at::date >= p.birth_date + (:conditionAgeFrom) * ")
                    .append(oneUnit(ageFromUnit)).append('\n');
        }
        if (ageTo != null) {
            sql.append(" and f.created_at::date < p.birth_date + (:conditionAgeTo) * ")
                    .append(oneUnit(ageToUnit)).append('\n');
        }
        return sql.toString();
    }

    /** Binds the parameters {@link #sqlClause} introduced into the two table blocks. */
    public Query bind(Query query) {
        if (gender != null) {
            query.setParameter("conditionGender", gender.name());
        }
        if (ageFrom != null) {
            query.setParameter("conditionAgeFrom", ageFrom);
        }
        if (ageTo != null) {
            query.setParameter("conditionAgeTo", ageTo);
        }
        if (labConfirmation != null) {
            query.setParameter("conditionLabConfirmation", labConfirmation);
        }
        if (diseasePlaceCodes != null) {
            query.setParameter("conditionDiseasePlaces", diseasePlaceCodes);
        }
        if (populationType != null) {
            query.setParameter("conditionPopulationType", populationType.name());
        }
        return query;
    }

    private static String oneUnit(ManualReportAgeUnit unit) {
        return switch (unit) {
            case DAY -> "interval '1 day'";
            case MONTH -> "interval '1 month'";
            case YEAR -> "interval '1 year'";
        };
    }
}
