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
import uz.uzinfocom.app.modules.form058.application.command.approve.ApproveForm058Service;
import uz.uzinfocom.app.modules.form058.application.command.cancel.CancelForm058Service;
import uz.uzinfocom.app.modules.form058.application.command.create.CreateForm058Service;
import uz.uzinfocom.app.modules.form058.application.command.delete.DeleteForm058Service;
import uz.uzinfocom.app.modules.form058.application.command.update.UpdateForm058Service;
import uz.uzinfocom.app.modules.form058.web.dto.request.ApproveForm058Request;
import uz.uzinfocom.app.modules.form058.web.dto.request.CancelForm058Request;
import uz.uzinfocom.app.modules.form058.web.dto.request.CreateForm058Request;
import uz.uzinfocom.app.modules.form058.web.dto.request.DeleteForm058Request;
import uz.uzinfocom.app.modules.form058.web.dto.request.NotApproveForm058Request;
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
    private final Form058WebMapper form058WebMapper;
    private final Form058SourceResolver sourceResolver;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Создать форму №058",
            description = "Регистрирует новую форму №058 и связанного пациента (если он ещё не зарегистрирован "
                    + "в системе). Начальный статус формы — NOT_APPROVED."
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
            summary = "Утвердить форму №058",
            description = "Переводит форму в статус APPROVED. Доступно, когда форма находится в статусе "
                    + "ожидания утверждения (APPROVED_PENDING)."
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
            summary = "Отклонить утверждение формы №058",
            description = "Отказывает в утверждении формы с указанием причины — форма возвращается на доработку."
    )
    @PatchMapping(ApiPaths.Form058.NOT_APPROVE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UpdateForm058Response> notApprove(
            @Parameter(description = "Идентификатор формы №058.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody NotApproveForm058Request request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.rejected"),
                form058WebMapper.toResponse(approveForm058Service.notApprove(form058WebMapper.toCommand(id, request)))
        );
    }

    @Operation(
            summary = "Аннулировать форму №058",
            description = "Переводит форму в статус CANCELED с обязательным указанием причины аннулирования."
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
}
