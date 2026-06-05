package uz.uzinfocom.app.platform.scope.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.scope.OrganizationScopeMode;

import java.util.UUID;

@Schema(description = "Текущая область доступа пользователя по выбранной организации.")
public record CurrentScopeResponse(
        @Schema(description = "Режим организационного доступа.")
        OrganizationScopeMode mode,
        @Schema(description = "UUID выбранной организации.")
        UUID organizationUuid,
        @Schema(description = "Код региона, доступного пользователю.")
        String regionCode,
        @Schema(description = "Код района или города, доступного пользователю.")
        String districtCode
) {
}
