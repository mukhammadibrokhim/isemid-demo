package uz.uzinfocom.app.modules.act.web.controller;

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
import uz.uzinfocom.app.modules.act.application.query.ActFilterRequest;
import uz.uzinfocom.app.modules.act.application.query.ActQueryService;
import uz.uzinfocom.app.modules.act.application.query.dto.ActTableResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

/**
 * Card-scoped act listing — lives under {@code /v1/cards/{id}/...} and is
 * grouped under the "Card" tag, not "Act". Mirrors
 * {@code Form058CardQueryController} exactly.
 */
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Card", description = "Управление жизненным циклом эпидемиологической карты и назначенных на неё актов.")
public class CardActQueryController {

    private final ActQueryService actQueryService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Список актов карты",
            description = "Возвращает постраничный список всех актов, привязанных к указанной карте, с "
                    + "возможностью фильтрации по статусу, прикреплённому сотруднику и супервайзеру."
    )
    @GetMapping(ApiPaths.Card.ROOT + ApiPaths.Card.ACTS)
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<ActTableResponse> findByCard(
            @Parameter(description = "Идентификатор карты.", required = true)
            @PathVariable @Positive Long id,
            @ParameterObject @Valid ActFilterRequest filter,
            HttpServletRequest httpRequest
    ) {
        ActFilterRequest scoped = new ActFilterRequest(
                filter.page(),
                filter.size(),
                filter.sortBy(),
                filter.sortDir(),
                id,
                filter.status(),
                filter.assignedToUserId(),
                filter.assignedById()
        );

        return pagedResponseAssembler
                .toResponse(actQueryService.findByCard(scoped), messageResolver.resolve("common.success"), httpRequest);
    }
}
