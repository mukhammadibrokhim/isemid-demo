package uz.uzinfocom.app.modules.card.web.dto.request.card174;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

import java.time.LocalDate;

@Schema(description = "Лабораторное исследование (строка таблицы лабораторных исследований).")
public record LaboratoryTestRequest(
        @Schema(description = "Идентификатор записи. Не указывается (null) при добавлении новой записи; "
                + "указывается для обновления уже существующей.")
        Long id,

        @Schema(description = "Вид животного и наименование исследованной пробы.")
        @Size(max = 255) String sampleName,

        @Schema(description = "Дата проведения лабораторного исследования.")
        LocalDate testDate,

        @Schema(description = "Количество отобранных проб для исследования.")
        Integer sampleCount,

        @Schema(description = "Метод проведённого исследования.")
        @Size(max = 255) String testingMethod,

        @Schema(description = "Результат проведённого исследования.")
        @Size(max = 500) String testResult
) implements ChildRequest {
}
