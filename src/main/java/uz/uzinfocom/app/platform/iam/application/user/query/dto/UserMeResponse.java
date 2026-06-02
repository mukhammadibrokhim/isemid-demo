package uz.uzinfocom.app.platform.iam.application.user.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RoleResponse;

import java.util.List;
import java.util.UUID;

@Schema(description = "Информация о текущем пользователе, его ролях и правах доступа.")
public record UserMeResponse(
        @Schema(description = "Уникальный идентификатор пользователя.", example = "1")
        Long id,

        @Schema(description = "Уникальный UUID пользователя.")
        UUID uuid,

        @Schema(description = "Имя пользователя.")
        String firstName,

        @Schema(description = "Фамилия пользователя.")
        String lastName,

        @Schema(description = "Отчество пользователя.")
        String middleName,

        @Schema(description = "Признак активности пользователя.", example = "true")
        Boolean active,

        List<RoleResponse> roles
) {
}