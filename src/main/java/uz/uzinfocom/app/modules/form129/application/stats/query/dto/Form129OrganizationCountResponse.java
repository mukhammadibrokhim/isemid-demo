package uz.uzinfocom.app.modules.form129.application.stats.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Количество форм №129 по организации-получателю.")
public record Form129OrganizationCountResponse(
        @Schema(description = "Идентификатор организации.")
        Long organizationId,

        @Schema(description = "Количество форм, относящихся к данной организации.")
        long count
) {
}
