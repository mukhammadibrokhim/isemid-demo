package uz.uzinfocom.app.modules.card.application.query.dto.detail.card174;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Мера, применённая к животным в очаге.")
public record AnimalMeasureResponse(
        @Schema(description = "Идентификатор записи.")
        Long id,

        @Schema(description = "Наименование меры.")
        String measureName,

        @Schema(description = "Дата проведения меры.")
        LocalDate measureDate,

        @Schema(description = "Количество проведённых мер.")
        Integer measureCount,

        @Schema(description = "Место проведения меры.")
        String measureLocation
) {
}
