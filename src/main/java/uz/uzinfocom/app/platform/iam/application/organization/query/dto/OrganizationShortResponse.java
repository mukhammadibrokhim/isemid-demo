package uz.uzinfocom.app.platform.iam.application.organization.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Краткая информация об организации.")
public record OrganizationShortResponse(
        @Schema(description = "Уникальный идентификатор записи.", example = "1")
        Long id,
        @Schema(description = "Уникальный UUID записи.")
        UUID uuid,
        @Schema(description = "Наименование организации.")
        String name
) {
}
