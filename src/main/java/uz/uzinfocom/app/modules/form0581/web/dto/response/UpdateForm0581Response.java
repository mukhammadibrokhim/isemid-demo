package uz.uzinfocom.app.modules.form0581.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.form0581.domain.enums.Form0581Status;

import java.util.UUID;

@Schema(description = "Результат изменения статуса/данных формы №058-1 "
        + "(обновление, утверждение, отклонение, аннулирование).")
public record UpdateForm0581Response(
        @Schema(description = "Идентификатор формы.")
        Long id,

        @Schema(description = "UUID формы.")
        UUID uuid,

        @Schema(description = "Текущий статус формы после операции.")
        Form0581Status status
) {
}
