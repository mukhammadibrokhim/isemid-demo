package uz.uzinfocom.app.modules.form129.application.stats.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Количество форм №129 по типу заболевания (определяется по основному маркерному "
        + "лабораторному показателю — см. Form129DiseaseType).")
public record Form129DiseaseTypeCountResponse(
        @Schema(description = "Тип заболевания.")
        Form129DiseaseType diseaseType,

        @Schema(description = "Количество форм с данным типом заболевания.")
        long count
) {
}
