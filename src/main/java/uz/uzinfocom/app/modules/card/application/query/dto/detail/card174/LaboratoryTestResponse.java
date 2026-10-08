package uz.uzinfocom.app.modules.card.application.query.dto.detail.card174;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Лабораторное исследование (строка таблицы лабораторных исследований).")
public record LaboratoryTestResponse(
        @Schema(description = "Идентификатор записи.")
        Long id,

        @Schema(description = "Вид животного и наименование исследованной пробы.")
        String sampleName,

        @Schema(description = "Дата проведения лабораторного исследования.")
        LocalDate testDate,

        @Schema(description = "Количество отобранных проб для исследования.")
        Integer sampleCount,

        @Schema(description = "Метод проведённого исследования.")
        String testingMethod,

        @Schema(description = "Результат проведённого исследования.")
        String testResult
) {
}
