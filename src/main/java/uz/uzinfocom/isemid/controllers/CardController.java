package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.annotations.CurrentUser;
import uz.uzinfocom.isemid.models.entities.card.CardStatus;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqCard;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqRejectCard;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.services.card.CardService;

import static uz.uzinfocom.isemid.utils.ApiUrlConstant.CARD;
import static uz.uzinfocom.isemid.utils.AppConstant.PAGE;
import static uz.uzinfocom.isemid.utils.AppConstant.SIZE;
import static uz.uzinfocom.isemid.utils.CommonUtils.getFullURL;

@RestController
@RequestMapping(CARD)
@RequiredArgsConstructor
@Tag(name = "Card")
public class CardController {

    final CardService cardService;

    @Operation(summary = "Get all cards with filters",
            description = "Retrieves a paginated list of cards based on optional filters like form type, organization names, and card status.")
    @GetMapping("/all")
    public ApiResponsePage allByFilter(
            @RequestParam(defaultValue = PAGE) int page,
            @RequestParam(defaultValue = SIZE) int size,
            @RequestParam(required = false) String formTypeCode,
            @RequestParam(required = false) String fromOrgName,
            @RequestParam(required = false) String toOrgName,
            @RequestParam(required = false) CardStatus cardStatus,
            @RequestParam(required = false) Long formId,
            @CurrentUser User user,
            HttpServletRequest request
    ) {
        return cardService.getAllCardsByFilter(page, size, formTypeCode, fromOrgName, toOrgName, cardStatus, formId, user, getFullURL(request));
    }

    @Operation(summary = "Get users assigned to a card",
            description = "Retrieves a paginated list of users assigned to a specific card, identified by userId.")
    @GetMapping("/user/{userId}")
    public ApiResponsePage getUsersByCardId(
            @RequestParam(defaultValue = PAGE) int page,
            @RequestParam(defaultValue = SIZE) int size,
            @PathVariable Long userId,
            HttpServletRequest request
    ) {
        return cardService.getCardUsers(page, size, userId, getFullURL(request));
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
            @RequestBody ReqCard reqCard,
            @CurrentUser User currentUser
    ) {
        return cardService.updateCard(id, reqCard, currentUser);
    }

    @Operation(summary = "Accepting the incoming card")
    @PatchMapping("/{cardId}/accept")
    public ApiResponse<?> acceptCard(@PathVariable long cardId, @CurrentUser User currentUser) {
        return cardService.acceptCard(cardId, currentUser);
    }

    @Operation(summary = "Reject the incoming card")
    @PutMapping("/{cardId}/reject")
    public ApiResponse<?> rejectCard(@PathVariable Long cardId, @CurrentUser User currentUser, @Valid @RequestBody ReqRejectCard rejectCard) {
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
                                               @Valid @RequestBody ReqRejectCard rejectCard) {
        return cardService.supervisorRejectCard(cardId, currentUser, rejectCard);
    }
}
