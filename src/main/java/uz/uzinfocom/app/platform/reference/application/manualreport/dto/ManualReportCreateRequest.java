package uz.uzinfocom.app.platform.reference.application.manualreport.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Schema(description = "Request payload for creating a Manual Report reference record.")
public record ManualReportCreateRequest(
        @Schema(
                description = "Unique Manual Report code.",
                example = "TUBERCULOSIS",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "{reference.code.required}")
        @Size(max = 50, message = "{reference.code.max_length}")
        String code,

        @Schema(description = "Short display name for the report.", example = "TB")
        @Size(max = 100, message = "{reference.manual_report.short_name.size}")
        String shortName,

        @Schema(description = "Report name in Uzbek Latin.", example = "Sil kasalligi bo‘yicha hisobot")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUz,

        @Schema(description = "Report name in Uzbek Cyrillic.", example = "Сил касаллиги бўйича ҳисобот")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUzCyril,

        @Schema(description = "Report name in Russian.", example = "Отчёт по туберкулёзу")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameRu,

        @Schema(description = "Report name in Karakalpak.", example = "Túberkulez esabatı")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameKaa,

        @Schema(description = "Whether matching diagnoses count toward aggregate totals.", example = "true")
        Boolean includeInTotal,

        @Schema(description = "Free-form report type tags used to group manual reports.")
        Set<String> reportTypes,

        @Schema(
                description = "ICD-10 (MKB-10) diagnosis codes this report aggregates. At least one is required.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotEmpty(message = "{reference.manual_report.mkb10_codes.required}")
        Set<String> mkb10Codes
) {
}
