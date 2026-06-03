package uz.uzinfocom.app.platform.iam.web.role;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.iam.application.permission.query.dto.PermissionDetailResponse;
import uz.uzinfocom.app.platform.iam.application.role.command.RoleCommandService;
import uz.uzinfocom.app.platform.iam.application.role.command.dto.RoleCreateRequest;
import uz.uzinfocom.app.platform.iam.application.role.command.dto.RolePermissionUpdateRequest;
import uz.uzinfocom.app.platform.iam.application.role.command.dto.RoleUpdateRequest;
import uz.uzinfocom.app.platform.iam.application.role.query.RoleQueryService;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RoleDetailResponse;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RoleFilterRequest;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RoleTableResponse;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.response.ApiResponse;
import uz.uzinfocom.app.shared.response.ErrorResponse;
import uz.uzinfocom.app.shared.response.PagedResponse;

@Tag(
        name = "Role",
        description = "API для управления ролями пользователей и их правами доступа."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Role.BASE)
@RequiredArgsConstructor
public class RoleController {

    private final RoleQueryService roleQueryService;
    private final RoleCommandService roleCommandService;
    private final MessageResolver messages;

    @Operation(
            summary = "Получить список ролей",
            description = "Возвращает постраничный список ролей с фильтрацией по наименованию, описанию и признаку активности."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный запрос.")
    @GetMapping
    public PagedResponse<RoleTableResponse> getAll(
            @ParameterObject @Valid @ModelAttribute RoleFilterRequest request
    ) {
        Page<RoleTableResponse> page = roleQueryService.findTable(request);
        return PagedResponse.fromPage(page, messages.resolve("common.success"));
    }

    @Operation(
            summary = "Получить роль по идентификатору",
            description = "Возвращает детальную информацию о роли, включая назначенные права доступа и действия."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный запрос.")
    @GetMapping(ApiPaths.Role.BY_ID)
    public ApiResponse<RoleDetailResponse> getById(
            @Parameter(description = "Уникальный идентификатор роли.", required = true)
            @PathVariable @Positive Long id
    ) {
        RoleDetailResponse response = roleQueryService.findDetail(id);
        return ApiResponse.success(messages.resolve("common.success"), response);
    }

    @Operation(
            summary = "Создать роль",
            description = "Создает новую роль пользователя для дальнейшего назначения в рамках организаций."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Запись успешно создана.")
    @PostMapping
    public ApiResponse<RoleDetailResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные создаваемой роли.",
                    required = true
            )
            @Valid @RequestBody RoleCreateRequest request
    ) {
        RoleDetailResponse response = roleCommandService.create(request);
        return ApiResponse.success(messages.resolve("role.created"), response);
    }

    @Operation(
            summary = "Обновить роль",
            description = "Обновляет наименование, описания и признак активности роли."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный запрос.")
    @PutMapping(ApiPaths.Role.BY_ID)
    public ApiResponse<RoleDetailResponse> update(
            @Parameter(description = "Уникальный идентификатор роли.", required = true)
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новые данные роли.",
                    required = true
            )
            @Valid @RequestBody RoleUpdateRequest request
    ) {
        RoleDetailResponse response = roleCommandService.update(id, request);
        return ApiResponse.success(messages.resolve("role.updated"), response);
    }

    @Operation(
            summary = "Удалить роль",
            description = "Выполняет мягкое удаление роли, после чего она не используется при авторизации."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Операция выполнена успешно.")
    @DeleteMapping(ApiPaths.Role.BY_ID)
    public ApiResponse<Void> delete(
            @Parameter(description = "Уникальный идентификатор роли.", required = true)
            @PathVariable @Positive Long id
    ) {
        roleCommandService.delete(id);
        return ApiResponse.success(messages.resolve("role.deleted"), null);
    }

    @Operation(
            summary = "Восстановить роль",
            description = "Восстанавливает мягко удаленную роль и делает ее активной."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Операция выполнена успешно.")
    @PatchMapping(ApiPaths.Role.RESTORE)
    public ApiResponse<Void> restore(
            @Parameter(description = "Уникальный идентификатор роли.", required = true)
            @PathVariable @Positive Long id
    ) {
        roleCommandService.restore(id);
        return ApiResponse.success(messages.resolve("role.restored"), null);
    }

    @Operation(
            summary = "Получить права роли",
            description = "Возвращает список прав доступа, назначенных роли, включая разрешенные действия."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный запрос.")
    @GetMapping(ApiPaths.Role.PERMISSIONS)
    public ApiResponse<java.util.List<PermissionDetailResponse>> getPermissions(
            @Parameter(description = "Уникальный идентификатор роли.", required = true)
            @PathVariable @Positive Long id
    ) {
        java.util.List<PermissionDetailResponse> response = roleQueryService.findPermissions(id);
        return ApiResponse.success(messages.resolve("common.success"), response);
    }

    @Operation(
            summary = "Добавить права роли",
            description = "Добавляет или объединяет права доступа роли. Если право уже назначено, действия объединяются."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный запрос.")
    @PatchMapping(ApiPaths.Role.PERMISSIONS)
    public ApiResponse<RoleDetailResponse> addPermissions(
            @Parameter(description = "Уникальный идентификатор роли.", required = true)
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Список прав доступа и действий для добавления.",
                    required = true
            )
            @Valid @RequestBody RolePermissionUpdateRequest request
    ) {
        RoleDetailResponse response = roleCommandService.addPermissions(id, request);
        return ApiResponse.success(messages.resolve("role.permissions.added"), response);
    }

    @Operation(
            summary = "Заменить права роли",
            description = "Полностью заменяет список прав доступа роли указанным набором."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный запрос.")
    @PutMapping(ApiPaths.Role.PERMISSIONS)
    public ApiResponse<RoleDetailResponse> replacePermissions(
            @Parameter(description = "Уникальный идентификатор роли.", required = true)
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новый полный список прав доступа и действий роли.",
                    required = true
            )
            @Valid @RequestBody RolePermissionUpdateRequest request
    ) {
        RoleDetailResponse response = roleCommandService.replacePermissions(id, request);
        return ApiResponse.success(messages.resolve("role.permissions.replaced"), response);
    }

    @Operation(
            summary = "Удалить права роли",
            description = "Удаляет указанные действия из прав роли. Если действий не остается, право снимается с роли."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный запрос.")
    @PatchMapping(ApiPaths.Role.REMOVE_PERMISSIONS)
    public ApiResponse<RoleDetailResponse> removePermissions(
            @Parameter(description = "Уникальный идентификатор роли.", required = true)
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Список прав доступа и действий для удаления.",
                    required = true
            )
            @Valid @RequestBody RolePermissionUpdateRequest request
    ) {
        RoleDetailResponse response = roleCommandService.removePermissions(id, request);
        return ApiResponse.success(messages.resolve("role.permissions.removed"), response);
    }
}
