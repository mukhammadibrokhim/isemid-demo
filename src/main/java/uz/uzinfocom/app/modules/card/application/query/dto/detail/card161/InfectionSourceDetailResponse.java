package uz.uzinfocom.app.modules.card.application.query.dto.detail.card161;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Детальные сведения об источнике заражения — заполняется, если источник не найден "
        + "либо им является животное.")
public record InfectionSourceDetailResponse(
        @Schema(description = "Идентификатор записи.")
        Long id,

        @Schema(description = "Код причины, по которой источник заражения не был найден (по справочнику).")
        String infectionSourceNotFoundCode,

        @Schema(description = "ФИО лица — установленного источника заражения.")
        String personFullName,

        @Schema(description = "Код периода заболевания источника на момент заражения (по справочнику).")
        String infectionSourceDiseasePeriodCode,

        @Schema(description = "Код вида животного — источника заражения (по справочнику).")
        String animalTypeCode
) {
}
