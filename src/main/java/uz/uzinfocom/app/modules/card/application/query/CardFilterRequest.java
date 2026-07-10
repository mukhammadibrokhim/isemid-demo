package uz.uzinfocom.app.modules.card.application.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

public record CardFilterRequest(
        @Schema(description = "Номер страницы (начиная с 1).")
        @Min(1) Integer page,

        @Schema(description = "Размер страницы (количество записей).")
        @Min(1) @Max(200) Integer size,

        @Schema(description = "Поле сортировки.")
        String sortBy,

        @Schema(description = "Направление сортировки (asc/desc).")
        String sortDir,

        @Schema(description = "Идентификатор формы №058 для фильтрации карт.")
        Long formId,

        @Schema(description = "Тип карты для фильтрации.")
        CardType cardType,

        @Schema(description = "Статус карты для фильтрации.")
        CardStatus status,

        @Schema(description = "Идентификатор прикреплённого сотрудника для фильтрации.")
        Long assignedToUserId,

        @Schema(description = "Идентификатор супервайзера для фильтрации.")
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
