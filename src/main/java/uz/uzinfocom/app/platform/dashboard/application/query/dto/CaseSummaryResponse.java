package uz.uzinfocom.app.platform.dashboard.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сводка по эпидемиологическим случаям (формы №058 и №058-1 вместе) в рамках доступа "
        + "текущей организации.")
public record CaseSummaryResponse(
        @Schema(description = "Общее количество случаев (форма №058 + форма №058-1).")
        long totalCases,

        @Schema(description = "Количество случаев, решение по которым ещё не принято (статус в процессе).")
        long activeCases,

        @Schema(description = "Количество случаев, зарегистрированных сегодня.")
        long newCasesToday,

        @Schema(description = "Количество случаев по форме №058.")
        long form058Total,

        @Schema(description = "Количество случаев по форме №058-1.")
        long form0581Total
) {
}
