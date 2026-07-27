package uz.uzinfocom.app.modules.report.form1.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Count breakdown shape specific to "Form 1" (see {@code
 * Form1ReportQueryService}). Each report under {@code modules.report}
 * aggregates different source data with its own breakdown columns — this
 * shape is not assumed to be reused by other reports. {@code under14}/
 * {@code under18}/{@code adult}/{@code female} are independent, overlapping
 * cuts of {@code total} (not a mutually exclusive partition), matching this
 * report's own source table.
 */
@Schema(description = "Возрастно-половая разбивка количества случаев.")
public record Form1CountBlockResponse(
        @Schema(description = "Всего случаев.")
        long total,

        @Schema(description = "Дети до 14 лет.")
        long under14,

        @Schema(description = "Дети до 18 лет.")
        long under18,

        @Schema(description = "Взрослые (18 лет и старше).")
        long adult,

        @Schema(description = "Женщины.")
        long female
) {
    public static final Form1CountBlockResponse EMPTY = new Form1CountBlockResponse(0, 0, 0, 0, 0);
}
