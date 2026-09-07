package uz.uzinfocom.app.modules.report.form8.application.export;

import java.time.LocalDate;

/**
 * Filter/request shape for the "Form 8" Excel export — mirrors {@code
 * Form8ReportController.root}'s own {@code from}/{@code to}/{@code
 * diagnosisCode} parameters. Only the geography tree (not the per-node
 * category breakdown) is exported.
 */
public record Form8ExportFilter(LocalDate from, LocalDate to, String diagnosisCode) {
}
