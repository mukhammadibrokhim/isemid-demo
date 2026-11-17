package uz.uzinfocom.app.modules.report.form1.application.export;

import java.time.LocalDate;

/**
 * Filter/request shape for the "Form 1" Excel export — mirrors {@code
 * Form1ReportController.root}'s own {@code from}/{@code to}/{@code
 * diagnosisCode} parameters.
 */
public record Form1ExportFilter(LocalDate from, LocalDate to, String diagnosisCode) {
}
