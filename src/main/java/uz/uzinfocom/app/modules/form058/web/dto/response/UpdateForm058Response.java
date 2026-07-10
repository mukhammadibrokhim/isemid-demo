package uz.uzinfocom.app.modules.form058.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;

import java.util.UUID;

@Schema(description = "Результат изменения статуса/данных формы №058 (обновление, утверждение, отклонение, аннулирование).")
public record UpdateForm058Response(
        @Schema(description = "Идентификатор формы.")
        Long id,

        @Schema(description = "UUID формы.")
        UUID uuid,

        @Schema(description = "Текущий статус формы после операции.")
        FormStatus status
) {
}
