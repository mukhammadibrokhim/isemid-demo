package uz.uzinfocom.app.modules.card.web.dto.request.card161;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Детальные сведения об источнике заражения — заполняется, если источник не найден "
        + "либо им является животное.")
public record InfectionSourceDetailRequest(
        @Schema(description = "Код причины, по которой источник заражения не был найден (по справочнику).")
        @Size(max = 64) String infectionSourceNotFoundCode,

        @Schema(description = "ФИО лица — установленного источника заражения.")
        @Size(max = 500) String personFullName,

        @Schema(description = "Код периода заболевания источника на момент заражения (по справочнику).")
        @Size(max = 64) String infectionSourceDiseasePeriodCode,

        @Schema(description = "Код вида животного — источника заражения (по справочнику).")
        @Size(max = 64) String animalTypeCode
) {
}
