package uz.uzinfocom.app.modules.form058.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Диагностические сведения формы №058.")
public record Form058DiagnosisDetailResponse(
        @Schema(description = "Код первичного диагноза по МКБ-10.")
        String mkb10Code,

        @Schema(description = "Наименование первичного диагноза по МКБ-10.")
        String mkb10Name,

        @Schema(description = "Итоговый (окончательный) код диагноза по МКБ-10, установленный при утверждении формы.")
        String finalMkb10Code,

        @Schema(description = "Итоговое (окончательное) наименование диагноза по МКБ-10, установленное при утверждении формы.")
        String finalMkb10Name
) {
}
