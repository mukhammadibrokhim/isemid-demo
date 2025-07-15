package uz.uzinfocom.isemid.features.card.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
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

    @Operation(
            summary = "Получить все карты с фильтрами",
            description = "Возвращает постраничный список карт с учетом необязательных фильтров, таких как код типа формы, названия организаций (отправителя и получателя) и статус карты."
    )
    @GetMapping("/all")
    public PagedResponse<?> allByFilter(
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

    @Operation(
            summary = "Получить пользователей, назначенных на карту",
            description = "Возвращает постраничный список пользователей, назначенных на конкретную карту по userId."
    )
    @GetMapping("/user/{userId}")
    public PagedResponse<?> getUsersByCardId(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @PathVariable Long userId,
            HttpServletRequest request
    ) {
        return cardService.getCardUsers(page, size, userId, getRequestUrl(request));
    }

    @Operation(
            summary = "Получить данные карты по ID",
            description = "Извлекает подробную информацию о конкретной карте по её уникальному идентификатору."
    )
    @GetMapping("/{id}")
    public ApiResponse<?> getCardById(@PathVariable long id) {
        return cardService.getById(id);
    }

    @Operation(
            summary = "Обновить данные карты",
            description = "Обновляет информацию о конкретной карте. Только авторизованные пользователи могут изменять данные карты."
    )
    @PutMapping("/{id}")
    public ApiResponse<?> updateCard(
            @PathVariable long id,
            @RequestBody CardRequest reqCard,
            @CurrentUser User currentUser
    ) {
        return cardService.updateCard(id, reqCard, currentUser);
    }

    @Operation(
            summary = "Принять входящую карту",
            description = "Отмечает карту как 'ACCEPTED_BY_USER', указывая, что назначенный пользователь принял карту."
    )
    @PatchMapping("/{cardId}/accept")
    public ApiResponse<?> acceptCard(@PathVariable long cardId, @CurrentUser User currentUser) {
        return cardService.acceptCard(cardId, currentUser);
    }

    @Operation(
            summary = "Отклонить входящую карту",
            description = "Отмечает карту как 'REJECTED_BY_USER', указывая, что назначенный пользователь отклонил карту. Требуется указать причину отклонения."
    )
    @PutMapping("/{cardId}/reject")
    public ApiResponse<?> rejectCard(
            @PathVariable Long cardId,
            @CurrentUser User currentUser,
            @Valid @RequestBody CardRejectRequest rejectCard
    ) {
        return cardService.rejectCard(cardId, currentUser, rejectCard);
    }

    @Operation(
            summary = "Завершить указанную карту",
            description = "API завершает указанную карту, присваивая ей статус 'COMPLETE'."
    )
    @PatchMapping("/{cardId}/complete")
    public ApiResponse<?> complete(@PathVariable Long cardId, @CurrentUser User currentUser) {
        return cardService.saveAndComplete(cardId, currentUser);
    }

    @Operation(
            summary = "Утвердить карту руководителем",
            description = "API позволяет руководителю (Заведующему) утвердить карту, изменяя её статус на 'APPROVED'."
    )
    @PatchMapping("/{cardId}/supervisor/approve")
    public ApiResponse<?> supervisorApproveCard(@PathVariable long cardId, @CurrentUser User currentUser) {
        return cardService.supervisorApproveCard(cardId, currentUser);
    }

    @Operation(
            summary = "Отклонить карту руководителем",
            description = "API позволяет руководителю (Заведующему) отклонить карту, изменяя её статус на 'REJECTED'. Требуется указать причину отклонения."
    )
    @PutMapping("/{cardId}/supervisor/reject")
    public ApiResponse<?> supervisorRejectCard(
            @PathVariable Long cardId,
            @CurrentUser User currentUser,
            @Valid @RequestBody CardRejectRequest rejectCard
    ) {
        return cardService.supervisorRejectCard(cardId, currentUser, rejectCard);
    }

    @Operation(
            summary = "Назначить Act к карте",
            description = "Связывает Act с определённой картой на основании переданных данных.")
    @PostMapping("/assign-act")
    public ApiResponse<?> assignAct(@Valid @RequestBody CardAssignActRequest reqCardAssignAct, @CurrentUser User user) {
        return cardService.assignAct(reqCardAssignAct, user);
    }

    @Operation(
            summary = "Получить список Act-ов, связанных с картой",
            description = "Возвращает список всех Act-ов, связанных с указанной картой по её ID.")
    @GetMapping("/{cardId}/acts")
    public ApiResponse<?> cardActs(@PathVariable long cardId) {
        return cardService.getCardActs(cardId);
    }
}
