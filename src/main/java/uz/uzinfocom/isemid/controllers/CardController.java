package uz.uzinfocom.isemid.controllers;

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

//    @PostMapping("/assign-user")
//    public ApiResponse<?> assignUser(
//            @Valid @RequestBody ReqCardAssignUsers assignUsers,
//            @CurrentUser User currentUser
//    ) {
//        return cardService.assignUsers(assignUsers, currentUser);
//    }

    @GetMapping("/user/{userId}")
    public ApiResponsePage getUsersByCardId(
            @RequestParam(defaultValue = PAGE) int page,
            @RequestParam(defaultValue = SIZE) int size,
            @PathVariable Long userId,
            HttpServletRequest request
    ) {
        return cardService.getCardUsers(page, size, userId, getFullURL(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getCardById(@PathVariable long id) {
        return cardService.getById(id);
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateCard(
            @PathVariable long id,
            @RequestBody ReqCard reqCard,
            @CurrentUser User currentUser
    ) {
        return cardService.updateCard(id, reqCard, currentUser);
    }

    @PatchMapping("/{cardId}/accept/{userId}")
    public ApiResponse<?> acceptCard(@PathVariable Long cardId, @PathVariable Long userId) {
        return cardService.acceptCard(cardId, userId);
    }

    @PatchMapping("/{cardId}/reject/{userId}")
    public ApiResponse<?> rejectCard(@PathVariable Long cardId, @PathVariable Long userId) {
        return cardService.rejectCard(cardId, userId);
    }

    @PatchMapping("/{cardId}/complete")
    public ApiResponse<?> completeCard(@PathVariable Long cardId) {
        return cardService.saveAndComplete(cardId);
    }

}
