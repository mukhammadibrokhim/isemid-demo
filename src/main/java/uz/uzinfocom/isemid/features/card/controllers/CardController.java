package uz.uzinfocom.isemid.features.card.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.ApiResponsePage;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardAssignActRequest;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRejectRequest;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;
import uz.uzinfocom.isemid.features.card.enums.CardStatus;
import uz.uzinfocom.isemid.features.card.services.CardService;
import uz.uzinfocom.isemid.features.user.annotation.CurrentUser;
import uz.uzinfocom.isemid.features.user.models.User;

import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_PAGE;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_SIZE;
import static uz.uzinfocom.isemid.commons.utils.HttpUtil.getRequestUrl;


@RestController
@RequestMapping(ApiPaths.Card.ROOT)
@RequiredArgsConstructor
@Tag(name = "Card")
public class CardController {

    private final CardService cardService;

    @Operation(summary = "Get all cards with filters",
            description = "Retrieves a paginated list of cards based on optional filters like form type, organization names, and card status.")
    @GetMapping("/all")
    public ApiResponsePage allByFilter(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(required = false) String formTypeCode,
            @RequestParam(required = false) String fromOrgName,
            @RequestParam(required = false) String toOrgName,
            @RequestParam(required = false) CardStatus cardStatus,
            @RequestParam(required = false) Long formId,
            @CurrentUser User user,
            HttpServletRequest request
    ) {
        return cardService.getAllCardsByFilter(page, size, formTypeCode, fromOrgName, toOrgName, cardStatus, formId, user, getRequestUrl(request));
    }

    @Operation(summary = "Get users assigned to a card",
            description = "Retrieves a paginated list of users assigned to a specific card, identified by userId.")
    @GetMapping("/user/{userId}")
    public ApiResponsePage getUsersByCardId(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @PathVariable Long userId,
            HttpServletRequest request
    ) {
        return cardService.getCardUsers(page, size, userId, getRequestUrl(request));
    }

    @Operation(summary = "Get card details by ID",
            description = "Fetches details of a specific card using its unique identifier.")
    @GetMapping("/{id}")
    public ApiResponse<?> getCardById(@PathVariable long id) {
        return cardService.getById(id);
    }

    @Operation(summary = "Update card details",
            description = "Updates the details of a specific card. Only authorized users can modify the card.")
    @PutMapping("/{id}")
    public ApiResponse<?> updateCard(
            @PathVariable long id,
            @RequestBody CardRequest reqCard,
            @CurrentUser User currentUser
    ) {
        return cardService.updateCard(id, reqCard, currentUser);
    }

    @Operation(
            summary = "Accept the incoming card",
            description = "Marks the card as 'ACCEPTED_BY_USER', indicating that the assigned user has accepted it."
    )
    @PatchMapping("/{cardId}/accept")
    public ApiResponse<?> acceptCard(@PathVariable long cardId, @CurrentUser User currentUser) {
        return cardService.acceptCard(cardId, currentUser);
    }

    @Operation(
            summary = "Reject the incoming card",
            description = "Marks the card as 'REJECTED_BY_USER', indicating that the assigned user has declined it. Requires a rejection reason."
    )
    @PutMapping("/{cardId}/reject")
    public ApiResponse<?> rejectCard(
            @PathVariable Long cardId,
            @CurrentUser User currentUser,
            @Valid @RequestBody CardRejectRequest rejectCard
    ) {
        return cardService.rejectCard(cardId, currentUser, rejectCard);
    }

    @Operation(summary = "Complete the specified Card",
            description = "This API finalizes the given Card object, marking it as 'COMPLETE'.")
    @PatchMapping("/{cardId}/complete")
    public ApiResponse<?> complete(@PathVariable Long cardId, @CurrentUser User currentUser) {
        return cardService.saveAndComplete(cardId, currentUser);
    }

    @Operation(summary = "Approve the assigned card by Supervisor",
            description = "This API allows the Supervisor (Заведующий) to approve the card, changing its status to 'APPROVED'.")
    @PatchMapping("/{cardId}/supervisor/approve")
    public ApiResponse<?> supervisorApproveCard(@PathVariable long cardId, @CurrentUser User currentUser) {
        return cardService.supervisorApproveCard(cardId, currentUser);
    }

    @Operation(summary = "Reject the assigned card by Supervisor",
            description = "This API allows the Supervisor (Заведующий) to reject the card, changing its status to 'REJECTED'. The reason for rejection must be provided.")
    @PutMapping("/{cardId}/supervisor/reject")
    public ApiResponse<?> supervisorRejectCard(@PathVariable Long cardId, @CurrentUser User currentUser,
                                               @Valid @RequestBody CardRejectRequest rejectCard) {
        return cardService.supervisorRejectCard(cardId, currentUser, rejectCard);
    }

    @PostMapping("/assign-act")
    public ApiResponse<?> assignAct(@Valid @RequestBody CardAssignActRequest reqCardAssignAct, @CurrentUser User user) {
        return cardService.assignAct(reqCardAssignAct, user);
    }

    @GetMapping("/{cardId}/acts")
    public ApiResponse<?> cardActs(@PathVariable long cardId) {
        return cardService.getCardActs(cardId);
    }
}
