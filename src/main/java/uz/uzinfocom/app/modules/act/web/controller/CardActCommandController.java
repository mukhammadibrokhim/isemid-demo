package uz.uzinfocom.app.modules.act.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.modules.act.application.command.ActCommandService;
import uz.uzinfocom.app.modules.act.web.dto.request.AssignActsRequest;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

/**
 * Card-triggered act creation — this path lives under
 * {@code /v1/cards/{id}/...} and is grouped under the "Card" tag, not "Act",
 * even though the actual Act lifecycle logic (via {@link ActCommandService})
 * belongs to the Act module. Mirrors exactly how
 * {@code Form058CardCommandController} keeps Form058-triggered card creation
 * out of the pure {@code CardCommandController}.
 */
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Card", description = "Управление жизненным циклом эпидемиологической карты и назначенных на неё актов.")
public class CardActCommandController {

    private final ActCommandService actCommandService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Назначить акты и сотрудников на карту",
            description = "Массово создаёт пустые акты для карты: по одному пустому акту на каждый "
                    + "запрошенный уникальный тип, все прикреплены к одному и тому же набору сотрудников. "
                    + "Сотрудники затем видят акты в списке \"Мои акты\" (GET /acts/mine) и заполняют их через "
                    + "PUT /acts/{id}. Это единственный способ создания актов, полностью повторяющий назначение "
                    + "карт на форму №058."
    )
    @PostMapping(ApiPaths.Card.ROOT + ApiPaths.Card.ACTS)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> assignActs(
            @Parameter(description = "Идентификатор карты.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody AssignActsRequest request
    ) {
        actCommandService.assignActs(id, request);
        return ApiResponse.success(messageResolver.resolve("common.created"));
    }
}
