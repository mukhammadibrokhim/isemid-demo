package uz.uzinfocom.app.modules.form0581.application.stats.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Количество форм №058-1 по организации.")
public record Form0581OrganizationCountResponse(
        @Schema(description = "Идентификатор организации.")
        Long organizationId,

        @Schema(description = "Количество форм, относящихся к данной организации.")
        long count
) {
}
