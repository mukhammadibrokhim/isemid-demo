package uz.uzinfocom.app.platform.iam.web.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.OrganizationShortResponse;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RoleShortResponse;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Подробная информация о пользователе.")
public record UserDetailedResponse(

        @Schema(description = "Внутренний идентификатор пользователя.", example = "1")
        Long id,

        @Schema(
                description = "Уникальный идентификатор пользователя.",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID uuid,

        @Schema(description = "Имя пользователя или логин.", example = "user.login")
        String username,

        @Schema(description = "Признак активности пользователя.", example = "true")
        Boolean active,

        @Schema(description = "Имя пользователя.", example = "Алексей")
        String firstName,

        @Schema(description = "Фамилия пользователя.", example = "Иванов")
        String lastName,

        @Schema(description = "Отчество пользователя.", example = "Сергеевич")
        String middleName,

        @Schema(description = "Идентификатор NNUZB.", example = "998901234567")
        String nnuzb,

        @Schema(description = "Дата рождения пользователя.", example = "1990-01-01")
        LocalDate birthDate,

        @Schema(description = "Номер телефона пользователя.", example = "+998901234567")
        String phoneNumber,

        @Schema(description = "Код региона пользователя.", example = "UZ-AN")
        String regionCode,

        @Schema(description = "Код района пользователя.", example = "17-220")
        String districtCode,

        @Schema(description = "Адрес пользователя.", example = "Улица Амира Темура, дом 10")
        String line,

        @Schema(description = "Код пола пользователя.", example = "male")
        String genderCode,

        @Schema(description = "Информация о создании и последнем изменении записи.")
        AuditResponse audit,

        @Schema(description = "Организации, к которым относится пользователь.")
        Set<OrganizationShortResponse> organizations,

        @Schema(description = "Глобальные роли, назначенные пользователю.")
        Set<RoleShortResponse> roles
) {
}
