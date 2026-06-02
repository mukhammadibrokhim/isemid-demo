package uz.uzinfocom.app.platform.iam.web.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.OrganizationShortResponse;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RoleShortResponse;
import uz.uzinfocom.app.platform.iam.application.user.query.dto.UserOrganizationRolesResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Детальная информация о пользователе.")
public record UserDetailedResponse(
        @Schema(description = "Уникальный идентификатор записи.", example = "1")
        Long id,
        @Schema(description = "Уникальный UUID записи.")
        UUID uuid,
        @Schema(description = "Признак активности пользователя.", example = "true")
        Boolean active,
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
        @Schema(description = "Код региона пользователя.")
        String stateCode,
        @Schema(description = "Код района или города пользователя.")
        String cityCode,
        @Schema(description = "Адрес пользователя.")
        String line,
        @Schema(description = "Код пола пользователя.")
        String genderCode,
        @Schema(description = "Организации пользователя.")
        Set<OrganizationShortResponse> organizations,
        @Schema(description = "Глобальные роли пользователя, если поддерживаются для обратной совместимости.")
        Set<RoleShortResponse> roles,
        @Schema(description = "Роли и права пользователя в разрезе организаций.")
        List<UserOrganizationRolesResponse> organizationRoles
) {
}
