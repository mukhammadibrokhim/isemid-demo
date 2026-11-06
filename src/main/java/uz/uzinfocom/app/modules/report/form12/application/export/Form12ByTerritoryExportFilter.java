package uz.uzinfocom.app.modules.report.form12.application.export;

import java.time.LocalDate;

/**
 * Filter/request shape for the "Form 12 by territory" Excel export — mirrors
 * {@code Form12ByTerritoryReportController.root}'s own {@code from}/{@code
 * to} parameters.
 */
public record Form12ByTerritoryExportFilter(LocalDate from, LocalDate to) {
}
