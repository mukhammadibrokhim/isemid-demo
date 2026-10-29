package uz.uzinfocom.app.modules.report.form282.application.export;

import java.time.LocalDate;

/**
 * Filter/request shape for the "Form 28.2" (disease-first) Excel export —
 * mirrors {@code Form282ReportController.root}'s {@code from}/{@code to}.
 */
public record Form282ExportFilter(LocalDate from, LocalDate to) {
}
