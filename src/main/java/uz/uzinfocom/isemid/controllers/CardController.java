package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.annotations.CurrentUser;
import uz.uzinfocom.isemid.models.entities.card.CardStatus;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqCard;
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
            @CurrentUser User user,
            HttpServletRequest request
    ) {
        return cardService.getAllCardsByFilter(page, size, formTypeCode, fromOrgName, toOrgName, cardStatus, user, getFullURL(request));
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

    @Operation(summary = "Accept assigned card",
            description = "Allows an assigned user to accept the card. The card status changes to 'APPROVED'. If the card was previously rejected, it must be refilled first.")
    @PatchMapping("/{cardId}/accept/{userId}")
    public ApiResponse<?> acceptCard(@PathVariable Long cardId, @PathVariable Long userId) {
        return cardService.acceptCard(cardId, userId);
    }

    @Operation(summary = "Reject assigned card",
            description = "Allows an assigned user to reject the card if they find errors in the form. The status will be set to 'REJECTED_BY_USER'. If the card is already marked as 'COMPLETED', it cannot be rejected.")
    @PatchMapping("/{cardId}/reject/{userId}")
    public ApiResponse<?> rejectCard(@PathVariable Long cardId, @PathVariable Long userId) {
        return cardService.rejectCard(cardId, userId);
    }

    @Operation(summary = "Complete the assigned card",
            description = "Marks the assigned card as completed. Once completed, the card status changes to 'APPROVED' and cannot be modified.")
    @PatchMapping("/{cardId}/complete")
    public ApiResponse<?> completeCard(@PathVariable Long cardId, @CurrentUser User user) {
        return cardService.saveAndComplete(cardId, user);
    }

    @Operation(summary = "Reject the assigned card due to errors",
            description = "Marks the assigned card as rejected if there are errors in the completed data. The status will be set to 'REJECTED_BY_CHECKER'.")
    @PatchMapping("/{cardId}/reject")
    public ApiResponse<?> rejectByChecker(@PathVariable Long cardId, @CurrentUser User user) {
        return cardService.rejectCardByChecker(cardId, user);
    }
}
