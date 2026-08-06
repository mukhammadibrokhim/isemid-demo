package uz.uzinfocom.app.modules.form0581.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.modules.form0581.application.command.accept.AcceptForm0581Service;
import uz.uzinfocom.app.modules.form0581.application.command.approve.ApproveForm0581Service;
import uz.uzinfocom.app.modules.form0581.application.command.cancel.CancelForm0581Service;
import uz.uzinfocom.app.modules.form0581.application.command.create.CreateForm0581Service;
import uz.uzinfocom.app.modules.form0581.application.command.delete.DeleteForm0581Service;
import uz.uzinfocom.app.modules.form0581.application.command.reopen.ReopenForm0581Service;
import uz.uzinfocom.app.modules.form0581.application.command.update.UpdateForm0581Service;
import uz.uzinfocom.app.modules.form0581.web.dto.request.ApproveForm0581Request;
import uz.uzinfocom.app.modules.form0581.web.dto.request.CancelForm0581Request;
import uz.uzinfocom.app.modules.form0581.web.dto.request.CreateForm0581Request;
import uz.uzinfocom.app.modules.form0581.web.dto.request.DeleteForm0581Request;
import uz.uzinfocom.app.modules.form0581.web.dto.request.UpdateForm0581Request;
import uz.uzinfocom.app.modules.form0581.web.dto.response.CreateForm0581Response;
import uz.uzinfocom.app.modules.form0581.web.dto.response.UpdateForm0581Response;
import uz.uzinfocom.app.modules.form0581.web.mapper.Form0581WebMapper;
import uz.uzinfocom.app.modules.form0581.web.resolvers.Form0581SourceResolver;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.security.context.SourceHeader;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

@Validated
@RestController
@RequiredArgsConstructor
@Tag(
        name = "Form 058-1",
        description = "Управление формой №058-1 — экстренным извещением о случае, подозрительном на бешенство "
                + "(укус/царапина/ослюнение животным): создание, редактирование, удаление, утверждение и аннулирование."
)
@RequestMapping(ApiPaths.Form0581.ROOT)
public class Form0581CommandController {

    private final CreateForm0581Service createForm0581Service;
    private final UpdateForm0581Service updateForm0581Service;
    private final DeleteForm0581Service deleteForm0581Service;
    private final ApproveForm0581Service approveForm0581Service;
    private final CancelForm0581Service cancelForm0581Service;
    private final AcceptForm0581Service acceptForm0581Service;
    private final ReopenForm0581Service reopenForm0581Service;
    private final Form0581WebMapper form0581WebMapper;
    private final Form0581SourceResolver sourceResolver;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Создать форму №058-1",
            description = "Регистрирует новую форму №058-1 и связанного пациента (если он ещё не зарегистрирован "
                    + "в системе). Начальный статус формы — SENT."
    )
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CreateForm0581Response> create(
            @Parameter(description = "Источник поступления формы (заполняется автоматически по заголовку запроса).")
            @RequestHeader(value = SourceHeader.X_SOURCE, required = false) String sourceHeader,
            @Valid @RequestBody CreateForm0581Request request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.created"),
                form0581WebMapper.toResponse(createForm0581Service.create(
                        form0581WebMapper.toCommand(request, sourceResolver.resolve(sourceHeader))
                ))
        );
    }

    @Operation(
            summary = "Обновить форму №058-1",
            description = "Редактирует данные ранее созданной формы. Все поля запроса необязательны — "
                    + "изменяются только переданные. Недоступно после утверждения/аннулирования формы."
    )
    @PutMapping(ApiPaths.Form0581.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UpdateForm0581Response> update(
            @Parameter(description = "Идентификатор формы №058-1.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateForm0581Request request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.updated"),
                form0581WebMapper.toResponse(updateForm0581Service.update(form0581WebMapper.toCommand(id, request)))
        );
    }

    @Operation(
            summary = "Удалить форму №058-1",
            description = "Удаляет форму с обязательным указанием причины удаления."
    )
    @DeleteMapping(value = ApiPaths.Form0581.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> delete(
            @Parameter(description = "Идентификатор формы №058-1.", required = true)
            @PathVariable Long id,
            @Valid @RequestBody DeleteForm0581Request request
    ) {
        deleteForm0581Service.delete(id, request.reason());
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }

    @Operation(
            summary = "Принять форму №058-1 (получатель)",
            description = "Получатель подтверждает приём формы: переводит её из статуса SENT в ACCEPTED. "
                    + "Назначение карт возможно только после этого. Доступно только организации-получателю. "
                    + "После приёма аннулировать/отклонить форму уже нельзя — см. эндпоинт cancel."
    )
    @PatchMapping(ApiPaths.Form0581.ACCEPT)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UpdateForm0581Response> accept(
            @Parameter(description = "Идентификатор формы №058-1.", required = true)
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.accepted"),
                form0581WebMapper.toResponse(acceptForm0581Service.accept(id))
        );
    }

    @Operation(
            summary = "Утвердить форму №058-1 (отправитель)",
            description = "Переводит форму в статус APPROVED с итоговым диагнозом. Доступно организации-отправителю "
                    + "после того, как форма принята получателем и к ней привязана карта (статус CARD_LINKED)."
    )
    @PatchMapping(ApiPaths.Form0581.APPROVE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UpdateForm0581Response> approve(
            @Parameter(description = "Идентификатор формы №058-1.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody ApproveForm0581Request request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.approved"),
                form0581WebMapper.toResponse(approveForm0581Service.approve(form0581WebMapper.toCommand(id, request)))
        );
    }

    @Operation(
            summary = "Аннулировать/отклонить форму №058-1",
            description = "Переводит форму в статус CANCELED с обязательным указанием причины. Доступно и "
                    + "организации-отправителю (отзыв), и организации-получателю (отклонение) — но только пока "
                    + "форма ещё не принята получателем (статус SENT); после accept отменить форму уже нельзя. "
                    + "Это финальное, заблокированное состояние — восстановить его может только "
                    + "супер-администратор через отдельный эндпоинт reopen."
    )
    @PatchMapping(ApiPaths.Form0581.CANCEL)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UpdateForm0581Response> cancel(
            @Parameter(description = "Идентификатор формы №058-1.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody CancelForm0581Request request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.canceled"),
                form0581WebMapper.toResponse(cancelForm0581Service.cancel(form0581WebMapper.toCommand(id, request)))
        );
    }

    @Operation(
            summary = "Восстановить аннулированную/отклонённую форму №058-1 (только супер-админ)",
            description = "Переводит форму из статуса CANCELED обратно в SENT — независимо от того, какая "
                    + "организация её отменила. Единственный способ снять блокировку с закрытой формы — "
                    + "доступен только супер-администратору."
    )
    @PatchMapping(ApiPaths.Form0581.REOPEN)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UpdateForm0581Response> reopen(
            @Parameter(description = "Идентификатор формы №058-1.", required = true)
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.reopened"),
                form0581WebMapper.toResponse(reopenForm0581Service.reopen(id))
        );
    }
}
