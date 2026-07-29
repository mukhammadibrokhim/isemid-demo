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
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.modules.card.application.query.CardFilterRequest;
import uz.uzinfocom.app.modules.card.application.query.CardQueryService;
import uz.uzinfocom.app.modules.card.application.query.dto.CardTableResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

/**
 * Form0581-scoped card listing — mirrors {@link Form058CardQueryController}
 * exactly, lives under {@code /v1/form-058-1/{id}/...} and is grouped under
 * the "Form 058-1" tag, not "Card".
 */
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Form 058-1", description = "Форма №058-1 — экстренное извещение о случае укуса/ослюнения животным.")
public class Form0581CardQueryController {

    private final CardQueryService cardQueryService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Список карт формы №058-1",
            description = "Возвращает постраничный список всех карт, привязанных к указанной форме №058-1, "
                    + "с возможностью фильтрации по типу карты, статусу, прикреплённому сотруднику и "
                    + "супервайзеру."
    )
    @GetMapping(ApiPaths.Form0581.ROOT + ApiPaths.Form0581.CARDS)
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<CardTableResponse> findByForm(
            @Parameter(description = "Идентификатор формы №058-1.", required = true)
            @PathVariable @Positive Long id,
            @ParameterObject @Valid CardFilterRequest filter,
            HttpServletRequest httpRequest
    ) {
        CardFilterRequest scoped = new CardFilterRequest(
                filter.page(),
                filter.size(),
                filter.sortBy(),
                filter.sortDir(),
                null,
                id,
                filter.cardType(),
                filter.status(),
                filter.assignedToUserId(),
                filter.assignedById()
        );

        return pagedResponseAssembler
                .toResponse(cardQueryService.findTable(scoped), messageResolver.resolve("common.success"), httpRequest);
    }
}
