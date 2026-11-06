package uz.uzinfocom.app.modules.report.form281.application.export;

import java.time.LocalDate;

/**
 * Filter/request shape for the "Form 28.1 by territory" Excel export —
 * mirrors {@code Form281ByTerritoryReportController.root}'s own {@code
 * from}/{@code to}.
 */
public record Form281ByTerritoryExportFilter(LocalDate from, LocalDate to) {
}
