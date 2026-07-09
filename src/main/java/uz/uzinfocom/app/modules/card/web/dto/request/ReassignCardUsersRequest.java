package uz.uzinfocom.app.modules.card.web.dto.request;

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
public record ReassignCardUsersRequest(
        @NotEmpty List<@NotNull @Positive Long> assignUserIds
) {
}
