package uz.uzinfocom.app.modules.reference.application.manualreport.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ручной отчёт для справочного выбора (select).")
public record ManualReportLookupResponse(
        @Schema(description = "Внутренний идентификатор ручного отчёта.", example = "1")
        Long id,
        @Schema(description = "Уникальный код ручного отчёта.", example = "TUBERCULOSIS")
        String code,
        @Schema(description = "Краткое отображаемое наименование отчёта.", example = "TB")
        String shortName,
        @Schema(description = "Наименование отчёта, локализованное по текущей локали запроса.")
        String name
) {
}
