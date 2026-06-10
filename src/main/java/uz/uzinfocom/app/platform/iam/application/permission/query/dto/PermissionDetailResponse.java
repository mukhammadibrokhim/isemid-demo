package uz.uzinfocom.app.platform.iam.application.permission.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;

@Schema(description = "Детальная информация о праве доступа.")
public record PermissionDetailResponse(
        @Schema(description = "Уникальный идентификатор записи.", example = "1")
        Long id,
        @Schema(description = "Субъект права доступа.", example = "users")
        String subject,
        @Schema(description = "Описание права доступа на узбекском языке.")
        String descriptionUz,
        @Schema(description = "Описание права доступа на русском языке.")
        String descriptionRu,
        @Schema(description = "Описание права доступа на узбекском языке кириллицей.")
        String descriptionUzCyril,
        @Schema(description = "Описание права доступа на каракалпакском языке.")
        String descriptionKaa,
        @Schema(description = "Информация об аудите разрешения.")
        AuditResponse audit
) {
}
