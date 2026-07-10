package uz.uzinfocom.app.modules.card.web.controller;

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
import uz.uzinfocom.app.modules.card.application.command.CardCommandService;
import uz.uzinfocom.app.modules.card.web.dto.request.AssignCardsRequest;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.response.ApiResponse;

/**
 * Form058-triggered card creation — this path lives under
 * {@code /v1/form-058/{id}/...} and is grouped under the "Form 058" tag,
 * not "Card", even though the actual Card lifecycle logic (via
 * {@link CardCommandService}) belongs to the Card module. Kept in a
 * dedicated file so {@link CardCommandController} stays exclusively
 * {@code /v1/cards/*}.
 */
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Form 058", description = "Форма №058 — эпидемиологическое извещение об инфекционном заболевании.")
public class Form058CardCommandController {

    private final CardCommandService cardCommandService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Назначить карты и сотрудников на форму",
            description = "Массово создаёт пустые карты для формы №058: по одной пустой карте на каждый "
                    + "запрошенный уникальный тип, все прикреплены к одному и тому же набору сотрудников. "
                    + "Сотрудники затем видят карты в списке \"Мои карты\" (GET /cards/assigned-to-me) и "
                    + "заполняют их через PUT /cards/{id}. Это единственный способ создания карт — "
                    + "отдельного эндпоинта для создания одной полностью заполненной карты не существует. "
                    + "При успешном выполнении статус формы переходит в CARD_LINKED."
    )
    @PostMapping(ApiPaths.Form058.ROOT + ApiPaths.Form058.ASSIGN_CARDS)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> assignCards(
            @Parameter(description = "Идентификатор формы №058.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody AssignCardsRequest request
    ) {
        cardCommandService.assignCards(id, request);
        return ApiResponse.success(messageResolver.resolve("common.created"));
    }
}
