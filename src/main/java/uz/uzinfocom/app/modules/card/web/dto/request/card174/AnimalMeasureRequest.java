package uz.uzinfocom.app.modules.card.web.dto.request.card174;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

import java.time.LocalDate;

@Schema(description = "Мера, применённая к животным в очаге.")
public record AnimalMeasureRequest(
        @Schema(description = "Идентификатор записи. Не указывается (null) при добавлении новой записи; "
                + "указывается для обновления уже существующей.")
        Long id,

        @Schema(description = "Наименование меры.")
        @Size(max = 500) String measureName,

        @Schema(description = "Дата проведения меры.")
        LocalDate measureDate,

        @Schema(description = "Количество проведённых мер.")
        Integer measureCount,

        @Schema(description = "Место проведения меры.")
        @Size(max = 500) String measureLocation
) implements ChildRequest {
}
