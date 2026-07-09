package uz.uzinfocom.app.modules.card.web.controller;

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
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.modules.card.web.dto.request.AssignCardsRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.CardRequest;
import uz.uzinfocom.app.modules.card.web.dto.response.CardCreateResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.response.ApiResponse;

/**
 * Form058-triggered card creation — these paths live under
 * {@code /v1/form-058/{id}/...} and are grouped under the "Form 058" tag,
 * not "Card", even though the actual Card lifecycle logic (via
 * {@link CardCommandService}) belongs to the Card module. Kept in a
 * dedicated file so {@link CardCommandController} stays exclusively
 * {@code /v1/cards/*}.
 */
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Form 058")
public class Form058CardCommandController {

    private final CardCommandService cardCommandService;
    private final MessageResolver messageResolver;

    @PostMapping(ApiPaths.Form058.ROOT + ApiPaths.Form058.CARDS)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CardCreateResponse> create(
            @PathVariable @Positive Long id,
            @Valid @RequestBody CardRequest request
    ) {
        Card created = cardCommandService.create(id, request);
        return ApiResponse.success(
                messageResolver.resolve("common.created"),
                new CardCreateResponse(created.getId(), created.getCardType(), created.getStatus())
        );
    }

    /**
     * Bulk-assigns card types + employees to a form — creates one blank
     * card per distinct requested type, all attached to the same set of
     * users, ready for those employees to fill in via {@code PUT
     * /cards/{id}} once they see it in {@code GET /cards/mine}. Only
     * success matters here, not the created cards themselves — the form's
     * status moving to CARD_LINKED is the actual observable effect.
     */
    @PostMapping(ApiPaths.Form058.ROOT + ApiPaths.Form058.ASSIGN_CARDS)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> assignCards(
            @PathVariable @Positive Long id,
            @Valid @RequestBody AssignCardsRequest request
    ) {
        cardCommandService.assignCards(id, request);
        return ApiResponse.success(messageResolver.resolve("common.created"));
    }
}
