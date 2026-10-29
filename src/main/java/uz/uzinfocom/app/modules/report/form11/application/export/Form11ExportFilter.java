package uz.uzinfocom.app.modules.report.form11.application.export;

import uz.uzinfocom.app.modules.report.shared.ReportPeriod;

/**
 * Filter/request shape for the "Form 11" Excel export — mirrors {@code
 * Form11ReportController.root}'s own {@code year}/{@code period}/{@code
 * diagnosisCode}/{@code koef} parameters (already defaulted by the
 * controller before this is built, same as {@code root}/{@code children}).
 * Matches {@code Form10ExportFilter} exactly.
 */
public record Form11ExportFilter(int year, ReportPeriod period, String diagnosisCode, long koef) {
}
