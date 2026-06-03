package uz.uzinfocom.app.platform.iam.web.permission;

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
import uz.uzinfocom.app.platform.iam.application.permission.command.PermissionCommandService;
import uz.uzinfocom.app.platform.iam.application.permission.command.dto.PermissionCreateRequest;
import uz.uzinfocom.app.platform.iam.application.permission.command.dto.PermissionUpdateRequest;
import uz.uzinfocom.app.platform.iam.application.permission.query.PermissionQueryService;
import uz.uzinfocom.app.platform.iam.application.permission.query.dto.PermissionDetailResponse;
import uz.uzinfocom.app.platform.iam.application.permission.query.dto.PermissionFilterRequest;
import uz.uzinfocom.app.platform.iam.application.permission.query.dto.PermissionTableResponse;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.response.ApiResponse;
import uz.uzinfocom.app.shared.response.ErrorResponse;
import uz.uzinfocom.app.shared.response.PagedResponse;

@Tag(
        name = "Permission",
        description = "API для просмотра и управления правами доступа."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Permission.BASE)
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionQueryService permissionQueryService;
    private final PermissionCommandService permissionCommandService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Получить список прав доступа",
            description = "Возвращает постраничный список прав доступа с фильтрацией по субъекту и признаку активности."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный запрос.")
    @GetMapping
    public PagedResponse<PermissionTableResponse> getAll(
           @ParameterObject @Valid @ModelAttribute PermissionFilterRequest request
    ) {
        Page<PermissionTableResponse> responses = permissionQueryService.findTable(request);
        return PagedResponse.fromPage(responses, messageResolver.resolve("common.success"));
    }

    @Operation(
            summary = "Получить право доступа по идентификатору",
            description = "Возвращает детальную информацию о праве доступа."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный запрос.")
    @GetMapping(ApiPaths.Permission.BY_ID)
    public ApiResponse<PermissionDetailResponse> getById(
            @Parameter(description = "Уникальный идентификатор права доступа.", required = true)
            @PathVariable @Positive Long id
    ) {
        PermissionDetailResponse response = permissionQueryService.getById(id);
        return ApiResponse.success(messageResolver.resolve("common.success"), response);
    }

    @Operation(
            summary = "Создать право доступа",
            description = "Создает новое право доступа для последующего назначения ролям."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Запись успешно создана.")
    @PostMapping
    public ApiResponse<PermissionTableResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные создаваемого права доступа.",
                    required = true
            )
            @Valid @RequestBody PermissionCreateRequest request
    ) {
        PermissionTableResponse response = permissionCommandService.create(request);
        return ApiResponse.success(messageResolver.resolve("permission.created"), response);
    }

    @Operation(
            summary = "Обновить право доступа",
            description = "Обновляет субъект, описания и признак активности права доступа."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный запрос.")
    @PutMapping(ApiPaths.Permission.BY_ID)
    public ApiResponse<PermissionTableResponse> update(
            @Parameter(description = "Уникальный идентификатор права доступа.", required = true)
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новые данные права доступа.",
                    required = true
            )
            @Valid @RequestBody PermissionUpdateRequest request
    ) {
        PermissionTableResponse response = permissionCommandService.update(id, request);
        return ApiResponse.success(messageResolver.resolve("permission.updated"), response);
    }

    @Operation(
            summary = "Удалить право доступа",
            description = "Выполняет мягкое удаление права доступа, после чего оно не используется при авторизации."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Операция выполнена успешно.")
    @DeleteMapping(ApiPaths.Permission.BY_ID)
    public ApiResponse<Void> deleteById(
            @Parameter(description = "Уникальный идентификатор права доступа.", required = true)
            @PathVariable @Positive Long id
    ) {
        permissionCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("permission.deleted"), null);
    }

    @Operation(
            summary = "Восстановить право доступа",
            description = "Восстанавливает мягко удаленное право доступа и делает его активным."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Операция выполнена успешно.")
    @PatchMapping(ApiPaths.Permission.RESTORE)
    public ApiResponse<Void> restore(
            @Parameter(description = "Уникальный идентификатор права доступа.", required = true)
            @PathVariable @Positive Long id
    ) {
        permissionCommandService.restore(id);
        return ApiResponse.success(messageResolver.resolve("permission.restored"), null);
    }
}
