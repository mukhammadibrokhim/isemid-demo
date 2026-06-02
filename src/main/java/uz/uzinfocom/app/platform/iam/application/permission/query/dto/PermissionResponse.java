package uz.uzinfocom.app.platform.iam.application.permission.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "Право доступа пользователя или роли.")
public record PermissionResponse(
        @Schema(description = "Уникальный идентификатор записи.", example = "1")
        Long id,
        @Schema(description = "Субъект права доступа.", example = "users")
        String subject,
        @Schema(description = "Список разрешенных действий.", example = "[\"VIEW\", \"CREATE\"]")
        Set<String> actions
) {
}
