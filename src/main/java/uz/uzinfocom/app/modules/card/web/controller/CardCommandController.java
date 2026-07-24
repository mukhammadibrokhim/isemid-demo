package uz.uzinfocom.app.modules.card.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.app.modules.card.application.command.CardCommandService;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.CardDetailResponse;
import uz.uzinfocom.app.modules.card.web.dto.request.CardRejectRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.CardRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.DeleteCardRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.ReassignCardUsersRequest;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

/**
 * Exclusively {@code /v1/cards/*} — anything triggered from a Form058 path
 * (create, bulk-assign) lives in {@link Form058CardCommandController}
 * instead, so this stays a pure "Card" tag in Swagger.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.Card.ROOT)
@Tag(
        name = "Card",
        description = "Управление жизненным циклом эпидемиологической карты: заполнение, приём/отклонение "
                + "сотрудником, завершение, решение супервайзера и назначение актов."
)
public class CardCommandController {

    private final CardCommandService cardCommandService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Сохранить карту (перевод в статус \"В работе\")",
            description = "Пошагово сохраняет заполненные поля карты. После первого успешного сохранения "
                    + "статус карты переходит в IN_PROGRESS и остаётся в нём при каждом последующем "
                    + "сохранении — доступно только после того, как сотрудник принял карту (ACCEPTED_BY_USER) "
                    + "или карта возвращена супервайзером на доработку (REJECTED). Возвращает полную "
                    + "детальную информацию по карте после сохранения."
    )
    @PutMapping(ApiPaths.Card.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CardDetailResponse> update(
            @Parameter(description = "Идентификатор карты.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody CardRequest request
    ) {
        CardDetailResponse updated = cardCommandService.update(id, request);
        return ApiResponse.success(messageResolver.resolve("common.updated"), updated);
    }

    @Operation(
            summary = "Удалить карту",
            description = "Удаление возможно только до появления реальных данных по карте — в статусах "
                    + "NEW, ACCEPTED_BY_USER или REJECTED_BY_USER. После начала заполнения (IN_PROGRESS) "
                    + "и далее по жизненному циклу удаление запрещено. Это мягкое удаление — запись остаётся "
                    + "в базе с отметкой об удалении."
    )
    @DeleteMapping(ApiPaths.Card.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> delete(
            @Parameter(description = "Идентификатор карты.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody DeleteCardRequest request
    ) {
        cardCommandService.delete(id, request.reason());
        return ApiResponse.success(messageResolver.resolve("common.deleted"));
    }

    @Operation(
            summary = "Принять карту (сотрудник)",
            description = "Прикреплённый сотрудник подтверждает, что берёт карту в работу. Доступно только "
                    + "из статуса NEW; после принятия карта переходит в ACCEPTED_BY_USER и становится "
                    + "доступной для сохранения/заполнения."
    )
    @PatchMapping(ApiPaths.Card.ACCEPT)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> accept(
            @Parameter(description = "Идентификатор карты.", required = true)
            @PathVariable @Positive Long id
    ) {
        cardCommandService.acceptByUser(id);
        return ApiResponse.success(messageResolver.resolve("common.accepted"));
    }

    @Operation(
            summary = "Отклонить карту (сотрудник)",
            description = "Прикреплённый сотрудник отказывается от карты с указанием причины. Доступно из "
                    + "статусов NEW и ACCEPTED_BY_USER (но не после первого сохранения). Карта переходит в "
                    + "REJECTED_BY_USER и ожидает повторного назначения супервайзером другому сотруднику."
    )
    @PatchMapping(ApiPaths.Card.REJECT)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> reject(
            @Parameter(description = "Идентификатор карты.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody CardRejectRequest request
    ) {
        cardCommandService.rejectByUser(id, request.comment());
        return ApiResponse.success(messageResolver.resolve("common.rejected"));
    }

    @Operation(
            summary = "Завершить заполнение карты (\"Сохранить и завершить\")",
            description = "Финальный шаг заполнения — карта переходит в статус COMPLETED и становится "
                    + "видна супервайзеру для проверки. Доступно из IN_PROGRESS или REJECTED (повторная "
                    + "отправка после отклонения супервайзером)."
    )
    @PatchMapping(ApiPaths.Card.COMPLETE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> complete(
            @Parameter(description = "Идентификатор карты.", required = true)
            @PathVariable @Positive Long id
    ) {
        cardCommandService.complete(id);
        return ApiResponse.success(messageResolver.resolve("common.completed"));
    }

    @Operation(
            summary = "Утвердить карту (супервайзер)",
            description = "Супервайзер, на которого назначена карта, окончательно утверждает её. Доступно "
                    + "только из статуса COMPLETED. Статус APPROVED — финальный, дальнейшие изменения карты "
                    + "невозможны."
    )
    @PatchMapping(ApiPaths.Card.SUPERVISOR_APPROVE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> supervisorApprove(
            @Parameter(description = "Идентификатор карты.", required = true)
            @PathVariable @Positive Long id
    ) {
        cardCommandService.approveBySupervisor(id);
        return ApiResponse.success(messageResolver.resolve("common.approved"));
    }

    @Operation(
            summary = "Отклонить карту (супервайзер)",
            description = "Супервайзер возвращает завершённую карту на доработку с указанием причины. "
                    + "Доступно только из статуса COMPLETED. Карта переходит в REJECTED — сотрудник может "
                    + "снова сохранять и повторно завершать её."
    )
    @PatchMapping(ApiPaths.Card.SUPERVISOR_REJECT)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> supervisorReject(
            @Parameter(description = "Идентификатор карты.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody CardRejectRequest request
    ) {
        cardCommandService.rejectBySupervisor(id, request.comment());
        return ApiResponse.success(messageResolver.resolve("common.rejected"));
    }

    @Operation(
            summary = "Переназначить сотрудников карты (только супервайзер)",
            description = "Доступно только супервайзеру, на которого фактически назначена карта, и только "
                    + "после того, как сотрудник отклонил назначение (статус REJECTED_BY_USER). Полностью "
                    + "заменяет список прикреплённых сотрудников, очищает комментарий отклонения и "
                    + "возвращает карту в статус NEW для нового цикла принятия/отклонения."
    )
    @PatchMapping(ApiPaths.Card.SUPERVISOR_REASSIGN)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> supervisorReassign(
            @Parameter(description = "Идентификатор карты.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody ReassignCardUsersRequest request
    ) {
        cardCommandService.reassignUsers(id, request);
        return ApiResponse.success(messageResolver.resolve("common.reassigned"));
    }
}
