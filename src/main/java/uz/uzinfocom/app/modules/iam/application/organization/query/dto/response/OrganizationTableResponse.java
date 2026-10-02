package uz.uzinfocom.app.modules.iam.application.organization.query.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Краткая информация об организации для табличного списка.")
public record OrganizationTableResponse(
        @Schema(description = "Уникальный идентификатор записи.", example = "1")
        Long id,
        @Schema(description = "UUID организации.")
        UUID uuid,
        @Schema(description = "ИНН организации.")
        String tin,
        @Schema(description = "Активность организации.")
        Boolean active,
        @Schema(description = "Наименование организации.")
        String name,
        @Schema(description = "Код региона организации.")
        String regionName,
        @Schema(description = "Код района или города организации.")
        String districtName
) {
}
