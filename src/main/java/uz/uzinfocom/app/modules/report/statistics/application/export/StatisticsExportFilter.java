package uz.uzinfocom.app.modules.report.statistics.application.export;

import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsAgeGroup;

import java.time.LocalDate;

/**
 * Filter/request shape for the "Statistika" Excel export — mirrors {@code
 * StatisticsReportController.root}'s parameters: {@code fromA}/{@code
 * toA}/{@code fromB}/{@code toB} ("Davr A", required; optional "Davr B" for
 * comparison) plus the optional gender / age-band / social-category
 * cross-filters.
 */
public record StatisticsExportFilter(
        LocalDate fromA, LocalDate toA, LocalDate fromB, LocalDate toB,
        String genderCode, StatisticsAgeGroup ageGroup, String categoryCode
) {
}
