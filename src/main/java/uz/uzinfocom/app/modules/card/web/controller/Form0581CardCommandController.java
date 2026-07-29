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
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

/**
 * Form0581-triggered card creation — mirrors {@link Form058CardCommandController}
 * exactly, except only the zoonotic/animal-bite card types
 * (CARD174/CARD175/CARD205) are accepted here; see
 * {@code CardCommandService.assignCardsToForm0581}.
 */
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Form 058-1", description = "Форма №058-1 — экстренное извещение о случае укуса/ослюнения животным.")
public class Form0581CardCommandController {

    private final CardCommandService cardCommandService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Назначить карты и сотрудников на форму №058-1",
            description = "Массово создаёт пустые карты для формы №058-1: по одной пустой карте на каждый "
                    + "запрошенный уникальный тип, все прикреплены к одному и тому же набору сотрудников. "
                    + "Допустимые типы карт здесь: CARD174, CARD175, CARD205 (зоонозные/укус животного) — "
                    + "остальные типы отклоняются с ошибкой валидации. Сотрудники затем видят карты в списке "
                    + "\"Мои карты\" (GET /cards/mine) и заполняют их через PUT /cards/{id}."
    )
    @PostMapping(ApiPaths.Form0581.ROOT + ApiPaths.Form0581.ASSIGN_CARDS)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> assignCards(
            @Parameter(description = "Идентификатор формы №058-1.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody AssignCardsRequest request
    ) {
        cardCommandService.assignCardsToForm0581(id, request);
        return ApiResponse.success(messageResolver.resolve("common.created"));
    }
}
