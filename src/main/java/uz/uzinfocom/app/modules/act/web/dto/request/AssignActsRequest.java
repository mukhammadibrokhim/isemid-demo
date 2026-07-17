package uz.uzinfocom.app.modules.act.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Bulk-assigns one or more acts plus a shared set of employees to a card:
 * one blank act per distinct requested {@code actType} is created, each with
 * every listed user attached — the exact same "blank shell now, real data
 * later" pattern as {@code AssignCardsRequest} for Form058 -> Card. The
 * actual outcome is filled in afterwards through {@code PUT /acts/{id}}.
 */
@Schema(description = "Запрос на массовое создание пустых актов указанных типов и прикрепление сотрудников к карте.")
public record AssignActsRequest(
        @Schema(description = "Список типов актов, которые необходимо создать (по одному пустому акту на каждый уникальный тип).",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty List<@NotBlank @Size(max = 50) String> actTypes,

        @Schema(description = "Список идентификаторов сотрудников, прикрепляемых ко всем создаваемым актам.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty List<@NotNull @Positive Long> assignUserIds
) {
}
