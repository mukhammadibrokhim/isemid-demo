package uz.uzinfocom.app.modules.report.form13.application.export;

import java.time.LocalDate;

/**
 * Filter/request shape for the "Form 13 by disease" Excel export — mirrors
 * {@code Form13ByDiseaseReportController.root}'s {@code from}/{@code to}.
 */
public record Form13ByDiseaseExportFilter(LocalDate from, LocalDate to) {
}
