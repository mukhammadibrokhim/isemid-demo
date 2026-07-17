package uz.uzinfocom.app.modules.act.application.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

public record ActFilterRequest(
        @Schema(description = "Номер страницы (начиная с 1).")
        @Min(1) Integer page,

        @Schema(description = "Размер страницы (количество записей).")
        @Min(1) @Max(200) Integer size,

        @Schema(description = "Поле сортировки.")
        String sortBy,

        @Schema(description = "Направление сортировки (asc/desc).")
        String sortDir,

        @Schema(description = "Идентификатор карты для фильтрации актов.")
        Long cardId,

        @Schema(description = "Статус акта для фильтрации.")
        ActStatus status,

        @Schema(description = "Идентификатор прикреплённого сотрудника для фильтрации.")
        Long assignedToUserId,

        @Schema(description = "Идентификатор супервайзера для фильтрации.")
        Long assignedById
) implements PageableRequest {

    /**
     * Copies this filter with {@code assignedToUserId} forced to the given
     * value, ignoring whatever the caller originally supplied — used to
     * scope the "my acts" view to the authenticated attached employee, never
     * a client-chosen id.
     */
    public ActFilterRequest scopedToAttachedUser(Long userId) {
        return new ActFilterRequest(page, size, sortBy, sortDir, cardId, status, userId, assignedById);
    }
}
