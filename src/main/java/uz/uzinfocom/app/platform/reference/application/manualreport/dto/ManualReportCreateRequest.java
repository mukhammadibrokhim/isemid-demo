package uz.uzinfocom.app.platform.reference.application.manualreport.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Schema(description = "Данные для создания записи ручного отчёта.")
public record ManualReportCreateRequest(
        @Schema(
                description = "Уникальный код ручного отчёта.",
                example = "TUBERCULOSIS",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "{reference.code.required}")
        @Size(max = 50, message = "{reference.code.max_length}")
        String code,

        @Schema(description = "Краткое отображаемое наименование отчёта.", example = "TB")
        @Size(max = 100, message = "{reference.manual_report.short_name.size}")
        String shortName,

        @Schema(description = "Наименование отчёта на узбекском языке (латиница).", example = "Sil kasalligi bo‘yicha hisobot")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUz,

        @Schema(description = "Наименование отчёта на узбекском языке (кириллица).", example = "Сил касаллиги бўйича ҳисобот")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUzCyril,

        @Schema(description = "Наименование отчёта на русском языке.", example = "Отчёт по туберкулёзу")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameRu,

        @Schema(description = "Наименование отчёта на каракалпакском языке.", example = "Túberkulez esabatı")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameKaa,

        @Schema(description = "Учитываются ли соответствующие диагнозы в общих итогах.", example = "true")
        Boolean includeInTotal,

        @Schema(description = "Произвольные теги типа отчёта, используемые для группировки ручных отчётов.")
        Set<String> reportTypes,

        @Schema(
                description = "Коды диагнозов МКБ-10, агрегируемые данным отчётом. Требуется хотя бы один код.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotEmpty(message = "{reference.manual_report.mkb10_codes.required}")
        Set<String> mkb10Codes
) {
}
