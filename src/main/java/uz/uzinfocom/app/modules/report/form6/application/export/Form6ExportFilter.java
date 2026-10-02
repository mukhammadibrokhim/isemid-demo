package uz.uzinfocom.app.modules.report.form6.application.export;

import java.time.LocalDate;

/**
 * Filter/request shape for the "Form 6" Excel export — mirrors {@code
 * Form6ReportController.root}'s own {@code from}/{@code to}/{@code
 * diagnosisCode} parameters. Only the geography tree (not the per-node age
 * breakdown) is exported — see {@code Form6ExcelExportSource}'s Javadoc.
 */
public record Form6ExportFilter(LocalDate from, LocalDate to, String diagnosisCode) {
}
