package uz.uzinfocom.app.platform.dashboard.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Наиболее частый диагноз (код МКБ-10) среди случаев форм №058 и №058-1 вместе.")
public record TopDiagnosisResponse(
        @Schema(description = "Код диагноза по МКБ-10.")
        String mkb10Code,

        @Schema(description = "Суммарное количество случаев с данным кодом диагноза (форма №058 + форма №058-1).")
        long count
) {
}
