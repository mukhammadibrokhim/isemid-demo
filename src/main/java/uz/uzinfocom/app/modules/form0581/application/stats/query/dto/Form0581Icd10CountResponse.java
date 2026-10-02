package uz.uzinfocom.app.modules.form0581.application.stats.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Количество форм №058-1 по коду диагноза МКБ-10.")
public record Form0581Icd10CountResponse(
        @Schema(description = "Код диагноза по МКБ-10.")
        String icd10Code,

        @Schema(description = "Количество форм с данным кодом диагноза.")
        long count
) {
}
