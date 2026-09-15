package uz.uzinfocom.app.platform.devpanel.web;

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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.modules.iam.application.role.command.RoleCommandService;
import uz.uzinfocom.app.modules.iam.application.role.command.dto.RoleCreateRequest;
import uz.uzinfocom.app.modules.iam.application.role.command.dto.RolePermissionUpdateRequest;
import uz.uzinfocom.app.modules.iam.application.role.command.dto.RoleUpdateRequest;
import uz.uzinfocom.app.modules.iam.application.role.query.RoleQueryService;
import uz.uzinfocom.app.modules.iam.application.role.query.dto.RoleDetailResponse;
import uz.uzinfocom.app.modules.iam.application.role.query.dto.RoleFilterRequest;
import uz.uzinfocom.app.modules.iam.application.role.query.dto.RolePermissionResponse;
import uz.uzinfocom.app.modules.iam.application.role.query.dto.RoleTableResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

import java.util.List;

/**
 * Dev-panel access to the RBAC {@code Role} entity - delegates directly to
 * the same {@link RoleQueryService} / {@link RoleCommandService} used by the
 * SSO-facing {@code RoleController}, since {@code Role} is a shared
 * production entity, not a dev-panel-only lookup. Reads are open to any
 * authenticated dev-panel account; create/update/permission changes require
 * {@code ROLE_DEV_ADMIN}; delete/restore require {@code ROLE_DEV_SUPER_ADMIN}
 * - same convention as every other {@code /v1/dev/**} controller.
 */
@Tag(
        name = "Dev Panel - Role",
        description = "Dev-panel CRUD for RBAC roles."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Dev.ROOT)
@RequiredArgsConstructor
public class DevRoleController {

    private final RoleQueryService roleQueryService;
    private final RoleCommandService roleCommandService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(summary = "List roles")
    @GetMapping(ApiPaths.Dev.ROLES)
    public PagedResponse<RoleTableResponse> findAll(
            @ParameterObject @Valid @ModelAttribute RoleFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<RoleTableResponse> page = roleQueryService.findTable(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(summary = "Get a role by id")
    @GetMapping(ApiPaths.Dev.ROLE_BY_ID)
    public ApiResponse<RoleDetailResponse> getById(
            @Parameter(description = "Unique identifier of the role.", required = true)
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), roleQueryService.findDetail(id));
    }

    @Operation(summary = "Create a role")
    @PostMapping(ApiPaths.Dev.ROLES)
    @PreAuthorize("hasRole('DEV_ADMIN')")
    public ApiResponse<RoleDetailResponse> create(@Valid @RequestBody RoleCreateRequest request) {
        return ApiResponse.success(messageResolver.resolve("role.created"), roleCommandService.create(request));
    }

    @Operation(summary = "Update a role")
    @PutMapping(ApiPaths.Dev.ROLE_BY_ID)
    @PreAuthorize("hasRole('DEV_ADMIN')")
    public ApiResponse<RoleDetailResponse> update(
            @Parameter(description = "Unique identifier of the role.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody RoleUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("role.updated"), roleCommandService.update(id, request));
    }

    @Operation(
            summary = "Delete a role",
            description = "Soft-deletes the role; it stops being used during authorization."
    )
    @DeleteMapping(ApiPaths.Dev.ROLE_BY_ID)
    @PreAuthorize("hasRole('DEV_SUPER_ADMIN')")
    public ApiResponse<Void> delete(
            @Parameter(description = "Unique identifier of the role.", required = true)
            @PathVariable @Positive Long id
    ) {
        roleCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("role.deleted"), null);
    }

    @Operation(summary = "Restore a soft-deleted role")
    @PatchMapping(ApiPaths.Dev.ROLE_RESTORE)
    @PreAuthorize("hasRole('DEV_SUPER_ADMIN')")
    public ApiResponse<Void> restore(
            @Parameter(description = "Unique identifier of the role.", required = true)
            @PathVariable @Positive Long id
    ) {
        roleCommandService.restore(id);
        return ApiResponse.success(messageResolver.resolve("role.restored"), null);
    }

    @Operation(summary = "Get permissions assigned to a role")
    @GetMapping(ApiPaths.Dev.ROLE_PERMISSIONS)
    public ApiResponse<List<RolePermissionResponse>> getPermissions(
            @Parameter(description = "Unique identifier of the role.", required = true)
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), roleQueryService.findPermissions(id));
    }

    @Operation(
            summary = "Add permissions to a role",
            description = "Adds or merges permissions on the role; if a permission is already assigned, its actions are merged."
    )
    @PatchMapping(ApiPaths.Dev.ROLE_PERMISSIONS)
    @PreAuthorize("hasRole('DEV_ADMIN')")
    public ApiResponse<RoleDetailResponse> addPermissions(
            @Parameter(description = "Unique identifier of the role.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody RolePermissionUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("role.permissions.added"), roleCommandService.addPermissions(id, request));
    }

    @Operation(
            summary = "Replace a role's permissions",
            description = "Fully replaces the role's permission set with the given one."
    )
    @PutMapping(ApiPaths.Dev.ROLE_PERMISSIONS)
    @PreAuthorize("hasRole('DEV_ADMIN')")
    public ApiResponse<RoleDetailResponse> replacePermissions(
            @Parameter(description = "Unique identifier of the role.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody RolePermissionUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("role.permissions.replaced"), roleCommandService.replacePermissions(id, request));
    }

    @Operation(
            summary = "Remove permissions from a role",
            description = "Removes the given actions from the role's permissions; a permission is dropped entirely once no actions remain."
    )
    @PatchMapping(ApiPaths.Dev.ROLE_REMOVE_PERMISSIONS)
    @PreAuthorize("hasRole('DEV_ADMIN')")
    public ApiResponse<RoleDetailResponse> removePermissions(
            @Parameter(description = "Unique identifier of the role.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody RolePermissionUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("role.permissions.removed"), roleCommandService.removePermissions(id, request));
    }
}
