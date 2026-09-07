package uz.uzinfocom.app.modules.report.statistics.application.export;

import java.time.LocalDate;

/**
 * Filter/request shape for the "Statistika" Excel export — mirrors {@code
 * StatisticsReportController.root}'s own {@code fromA}/{@code toA}/{@code
 * fromB}/{@code toB} parameters ("Davr A", required; optional "Davr B" for
 * comparison).
 */
public record StatisticsExportFilter(LocalDate fromA, LocalDate toA, LocalDate fromB, LocalDate toB) {
}
