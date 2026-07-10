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
import uz.uzinfocom.app.modules.form0581.application.command.approve.ApproveForm0581Service;
import uz.uzinfocom.app.modules.form0581.application.command.cancel.CancelForm0581Service;
import uz.uzinfocom.app.modules.form0581.application.command.create.CreateForm0581Service;
import uz.uzinfocom.app.modules.form0581.application.command.delete.DeleteForm0581Service;
import uz.uzinfocom.app.modules.form0581.application.command.update.UpdateForm0581Service;
import uz.uzinfocom.app.modules.form0581.web.dto.request.ApproveForm0581Request;
import uz.uzinfocom.app.modules.form0581.web.dto.request.CancelForm0581Request;
import uz.uzinfocom.app.modules.form0581.web.dto.request.CreateForm0581Request;
import uz.uzinfocom.app.modules.form0581.web.dto.request.DeleteForm0581Request;
import uz.uzinfocom.app.modules.form0581.web.dto.request.NotApproveForm0581Request;
import uz.uzinfocom.app.modules.form0581.web.dto.request.UpdateForm0581Request;
import uz.uzinfocom.app.modules.form0581.web.dto.response.CreateForm0581Response;
import uz.uzinfocom.app.modules.form0581.web.dto.response.UpdateForm0581Response;
import uz.uzinfocom.app.modules.form0581.web.mapper.Form0581WebMapper;
import uz.uzinfocom.app.modules.form0581.web.resolvers.Form0581Headers;
import uz.uzinfocom.app.modules.form0581.web.resolvers.Form0581SourceResolver;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.response.ApiResponse;

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
            @RequestHeader(value = Form0581Headers.X_SOURCE, required = false) String sourceHeader,
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
            summary = "Утвердить форму №058-1",
            description = "Переводит форму в статус APPROVED. Доступно, когда решение получателя ещё не принято "
                    + "(SENT, RECEIVED, APPROVED_PENDING)."
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
            summary = "Отклонить утверждение формы №058-1",
            description = "Отказывает в утверждении формы с указанием причины — форма возвращается на доработку."
    )
    @PatchMapping(ApiPaths.Form0581.NOT_APPROVE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UpdateForm0581Response> notApprove(
            @Parameter(description = "Идентификатор формы №058-1.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody NotApproveForm0581Request request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.rejected"),
                form0581WebMapper.toResponse(approveForm0581Service.notApprove(form0581WebMapper.toCommand(id, request)))
        );
    }

    @Operation(
            summary = "Аннулировать форму №058-1",
            description = "Переводит форму в статус CANCELED с обязательным указанием причины аннулирования."
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
}
