package uz.uzinfocom.app.platform.dashboard.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Количество случаев (форма №058 + форма №058-1) по источнику поступления, за всё время.")
public record SourceCountResponse(
        @Schema(description = "Источник поступления.")
        String source,

        @Schema(description = "Количество случаев с данным источником.")
        long count
) {
}
