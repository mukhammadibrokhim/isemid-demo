package uz.uzinfocom.app.platform.iam.application.organization.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Краткая информация об организации для табличного списка.")
public record OrganizationTableResponse(
        @Schema(description = "Уникальный идентификатор записи.", example = "1")
        Long id,
        @Schema(description = "Наименование организации.")
        String name,
        @Schema(description = "Код региона организации.")
        String stateCode,
        @Schema(description = "Код района или города организации.")
        String cityCode
) {
}
