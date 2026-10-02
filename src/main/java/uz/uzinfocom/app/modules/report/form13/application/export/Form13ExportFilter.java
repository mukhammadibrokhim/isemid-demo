package uz.uzinfocom.app.modules.report.form13.application.export;

import java.time.LocalDate;

/**
 * Filter/request shape for the "Form 13" (geography-first) Excel export —
 * mirrors {@code Form13ReportController.root}'s own {@code from}/{@code to}.
 */
public record Form13ExportFilter(LocalDate from, LocalDate to) {
}
