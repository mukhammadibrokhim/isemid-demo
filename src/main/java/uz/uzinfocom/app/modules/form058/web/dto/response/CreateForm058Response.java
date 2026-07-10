package uz.uzinfocom.app.modules.form058.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;

import java.util.UUID;

@Schema(description = "Результат создания формы №058.")
public record CreateForm058Response(
        @Schema(description = "Идентификатор созданной формы.")
        Long id,

        @Schema(description = "UUID созданной формы.")
        UUID uuid,

        @Schema(description = "Статус формы после создания.")
        FormStatus status
) {
}
