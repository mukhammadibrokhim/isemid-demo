package uz.uzinfocom.app.modules.card.web.controller;

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
import uz.uzinfocom.app.shared.response.ApiResponse;
import uz.uzinfocom.app.shared.response.PagedResponse;
import uz.uzinfocom.app.shared.response.PagedResponseAssembler;

/**
 * Exclusively {@code /v1/cards/*} — listing cards under a specific form
 * lives in {@link Form058CardQueryController} instead, so this stays a pure
 * "Card" tag in Swagger.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(name = ApiPaths.Card.ROOT)
@Tag(name = "Card")
public class CardQueryController {

    private final CardQueryService cardQueryService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    /**
     * The attached employee's own queue — always scoped server-side to the
     * authenticated user, never to a client-supplied id.
     */
    @GetMapping(ApiPaths.Card.MINE)
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<CardTableResponse> findAssignedToMe(
            @ParameterObject @Valid CardFilterRequest filter,
            HttpServletRequest httpRequest
    ) {
        return pagedResponseAssembler
                .toResponse(cardQueryService.findAssignedToMe(filter), messageResolver.resolve("common.success"), httpRequest);
    }

    @GetMapping(ApiPaths.Card.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CardDetailResponse> byId(@PathVariable @Positive Long id) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                cardQueryService.getById(id)
        );
    }
}
