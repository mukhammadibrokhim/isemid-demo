package uz.uzinfocom.app.platform.reference.application.manualreport.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "Детальный ответ по ручному отчёту.")
public record ManualReportResponse(

        @Schema(description = "Внутренний идентификатор ручного отчёта.", example = "1")
        Long id,

        @Schema(description = "Уникальный код ручного отчёта.", example = "TUBERCULOSIS")
        String code,

        @Schema(description = "Краткое отображаемое наименование отчёта.", example = "TB")
        String shortName,

        @Schema(description = "Наименование отчёта на узбекском языке (латиница).", example = "Sil kasalligi bo‘yicha hisobot")
        String nameUz,

        @Schema(description = "Наименование отчёта на узбекском языке (кириллица).", example = "Сил касаллиги бўйича ҳисобот")
        String nameUzCyril,

        @Schema(description = "Наименование отчёта на русском языке.", example = "Отчёт по туберкулёзу")
        String nameRu,

        @Schema(description = "Наименование отчёта на каракалпакском языке.", example = "Túberkulez esabatı")
        String nameKaa,

        @Schema(description = "Учитываются ли соответствующие диагнозы в общих итогах.", example = "true")
        Boolean includeInTotal,

        @Schema(description = "Произвольные теги типа отчёта, используемые для группировки ручных отчётов.")
        Set<String> reportTypes,

        @Schema(description = "Коды диагнозов МКБ-10, агрегируемые данным отчётом.")
        Set<String> mkb10Codes,

        @Schema(description = "Признак мягкого удаления.", example = "false")
        Boolean deleted
) {
}
