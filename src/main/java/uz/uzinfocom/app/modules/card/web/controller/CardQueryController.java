package uz.uzinfocom.app.modules.card.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.modules.card.application.query.CardFilterRequest;
import uz.uzinfocom.app.modules.card.application.query.CardQueryService;
import uz.uzinfocom.app.modules.card.application.query.dto.CardTableResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.CardDetailResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

/**
 * Exclusively {@code /v1/cards/*} — listing cards under a specific form
 * lives in {@link Form058CardQueryController} instead, so this stays a pure
 * "Card" tag in Swagger.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.Card.ROOT)
@Tag(
        name = "Card",
        description = "Просмотр эпидемиологических карт (CARD161, CARD174, CARD175, CARD205, CARD_TUBE): "
                + "личный список карт сотрудника и получение карты по идентификатору."
)
public class CardQueryController {

    private final CardQueryService cardQueryService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Мои карты",
            description = "Возвращает постраничный список карт, прикреплённых к текущему авторизованному "
                    + "сотруднику. Область видимости всегда определяется на сервере по авторизованному "
                    + "пользователю — передать чужой идентификатор пользователя через фильтр невозможно. "
                    + "Для организации более высокого уровня (регионального или республиканского масштаба) "
                    + "список автоматически расширяется до всех карт в пределах её области видимости, а не "
                    + "только личных назначений."
    )
    @GetMapping(ApiPaths.Card.MINE)
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<CardTableResponse> findMine(
            @ParameterObject @Valid CardFilterRequest filter,
            HttpServletRequest httpRequest
    ) {
        return pagedResponseAssembler
                .toResponse(cardQueryService.findMine(filter), messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Получить карту по идентификатору",
            description = "Возвращает полную детальную информацию по карте, включая все дочерние данные "
                    + "(вакцинации, факторы риска, контактные лица и т.д.), в зависимости от типа карты."
    )
    @GetMapping(ApiPaths.Card.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CardDetailResponse> byId(
            @Parameter(description = "Идентификатор карты.", required = true)
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                cardQueryService.getById(id)
        );
    }
}
