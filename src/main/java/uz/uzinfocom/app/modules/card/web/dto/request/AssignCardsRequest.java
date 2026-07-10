package uz.uzinfocom.app.modules.card.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

import java.util.List;

/**
 * Bulk-assigns one or more card types plus a shared set of employees to a
 * form: one blank card per distinct requested type is created, each with
 * every listed user attached. The actual field data for each card is filled
 * in afterwards through the normal {@code PUT /cards/{id}} update flow —
 * this endpoint only sets up who owns what, matching the legacy
 * "assign card" step that precedes real data entry.
 */
@Schema(description = "Запрос на массовое создание пустых карт указанных типов и прикрепление сотрудников к форме №058.")
public record AssignCardsRequest(
        @Schema(description = "Список типов карт, которые необходимо создать (по одной пустой карте на каждый уникальный тип).",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty List<@NotNull CardType> cardTypes,

        @Schema(description = "Список идентификаторов сотрудников, прикрепляемых ко всем создаваемым картам.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty List<@NotNull @Positive Long> assignUserIds
) {
}
