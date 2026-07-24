package uz.uzinfocom.app.modules.card.web.dto.request.card161;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

import java.time.LocalDateTime;

@Schema(description = "Сведения о проведении экстренной профилактической/антирабической помощи пациенту.")
public record EmergencyProphylaxisRequest(
        @Schema(description = "Идентификатор записи. Не указывается (null) при добавлении новой записи; "
                + "указывается для обновления уже существующей.")
        Long id,

        @Schema(description = "Дата и время проведения помощи.")
        LocalDateTime treatmentDate,

        @Schema(description = "Наименование препарата.")
        @Size(max = 255) String drugName,

        @Schema(description = "Доза препарата.")
        @Size(max = 100) String dose,

        @Schema(description = "Серия препарата.")
        @Size(max = 100) String serialNumber,

        @Schema(description = "График проведения.")
        @Size(max = 255) String administrationSchedule
) implements ChildRequest {
}
