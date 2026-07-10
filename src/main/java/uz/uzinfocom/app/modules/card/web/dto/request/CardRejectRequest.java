package uz.uzinfocom.app.modules.card.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Shared by both the attached-user reject flow and the supervisor reject
 * flow, matching the legacy {@code CardRejectRequest} used in both places.
 */
@Schema(description = "Запрос на отклонение карты — используется как сотрудником, так и супервайзером.")
public record CardRejectRequest(
        @Schema(description = "Причина отклонения карты.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 1000) String comment
) {
}
