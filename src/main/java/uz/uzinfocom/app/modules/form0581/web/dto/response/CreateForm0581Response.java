package uz.uzinfocom.app.modules.form0581.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.form0581.domain.enums.Form0581Status;

import java.util.UUID;

@Schema(description = "Результат создания формы №058-1.")
public record CreateForm0581Response(
        @Schema(description = "Идентификатор созданной формы.")
        Long id,

        @Schema(description = "UUID созданной формы.")
        UUID uuid,

        @Schema(description = "Статус формы после создания.")
        Form0581Status status
) {
}
