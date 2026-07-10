package uz.uzinfocom.app.modules.card.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * Only valid once the previously attached employee has rejected the
 * assignment — replaces the card's users entirely and resets it back to
 * NEW so the newly attached employee(s) go through their own accept/reject
 * cycle.
 */
@Schema(description = "Запрос супервайзера на переназначение карты новым сотрудникам после отклонения "
        + "предыдущим исполнителем.")
public record ReassignCardUsersRequest(
        @Schema(description = "Список идентификаторов новых сотрудников, которым передаётся карта.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty List<@NotNull @Positive Long> assignUserIds
) {
}
