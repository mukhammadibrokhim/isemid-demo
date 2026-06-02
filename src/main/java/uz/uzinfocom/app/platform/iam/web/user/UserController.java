package uz.uzinfocom.app.platform.iam.web.user;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.iam.application.user.command.UserOrganizationRoleCommandService;
import uz.uzinfocom.app.platform.iam.application.user.command.dto.UserOrganizationRolesRequest;
import uz.uzinfocom.app.platform.iam.application.user.query.UserOrganizationRoleQueryService;
import uz.uzinfocom.app.platform.iam.application.user.query.UserQueryService;
import uz.uzinfocom.app.platform.iam.application.user.query.dto.UserOrganizationRoleAssignmentResponse;
import uz.uzinfocom.app.platform.iam.application.user.query.dto.UserOrganizationRolesResponse;
import uz.uzinfocom.app.platform.iam.web.user.dto.request.UserFilterRequest;
import uz.uzinfocom.app.platform.iam.web.user.dto.response.UserDetailedResponse;
import uz.uzinfocom.app.platform.iam.web.user.dto.response.UserTableResponse;
import uz.uzinfocom.app.platform.web.api.ApiPaths;
import uz.uzinfocom.app.platform.web.response.ApiResponse;
import uz.uzinfocom.app.platform.web.response.ErrorResponse;
import uz.uzinfocom.app.platform.web.response.PagedResponse;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Пользователи",
        description = "API для управления пользователями, организациями пользователя и ролями в рамках организации."
)
@ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Некорректный запрос.",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Пользователь не авторизован.",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Доступ запрещён.",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Запись не найдена.",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Конфликт данных.",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
@RestController
@RequestMapping(ApiPaths.User.BASE)
@RequiredArgsConstructor
public class UserController {

    private final UserQueryService userQueryService;
    private final UserOrganizationRoleQueryService userOrganizationRoleQueryService;
    private final UserOrganizationRoleCommandService userOrganizationRoleCommandService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Получить список пользователей",
            description = "Возвращает постраничный список пользователей с фильтрами по ФИО, ННУЗБ, телефону и признаку активности."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный запрос.")
    @GetMapping
    public PagedResponse<UserTableResponse> findAll(@ParameterObject @Valid @ModelAttribute UserFilterRequest request) {
        Page<UserTableResponse> page = userQueryService.findTable(request);
        return PagedResponse.fromPage(page, messageResolver.resolve("common.success"));
    }

    @Operation(
            summary = "Получить пользователя по идентификатору",
            description = "Возвращает детальную карточку пользователя, включая организации и роли пользователя в организациях."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный запрос.")
    @GetMapping(ApiPaths.User.BY_ID)
    public ApiResponse<UserDetailedResponse> findById(
            @Parameter(description = "Уникальный идентификатор пользователя.", required = true)
            @PathVariable Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), userQueryService.getRequiredById(id));
    }

    @Operation(
            summary = "Получить пользователя по UUID",
            description = "Возвращает детальную карточку пользователя по UUID."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный запрос.")
    @GetMapping(ApiPaths.User.BY_UUID)
    public ApiResponse<UserDetailedResponse> findByUuid(
            @Parameter(description = "Уникальный UUID пользователя.", required = true)
            @PathVariable UUID uuid
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), userQueryService.getRequiredByUuid(uuid));
    }

    @Operation(
            summary = "Получить организации пользователя",
            description = "Возвращает организации пользователя, роли и права доступа внутри каждой организации."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный запрос.")
    @GetMapping(ApiPaths.User.ORGANIZATIONS)
    public ApiResponse<List<UserOrganizationRolesResponse>> getUserOrganizations(
            @Parameter(description = "Уникальный идентификатор пользователя.", required = true)
            @PathVariable(ApiPaths.User.ID) Long id
    ) {
        List<UserOrganizationRolesResponse> response =
                userOrganizationRoleQueryService.findOrganizationsWithRoles(id);
        return ApiResponse.success(messageResolver.resolve("common.success"), response);
    }

    @Operation(
            summary = "Добавить пользователя в организацию",
            description = "Создает связь пользователя с организацией без назначения роли."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Операция выполнена успешно.")
    @PutMapping(ApiPaths.User.ORGANIZATION)
    public ApiResponse<List<UserOrganizationRolesResponse>> assignUserToOrganization(
            @Parameter(description = "Уникальный идентификатор пользователя.", required = true)
            @PathVariable(ApiPaths.User.ID) Long id,
            @Parameter(description = "Уникальный идентификатор организации.", required = true)
            @PathVariable(ApiPaths.User.ORGANIZATION_ID) Long organizationId
    ) {
        List<UserOrganizationRolesResponse> response =
                userOrganizationRoleCommandService.assignUserToOrganization(id, organizationId);
        return ApiResponse.success(messageResolver.resolve("user.organization.assigned"), response);
    }

    @Operation(
            summary = "Удалить пользователя из организации",
            description = "Удаляет связь пользователя с организацией и связанные организационные роли согласно текущей бизнес-логике."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Операция выполнена успешно.")
    @DeleteMapping(ApiPaths.User.ORGANIZATION)
    public ApiResponse<List<UserOrganizationRolesResponse>> removeUserFromOrganization(
            @Parameter(description = "Уникальный идентификатор пользователя.", required = true)
            @PathVariable(ApiPaths.User.ID) Long id,
            @Parameter(description = "Уникальный идентификатор организации.", required = true)
            @PathVariable(ApiPaths.User.ORGANIZATION_ID) Long organizationId
    ) {
        List<UserOrganizationRolesResponse> response =
                userOrganizationRoleCommandService.removeUserFromOrganization(id, organizationId);
        return ApiResponse.success(messageResolver.resolve("user.organization.removed"), response);
    }

    @Operation(
            summary = "Получить роли пользователя в организации",
            description = "Возвращает роли пользователя, назначенные строго в указанной организации."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный запрос.")
    @GetMapping(ApiPaths.User.ORGANIZATION_ROLES)
    public ApiResponse<UserOrganizationRoleAssignmentResponse> getUserRolesByOrganization(
            @Parameter(description = "Уникальный идентификатор пользователя.", required = true)
            @PathVariable(ApiPaths.User.ID) Long id,
            @Parameter(description = "Уникальный идентификатор организации.", required = true)
            @PathVariable(ApiPaths.User.ORGANIZATION_ID) Long organizationId
    ) {
        UserOrganizationRoleAssignmentResponse response =
                userOrganizationRoleQueryService.findUserRolesByOrganization(id, organizationId);
        return ApiResponse.success(messageResolver.resolve("common.success"), response);
    }

    @Operation(
            summary = "Добавить роли пользователю в организации",
            description = "Добавляет роли пользователю только в рамках выбранной организации."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный запрос.")
    @PatchMapping(ApiPaths.User.ORGANIZATION_ROLES)
    public ApiResponse<UserOrganizationRoleAssignmentResponse> addUserRolesByOrganization(
            @Parameter(description = "Уникальный идентификатор пользователя.", required = true)
            @PathVariable(ApiPaths.User.ID) Long id,
            @Parameter(description = "Уникальный идентификатор организации.", required = true)
            @PathVariable(ApiPaths.User.ORGANIZATION_ID) Long organizationId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Список ролей для назначения пользователю в организации.",
                    required = true
            )
            @Valid @RequestBody UserOrganizationRolesRequest request
    ) {
        UserOrganizationRoleAssignmentResponse response =
                userOrganizationRoleCommandService.addRoles(id, organizationId, request.roleIds());
        return ApiResponse.success(messageResolver.resolve("user.organization.roles.assigned"), response);
    }

    @Operation(
            summary = "Заменить роли пользователя в организации",
            description = "Полностью заменяет набор ролей пользователя в указанной организации."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный запрос.")
    @PutMapping(ApiPaths.User.ORGANIZATION_ROLES)
    public ApiResponse<UserOrganizationRoleAssignmentResponse> replaceUserRolesByOrganization(
            @Parameter(description = "Уникальный идентификатор пользователя.", required = true)
            @PathVariable(ApiPaths.User.ID) Long id,
            @Parameter(description = "Уникальный идентификатор организации.", required = true)
            @PathVariable(ApiPaths.User.ORGANIZATION_ID) Long organizationId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новый полный список ролей пользователя в организации.",
                    required = true
            )
            @Valid @RequestBody UserOrganizationRolesRequest request
    ) {
        UserOrganizationRoleAssignmentResponse response =
                userOrganizationRoleCommandService.replaceRoles(id, organizationId, request.roleIds());
        return ApiResponse.success(messageResolver.resolve("user.organization.roles.replaced"), response);
    }

    @Operation(
            summary = "Удалить роль пользователя в организации",
            description = "Снимает одну роль с пользователя только в указанной организации."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный запрос.")
    @DeleteMapping(ApiPaths.User.ORGANIZATION_ROLE)
    public ApiResponse<UserOrganizationRoleAssignmentResponse> removeUserRoleByOrganization(
            @Parameter(description = "Уникальный идентификатор пользователя.", required = true)
            @PathVariable(ApiPaths.User.ID) Long id,
            @Parameter(description = "Уникальный идентификатор организации.", required = true)
            @PathVariable(ApiPaths.User.ORGANIZATION_ID) Long organizationId,
            @Parameter(description = "Уникальный идентификатор роли.", required = true)
            @PathVariable(ApiPaths.User.ROLE_ID) Long roleId
    ) {
        UserOrganizationRoleAssignmentResponse response =
                userOrganizationRoleCommandService.removeRole(id, organizationId, roleId);
        return ApiResponse.success(messageResolver.resolve("user.organization.roles.removed"), response);
    }

    @Operation(
            summary = "Получить все организационные роли пользователя",
            description = "Возвращает все организации пользователя вместе с ролями и правами доступа в каждой организации."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный запрос.")
    @GetMapping(ApiPaths.User.ALL_ORGANIZATION_ROLES)
    public ApiResponse<List<UserOrganizationRolesResponse>> getAllOrganizationRoles(
            @Parameter(description = "Уникальный идентификатор пользователя.", required = true)
            @PathVariable(ApiPaths.User.ID) Long id
    ) {
        List<UserOrganizationRolesResponse> response =
                userOrganizationRoleQueryService.findOrganizationsWithRoles(id);
        return ApiResponse.success(messageResolver.resolve("common.success"), response);
    }
}
