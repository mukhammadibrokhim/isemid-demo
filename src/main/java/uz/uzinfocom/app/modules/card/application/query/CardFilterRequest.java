package uz.uzinfocom.app.modules.card.application.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

public record CardFilterRequest(
        @Min(1) Integer page,
        @Min(1) @Max(200) Integer size,
        String sortBy,
        String sortDir,
        Long formId,
        CardType cardType,
        CardStatus status,
        Long assignedToUserId,
        Long assignedById
) implements PageableRequest {

    /**
     * Copies this filter with {@code assignedToUserId} forced to the given
     * value and {@code assignedById} cleared, ignoring whatever the caller
     * originally supplied for either — used to scope the "my cards" view to
     * the authenticated attached employee, never a client-chosen id.
     */
    public CardFilterRequest scopedToAttachedUser(Long userId) {
        return new CardFilterRequest(page, size, sortBy, sortDir, formId, cardType, status, userId, null);
    }
}
