package uz.uzinfocom.isemid.features.report.utils;

import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.report.dtos.Form8ReportDto;

import java.util.Locale;
import java.util.Map;

import static org.springframework.util.StringUtils.hasText;

@Component
public class Form8ReportHelper {

    private static final String CATEGORY_NO_ORGANIZED = "NO_ORGANIZED";
    private static final String CATEGORY_NOT_ORGANIZED = "NOT_ORGANIZED";
    private static final String CATEGORY_ORGANIZED = "ORGANIZED";
    private static final String CATEGORY_STUDENT_SCHOOL = "STUDENT_SCHOOL";
    private static final String CATEGORY_MIDDLE_STUDENT = "MIDDLE_STUDENT";
    private static final String CATEGORY_SECONDARY_STUDENT = "SECONDARY_STUDENT";
    private static final String CATEGORY_STUDENT = "STUDENT";
    private static final String CATEGORY_WORKER = "WORKER";
    private static final String CATEGORY_MEDICAL_WORKER = "MEDICAL_WORKER";
    private static final String CATEGORY_NOT_EMPLOYED = "NOT_EMPLOYED";
    private static final String CATEGORY_PENSIONER = "PENSIONER";
    private static final String CATEGORY_UNSHELTRED = "UNSHELTRED";
    private static final String CATEGORY_UNSHELTERED = "UNSHELTERED";
    private static final String CATEGORY_STAFF = "STAFF";

    // ============================================================
    // REPORT BUILDING
    // ============================================================

    /**
     * Native SQL orqali agregatsiya qilingan (categoryCode -> son) xaritasidan
     * Form8ReportDto quradi (masalan viloyat/shahar kesimida hisoblangan sonlar).
     */
    public Form8ReportDto buildReportFromCategoryCounts(
            String name,
            Map<String, Long> countsByCategoryCode
    ) {
        return toDto(name, calculateStatisticsFromCounts(countsByCategoryCode));
    }

    private Form8ReportDto toDto(String name, CategoryStatistics statistics) {
        return Form8ReportDto.builder()
                .name(hasText(name) ? name : "-")
                .unorganized(statistics.unorganized())
                .organized(statistics.organized())
                .pupils(statistics.pupils())
                .secondaryInstitutions(
                        statistics.secondaryInstitutions()
                )
                .institutions(statistics.institutions())
                .employee(statistics.employee())
                .medEmployee(statistics.medEmployee())
                .unEmployee(statistics.unEmployee())
                .pensioners(statistics.pensioners())
                .homelessPeople(statistics.homelessPeople())
                .total(statistics.total())
                .staff(statistics.staff())
                .build();
    }

    /**
     * Har bir categoryCode uchun oldindan hisoblangan sonni tegishli
     * statistik ustunga qo‘shadi.
     */
    private CategoryStatistics calculateStatisticsFromCounts(
            Map<String, Long> countsByCategoryCode
    ) {
        long total = 0L;
        long unorganized = 0L;
        long organized = 0L;
        long pupils = 0L;
        long secondaryInstitutions = 0L;
        long institutions = 0L;
        long employee = 0L;
        long medEmployee = 0L;
        long unEmployee = 0L;
        long pensioners = 0L;
        long homelessPeople = 0L;
        long explicitStaff = 0L;

        if (countsByCategoryCode != null) {
            for (Map.Entry<String, Long> entry : countsByCategoryCode.entrySet()) {
                long count = entry.getValue() == null ? 0L : entry.getValue();
                total += count;

                String categoryCode = normalizeCategoryCode(entry.getKey());

                if (!hasText(categoryCode)) {
                    continue;
                }

                switch (categoryCode) {
                    case CATEGORY_NO_ORGANIZED,
                         CATEGORY_NOT_ORGANIZED -> unorganized += count;

                    case CATEGORY_ORGANIZED -> organized += count;

                    case CATEGORY_STUDENT_SCHOOL -> pupils += count;

                    case CATEGORY_MIDDLE_STUDENT,
                         CATEGORY_SECONDARY_STUDENT -> secondaryInstitutions += count;

                    case CATEGORY_STUDENT -> institutions += count;

                    case CATEGORY_WORKER -> employee += count;

                    case CATEGORY_MEDICAL_WORKER -> medEmployee += count;

                    case CATEGORY_NOT_EMPLOYED -> unEmployee += count;

                    case CATEGORY_PENSIONER -> pensioners += count;

                    case CATEGORY_UNSHELTRED,
                         CATEGORY_UNSHELTERED -> homelessPeople += count;

                    case CATEGORY_STAFF -> explicitStaff += count;

                    default -> {
                    }
                }
            }
        }

        long staff = explicitStaff + employee + medEmployee;

        return new CategoryStatistics(
                unorganized,
                organized,
                pupils,
                secondaryInstitutions,
                institutions,
                employee,
                medEmployee,
                unEmployee,
                pensioners,
                homelessPeople,
                total,
                staff
        );
    }

    // ============================================================
    // NORMALIZATION
    // ============================================================

    private String normalizeCategoryCode(String value) {
        if (!hasText(value)) {
            return "";
        }

        return value
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }

    // ============================================================
    // INTERNAL MODELS
    // ============================================================

    private record CategoryStatistics(
            long unorganized,
            long organized,
            long pupils,
            long secondaryInstitutions,
            long institutions,
            long employee,
            long medEmployee,
            long unEmployee,
            long pensioners,
            long homelessPeople,
            long total,
            long staff
    ) {
    }
}
