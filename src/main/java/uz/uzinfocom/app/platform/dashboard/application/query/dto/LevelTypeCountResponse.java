package uz.uzinfocom.app.platform.dashboard.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.iam.domain.enums.OrganizationLevel;

@Schema(description = "Количество медицинских учреждений по уровню организации (levelType), в рамках доступа текущей организации.")
public record LevelTypeCountResponse(
        @Schema(description = "Уровень организации (республиканский, областной, районный и т.д.).")
        OrganizationLevel levelType,

        @Schema(description = "Количество активных учреждений этого уровня.")
        long count
) {
}
