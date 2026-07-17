package uz.uzinfocom.app.modules.act.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.app.modules.act.application.command.ActCommandService;
import uz.uzinfocom.app.modules.act.application.query.dto.ActDetailResponse;
import uz.uzinfocom.app.modules.act.web.dto.request.ActRejectRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.ReassignActUsersRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.UpdateActRequest;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

/**
 * Exclusively {@code /v1/acts/*} — attaching an act to a card lives in
 * {@link CardActCommandController} instead, so this stays a pure "Act" tag
 * in Swagger, matching how {@code CardCommandController} is organized.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.Act.ROOT)
@Tag(
        name = "Act",
        description = "Управление жизненным циклом акта: заполнение, приём/отклонение сотрудником, "
                + "завершение и решение супервайзера."
)
public class ActCommandController {

    private final ActCommandService actCommandService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Сохранить акт (перевод в статус \"В работе\")",
            description = "Сохраняет результат/заключение по акту. После первого успешного сохранения статус "
                    + "переходит в IN_PROGRESS — доступно только после того, как сотрудник принял акт "
                    + "(ACCEPTED_BY_USER) или акт возвращён супервайзером на доработку (REJECTED). Возвращает "
                    + "полную детальную информацию по акту после сохранения."
    )
    @PutMapping(ApiPaths.Act.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ActDetailResponse> update(
            @Parameter(description = "Идентификатор акта.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateActRequest request
    ) {
        ActDetailResponse updated = actCommandService.update(id, request);
        return ApiResponse.success(messageResolver.resolve("common.updated"), updated);
    }

    @Operation(
            summary = "Удалить акт",
            description = "Удаление возможно только до появления реальных данных по акту — в статусах NEW, "
                    + "ACCEPTED_BY_USER или REJECTED_BY_USER."
    )
    @DeleteMapping(ApiPaths.Act.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> delete(
            @Parameter(description = "Идентификатор акта.", required = true)
            @PathVariable @Positive Long id
    ) {
        actCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"));
    }

    @Operation(
            summary = "Принять акт (сотрудник)",
            description = "Прикреплённый сотрудник подтверждает, что берёт акт в работу. Доступно только из "
                    + "статуса NEW; после принятия акт переходит в ACCEPTED_BY_USER."
    )
    @PatchMapping(ApiPaths.Act.ACCEPT)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> accept(
            @Parameter(description = "Идентификатор акта.", required = true)
            @PathVariable @Positive Long id
    ) {
        actCommandService.acceptByUser(id);
        return ApiResponse.success(messageResolver.resolve("common.accepted"));
    }

    @Operation(
            summary = "Отклонить акт (сотрудник)",
            description = "Прикреплённый сотрудник отказывается от акта с указанием причины. Доступно из "
                    + "статусов NEW и ACCEPTED_BY_USER. Акт переходит в REJECTED_BY_USER и ожидает повторного "
                    + "назначения супервайзером другому сотруднику."
    )
    @PatchMapping(ApiPaths.Act.REJECT)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> reject(
            @Parameter(description = "Идентификатор акта.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody ActRejectRequest request
    ) {
        actCommandService.rejectByUser(id, request.comment());
        return ApiResponse.success(messageResolver.resolve("common.rejected"));
    }

    @Operation(
            summary = "Завершить заполнение акта (\"Сохранить и завершить\")",
            description = "Финальный шаг заполнения — акт переходит в статус COMPLETED и становится виден "
                    + "супервайзеру для проверки."
    )
    @PatchMapping(ApiPaths.Act.COMPLETE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> complete(
            @Parameter(description = "Идентификатор акта.", required = true)
            @PathVariable @Positive Long id
    ) {
        actCommandService.complete(id);
        return ApiResponse.success(messageResolver.resolve("common.completed"));
    }

    @Operation(
            summary = "Утвердить акт (супервайзер)",
            description = "Супервайзер, на которого назначен акт, окончательно утверждает его. Доступно только "
                    + "из статуса COMPLETED. Статус APPROVED — финальный."
    )
    @PatchMapping(ApiPaths.Act.SUPERVISOR_APPROVE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> supervisorApprove(
            @Parameter(description = "Идентификатор акта.", required = true)
            @PathVariable @Positive Long id
    ) {
        actCommandService.approveBySupervisor(id);
        return ApiResponse.success(messageResolver.resolve("common.approved"));
    }

    @Operation(
            summary = "Отклонить акт (супервайзер)",
            description = "Супервайзер возвращает завершённый акт на доработку с указанием причины. Доступно "
                    + "только из статуса COMPLETED. Акт переходит в REJECTED — сотрудник может снова сохранять "
                    + "и повторно завершать его."
    )
    @PatchMapping(ApiPaths.Act.SUPERVISOR_REJECT)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> supervisorReject(
            @Parameter(description = "Идентификатор акта.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody ActRejectRequest request
    ) {
        actCommandService.rejectBySupervisor(id, request.comment());
        return ApiResponse.success(messageResolver.resolve("common.rejected"));
    }

    @Operation(
            summary = "Переназначить сотрудников акта (только супервайзер)",
            description = "Доступно только супервайзеру, на которого фактически назначен акт, и только после "
                    + "того, как сотрудник отклонил назначение (статус REJECTED_BY_USER). Полностью заменяет "
                    + "список прикреплённых сотрудников и возвращает акт в статус NEW."
    )
    @PatchMapping(ApiPaths.Act.SUPERVISOR_REASSIGN)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> supervisorReassign(
            @Parameter(description = "Идентификатор акта.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody ReassignActUsersRequest request
    ) {
        actCommandService.reassignUsers(id, request);
        return ApiResponse.success(messageResolver.resolve("common.reassigned"));
    }
}
