package uz.uzinfocom.app.modules.report.form11.application.export;

import java.time.LocalDate;

/**
 * Filter/request shape for the "Form 11" Excel export — mirrors {@code
 * Form11ReportController.root}'s own {@code from}/{@code to}/{@code
 * diagnosisCode}/{@code koef}/{@code population} parameters.
 */
public record Form11ExportFilter(LocalDate from, LocalDate to, String diagnosisCode, long koef, long population) {
}
