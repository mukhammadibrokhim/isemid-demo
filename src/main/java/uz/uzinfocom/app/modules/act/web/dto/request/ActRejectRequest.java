package uz.uzinfocom.app.modules.act.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Shared by both the attached-user reject flow and the supervisor reject
 * flow, matching {@code CardRejectRequest}'s dual use.
 */
@Schema(description = "Запрос на отклонение акта — используется как сотрудником, так и супервайзером.")
public record ActRejectRequest(
        @Schema(description = "Причина отклонения акта.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 1000) String comment
) {
}
