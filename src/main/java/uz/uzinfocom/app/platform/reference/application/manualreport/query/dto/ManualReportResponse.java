package uz.uzinfocom.app.platform.reference.application.manualreport.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "Detailed Manual Report reference response.")
public record ManualReportResponse(

        @Schema(description = "Manual Report internal identifier.", example = "1")
        Long id,

        @Schema(description = "Unique Manual Report code.", example = "TUBERCULOSIS")
        String code,

        @Schema(description = "Short display name for the report.", example = "TB")
        String shortName,

        @Schema(description = "Report name in Uzbek Latin.", example = "Sil kasalligi bo‘yicha hisobot")
        String nameUz,

        @Schema(description = "Report name in Uzbek Cyrillic.", example = "Сил касаллиги бўйича ҳисобот")
        String nameUzCyril,

        @Schema(description = "Report name in Russian.", example = "Отчёт по туберкулёзу")
        String nameRu,

        @Schema(description = "Report name in Karakalpak.", example = "Túberkulez esabatı")
        String nameKaa,

        @Schema(description = "Whether matching diagnoses count toward aggregate totals.", example = "true")
        Boolean includeInTotal,

        @Schema(description = "Free-form report type tags used to group manual reports.")
        Set<String> reportTypes,

        @Schema(description = "ICD-10 (MKB-10) diagnosis codes this report aggregates.")
        Set<String> mkb10Codes,

        @Schema(description = "Soft-delete flag.", example = "false")
        Boolean deleted
) {
}
