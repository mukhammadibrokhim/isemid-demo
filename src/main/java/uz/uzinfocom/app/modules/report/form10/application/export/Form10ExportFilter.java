package uz.uzinfocom.app.modules.report.form10.application.export;

import uz.uzinfocom.app.modules.report.shared.ReportPeriod;

/**
 * Filter/request shape for the "Form 10" Excel export — mirrors {@code
 * Form10ReportController.root}'s own {@code year}/{@code period}/{@code
 * diagnosisCode}/{@code koef} parameters (already defaulted by the
 * controller before this is built, same as {@code root}/{@code children}).
 */
public record Form10ExportFilter(int year, ReportPeriod period, String diagnosisCode, long koef) {
}
