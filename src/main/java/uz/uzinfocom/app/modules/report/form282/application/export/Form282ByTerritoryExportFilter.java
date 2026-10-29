package uz.uzinfocom.app.modules.report.form282.application.export;

import java.time.LocalDate;

/**
 * Filter/request shape for the "Form 28.2 by territory" Excel export —
 * mirrors {@code Form282ByTerritoryReportController.root}'s own {@code
 * from}/{@code to}.
 */
public record Form282ByTerritoryExportFilter(LocalDate from, LocalDate to) {
}
