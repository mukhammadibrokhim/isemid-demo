package uz.uzinfocom.app.modules.report.form9.application.export;

import java.time.LocalDate;

/**
 * Filter/request shape for the "Form 9" Excel export — mirrors {@code
 * Form9ReportController.root}'s own {@code from}/{@code to}/{@code
 * diagnosisCode} parameters. Only the geography tree (not the per-node
 * monthly breakdown) is exported.
 */
public record Form9ExportFilter(LocalDate from, LocalDate to, String diagnosisCode) {
}
