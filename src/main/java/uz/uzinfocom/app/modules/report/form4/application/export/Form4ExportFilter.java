package uz.uzinfocom.app.modules.report.form4.application.export;

import java.time.LocalDate;

/**
 * Filter/request shape for the "Form 4" Excel export — mirrors {@code
 * Form4ReportController.root}'s own {@code from}/{@code to}/{@code
 * diagnosisCode} parameters.
 */
public record Form4ExportFilter(LocalDate from, LocalDate to, String diagnosisCode) {
}
