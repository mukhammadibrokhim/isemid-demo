package uz.uzinfocom.app.platform.iam.web.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RoleShortResponse;

import java.time.LocalDate;
import java.util.Set;

@Schema(description = "Краткая информация о пользователе для табличного списка.")
public record UserTableResponse(
        @Schema(description = "Уникальный идентификатор записи.", example = "1")
        Long id,
        @Schema(description = "Имя пользователя.")
        String firstName,
        @Schema(description = "Фамилия пользователя.")
        String lastName,
        @Schema(description = "Отчество пользователя.")
        String middleName,
        @Schema(description = "ННУЗБ пользователя.")
        String nnuzb,
        @Schema(description = "Дата рождения пользователя.", example = "1990-01-01")
        LocalDate birthDate,
        @Schema(description = "Номер телефона пользователя.")
        String phoneNumber,
        @Schema(description = "Глобальные роли пользователя, если поддерживаются для обратной совместимости.")
        Set<RoleShortResponse> roles,
        @Schema(description = "Признак активности пользователя.", example = "true")
        Boolean active
) {
}
