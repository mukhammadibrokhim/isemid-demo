package uz.uzinfocom.app.modules.form058.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.app.modules.form058.application.command.accept.AcceptForm058Service;
import uz.uzinfocom.app.modules.form058.application.command.approve.ApproveForm058Service;
import uz.uzinfocom.app.modules.form058.application.command.cancel.CancelForm058Service;
import uz.uzinfocom.app.modules.form058.application.command.create.CreateForm058Service;
import uz.uzinfocom.app.modules.form058.application.command.delete.DeleteForm058Service;
import uz.uzinfocom.app.modules.form058.application.command.reopen.ReopenForm058Service;
import uz.uzinfocom.app.modules.form058.application.command.update.UpdateForm058Service;
import uz.uzinfocom.app.modules.form058.web.dto.request.ApproveForm058Request;
import uz.uzinfocom.app.modules.form058.web.dto.request.CancelForm058Request;
import uz.uzinfocom.app.modules.form058.web.dto.request.CreateForm058Request;
import uz.uzinfocom.app.modules.form058.web.dto.request.DeleteForm058Request;
import uz.uzinfocom.app.modules.form058.web.dto.request.UpdateForm058Request;
import uz.uzinfocom.app.modules.form058.web.dto.response.CreateForm058Response;
import uz.uzinfocom.app.modules.form058.web.dto.response.UpdateForm058Response;
import uz.uzinfocom.app.modules.form058.web.mapper.Form058WebMapper;
import uz.uzinfocom.app.modules.form058.web.resolvers.Form058SourceResolver;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.security.context.SourceHeader;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

@Validated
@RestController
@RequiredArgsConstructor
@Tag(
        name = "Form 058",
        description = "Управление формой №058 — экстренным извещением об инфекционном заболевании: "
                + "создание, редактирование, удаление, утверждение и аннулирование."
)
@RequestMapping(ApiPaths.Form058.ROOT)
public class Form058CommandController {

    private final CreateForm058Service createForm058Service;
    private final UpdateForm058Service updateForm058Service;
    private final DeleteForm058Service deleteForm058Service;
    private final ApproveForm058Service approveForm058Service;
    private final CancelForm058Service cancelForm058Service;
    private final AcceptForm058Service acceptForm058Service;
    private final ReopenForm058Service reopenForm058Service;
    private final Form058WebMapper form058WebMapper;
    private final Form058SourceResolver sourceResolver;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Создать форму №058",
            description = "Регистрирует новую форму №058 и связанного пациента (если он ещё не зарегистрирован "
                    + "в системе). Начальный статус формы — SENT."
    )
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CreateForm058Response> create(
            @Parameter(description = "Источник поступления формы (заполняется автоматически по заголовку запроса).")
            @RequestHeader(value = SourceHeader.X_SOURCE, required = false) String sourceHeader,
            @Valid @RequestBody CreateForm058Request request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.created"),
                form058WebMapper.toResponse(createForm058Service.create(form058WebMapper.toCommand(request, sourceResolver.resolve(sourceHeader))))
        );
    }

    @Operation(
            summary = "Обновить форму №058",
            description = "Редактирует данные ранее созданной формы. Недоступно после утверждения/аннулирования формы."
    )
    @PutMapping(ApiPaths.Form058.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UpdateForm058Response> update(
            @Parameter(description = "Идентификатор формы №058.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateForm058Request request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.updated"),
                form058WebMapper.toResponse(updateForm058Service.update(form058WebMapper.toCommand(id, request)))
        );
    }

    @Operation(
            summary = "Удалить форму №058",
            description = "Удаляет форму с обязательным указанием причины удаления."
    )
    @DeleteMapping(value = ApiPaths.Form058.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> delete(
            @Parameter(description = "Идентификатор формы №058.", required = true)
            @PathVariable Long id,
            @Valid @RequestBody DeleteForm058Request request
    ) {
        deleteForm058Service.delete(id, request.reason());
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }

    @Operation(
            summary = "Принять форму №058 (получатель)",
            description = "Получатель подтверждает приём формы: переводит её из статуса SENT в ACCEPTED. "
                    + "Назначение карт возможно только после этого. Доступно только организации-получателю. "
                    + "После приёма аннулировать/отклонить форму уже нельзя — см. эндпоинт cancel."
    )
    @PatchMapping(ApiPaths.Form058.ACCEPT)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UpdateForm058Response> accept(
            @Parameter(description = "Идентификатор формы №058.", required = true)
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.accepted"),
                form058WebMapper.toResponse(acceptForm058Service.accept(id))
        );
    }

    @Operation(
            summary = "Утвердить форму №058 (отправитель)",
            description = "Переводит форму в статус APPROVED с итоговым диагнозом. Доступно организации-отправителю "
                    + "после того, как форма принята получателем и к ней привязана карта (статус CARD_LINKED)."
    )
    @PatchMapping(ApiPaths.Form058.APPROVE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UpdateForm058Response> approve(
            @Parameter(description = "Идентификатор формы №058.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody ApproveForm058Request request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.approved"),
                form058WebMapper.toResponse(approveForm058Service.approve(form058WebMapper.toCommand(id, request)))
        );
    }

    @Operation(
            summary = "Аннулировать/отклонить форму №058",
            description = "Переводит форму в статус CANCELED с обязательным указанием причины. Доступно и "
                    + "организации-отправителю (отзыв), и организации-получателю (отклонение) — но только пока "
                    + "форма ещё не принята получателем (статус SENT); после accept отменить форму уже нельзя. "
                    + "Это финальное, заблокированное состояние — восстановить его может только "
                    + "супер-администратор через отдельный эндпоинт reopen."
    )
    @PatchMapping(ApiPaths.Form058.CANCEL)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UpdateForm058Response> cancel(
            @Parameter(description = "Идентификатор формы №058.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody CancelForm058Request request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.canceled"),
                form058WebMapper.toResponse(cancelForm058Service.cancel(form058WebMapper.toCommand(id, request)))
        );
    }

    @Operation(
            summary = "Восстановить аннулированную/отклонённую форму №058 (только супер-админ)",
            description = "Переводит форму из статуса CANCELED обратно в SENT — независимо от того, какая "
                    + "организация её отменила. Единственный способ снять блокировку с закрытой формы — "
                    + "доступен только супер-администратору."
    )
    @PatchMapping(ApiPaths.Form058.REOPEN)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UpdateForm058Response> reopen(
            @Parameter(description = "Идентификатор формы №058.", required = true)
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.reopened"),
                form058WebMapper.toResponse(reopenForm058Service.reopen(id))
        );
    }
}
