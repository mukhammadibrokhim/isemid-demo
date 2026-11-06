package uz.uzinfocom.app.modules.report.form281.application.export;

import java.time.LocalDate;

/**
 * Filter/request shape for the "Form 28.1" (disease-first) Excel export —
 * mirrors {@code Form281ReportController.root}'s {@code from}/{@code to}.
 */
public record Form281ExportFilter(LocalDate from, LocalDate to) {
}
