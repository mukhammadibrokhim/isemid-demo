package uz.uzinfocom.app.modules.card.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Shared by both the attached-user reject flow and the supervisor reject
 * flow, matching the legacy {@code CardRejectRequest} used in both places.
 */
public record CardRejectRequest(
        @NotBlank @Size(max = 1000) String comment
) {
}
