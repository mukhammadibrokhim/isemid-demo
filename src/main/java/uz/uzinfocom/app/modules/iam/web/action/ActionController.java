package uz.uzinfocom.app.modules.iam.web.action;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.modules.iam.application.action.command.ActionCommandService;
import uz.uzinfocom.app.modules.iam.application.action.command.dto.ActionCreateRequest;
import uz.uzinfocom.app.modules.iam.application.action.command.dto.ActionUpdateRequest;
import uz.uzinfocom.app.modules.iam.application.action.query.ActionQueryService;
import uz.uzinfocom.app.modules.iam.application.action.query.dto.ActionDetailResponse;
import uz.uzinfocom.app.modules.iam.application.action.query.dto.ActionFilterRequest;
import uz.uzinfocom.app.modules.iam.application.action.query.dto.ActionTableResponse;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

@Tag(
        name = "Action",
        description = "API для просмотра и управления действиями (RBAC), доступными для назначения правам доступа."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Action.ROOT)
@RequiredArgsConstructor
public class ActionController {

    private final ActionQueryService actionQueryService;
    private final ActionCommandService actionCommandService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Получить список действий",
            description = "Возвращает постраничный список действий с фильтрацией по коду и признаку активности."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный запрос.")
    @GetMapping
    public PagedResponse<ActionTableResponse> getAll(
            @ParameterObject @Valid @ModelAttribute ActionFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<ActionTableResponse> responses = actionQueryService.findTable(request);
        return pagedResponseAssembler.toResponse(responses, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Получить действие по идентификатору",
            description = "Возвращает детальную информацию о действии."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный запрос.")
    @GetMapping(ApiPaths.Action.BY_ID)
    public ApiResponse<ActionDetailResponse> getById(
            @Parameter(description = "Уникальный идентификатор действия.", required = true)
            @PathVariable @Positive Long id
    ) {
        ActionDetailResponse response = actionQueryService.getById(id);
        return ApiResponse.success(messageResolver.resolve("common.success"), response);
    }

    @Operation(
            summary = "Создать действие",
            description = "Создает новое действие (например, custom-действие) для последующего назначения правам доступа. Доступно только Super Admin."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Запись успешно создана.")
    @PreAuthorize("@adminAccessGuard.isSuperAdmin()")
    @PostMapping
    public ApiResponse<ActionTableResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные создаваемого действия.",
                    required = true
            )
            @Valid @RequestBody ActionCreateRequest request
    ) {
        ActionTableResponse response = actionCommandService.create(request);
        return ApiResponse.success(messageResolver.resolve("action.created"), response);
    }

    @Operation(
            summary = "Обновить действие",
            description = "Обновляет код, описания и признак активности действия. Доступно только Super Admin."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный запрос.")
    @PreAuthorize("@adminAccessGuard.isSuperAdmin()")
    @PutMapping(ApiPaths.Action.BY_ID)
    public ApiResponse<ActionTableResponse> update(
            @Parameter(description = "Уникальный идентификатор действия.", required = true)
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новые данные действия.",
                    required = true
            )
            @Valid @RequestBody ActionUpdateRequest request
    ) {
        ActionTableResponse response = actionCommandService.update(id, request);
        return ApiResponse.success(messageResolver.resolve("action.updated"), response);
    }

    @Operation(
            summary = "Удалить действие",
            description = "Выполняет мягкое удаление действия, после чего оно не используется при авторизации. Доступно только Super Admin."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Операция выполнена успешно.")
    @PreAuthorize("@adminAccessGuard.isSuperAdmin()")
    @DeleteMapping(ApiPaths.Action.BY_ID)
    public ApiResponse<Void> deleteById(
            @Parameter(description = "Уникальный идентификатор действия.", required = true)
            @PathVariable @Positive Long id
    ) {
        actionCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("action.deleted"), null);
    }

    @Operation(
            summary = "Восстановить действие",
            description = "Восстанавливает мягко удаленное действие и делает его активным. Доступно только Super Admin."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Операция выполнена успешно.")
    @PreAuthorize("@adminAccessGuard.isSuperAdmin()")
    @PatchMapping(ApiPaths.Action.RESTORE)
    public ApiResponse<Void> restore(
            @Parameter(description = "Уникальный идентификатор действия.", required = true)
            @PathVariable @Positive Long id
    ) {
        actionCommandService.restore(id);
        return ApiResponse.success(messageResolver.resolve("action.restored"), null);
    }
}
