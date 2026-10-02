package uz.uzinfocom.app.modules.report.form12.application.export;

import java.time.LocalDate;

/**
 * Filter/request shape for the "Form 12" (disease-first) Excel export —
 * mirrors {@code Form12ReportController.root}'s own {@code from}/{@code to}
 * parameters, bundled into one record since {@link
 * uz.uzinfocom.app.platform.export.application.ExcelExportSource} needs a
 * single filter type.
 */
public record Form12ExportFilter(LocalDate from, LocalDate to) {
}
