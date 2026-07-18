package uz.uzinfocom.app.modules.form0581.application.stats.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Количество форм №058-1 по источнику поступления.")
public record Form0581SourceCountResponse(
        @Schema(description = "Источник поступления формы.")
        String source,

        @Schema(description = "Количество форм с данным источником.")
        long count
) {
}
