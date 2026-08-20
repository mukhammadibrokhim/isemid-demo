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
import uz.uzinfocom.app.modules.iam.application.permission.command.PermissionCommandService;
import uz.uzinfocom.app.modules.iam.application.permission.command.dto.PermissionCreateRequest;
import uz.uzinfocom.app.modules.iam.application.permission.command.dto.PermissionUpdateRequest;
import uz.uzinfocom.app.modules.iam.application.permission.query.PermissionQueryService;
import uz.uzinfocom.app.modules.iam.application.permission.query.dto.PermissionDetailResponse;
import uz.uzinfocom.app.modules.iam.application.permission.query.dto.PermissionFilterRequest;
import uz.uzinfocom.app.modules.iam.application.permission.query.dto.PermissionTableResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

/**
 * Dev-panel access to the RBAC {@code Permission} ("subject") entity -
 * delegates directly to the same {@link PermissionQueryService} /
 * {@link PermissionCommandService} used by the SSO-facing
 * {@code PermissionController}, since {@code Permission} is a shared
 * production entity, not a dev-panel-only lookup. Reads are open to any
 * authenticated dev-panel account; create/update require
 * {@code ROLE_DEV_ADMIN}; delete/restore require {@code ROLE_DEV_SUPER_ADMIN}
 * - same convention as every other {@code /v1/dev/**} controller.
 */
@Tag(
        name = "Dev Panel - Permission",
        description = "Dev-panel CRUD for RBAC permissions (subjects)."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Dev.ROOT)
@RequiredArgsConstructor
public class DevPermissionController {

    private final PermissionQueryService permissionQueryService;
    private final PermissionCommandService permissionCommandService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(summary = "List permissions (subjects)")
    @GetMapping(ApiPaths.Dev.PERMISSIONS)
    public PagedResponse<PermissionTableResponse> findAll(
            @ParameterObject @Valid @ModelAttribute PermissionFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<PermissionTableResponse> page = permissionQueryService.findTable(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(summary = "Get a permission (subject) by id")
    @GetMapping(ApiPaths.Dev.PERMISSION_BY_ID)
    public ApiResponse<PermissionDetailResponse> getById(
            @Parameter(description = "Unique identifier of the permission.", required = true)
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), permissionQueryService.getById(id));
    }

    @Operation(summary = "Create a permission (subject)")
    @PostMapping(ApiPaths.Dev.PERMISSIONS)
    @PreAuthorize("hasRole('DEV_ADMIN')")
    public ApiResponse<PermissionTableResponse> create(@Valid @RequestBody PermissionCreateRequest request) {
        return ApiResponse.success(messageResolver.resolve("permission.created"), permissionCommandService.create(request));
    }

    @Operation(summary = "Update a permission (subject)")
    @PutMapping(ApiPaths.Dev.PERMISSION_BY_ID)
    @PreAuthorize("hasRole('DEV_ADMIN')")
    public ApiResponse<PermissionTableResponse> update(
            @Parameter(description = "Unique identifier of the permission.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody PermissionUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("permission.updated"), permissionCommandService.update(id, request));
    }

    @Operation(
            summary = "Delete a permission (subject)",
            description = "Soft-deletes the permission; it stops being used during authorization."
    )
    @DeleteMapping(ApiPaths.Dev.PERMISSION_BY_ID)
    @PreAuthorize("hasRole('DEV_SUPER_ADMIN')")
    public ApiResponse<Void> delete(
            @Parameter(description = "Unique identifier of the permission.", required = true)
            @PathVariable @Positive Long id
    ) {
        permissionCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("permission.deleted"), null);
    }

    @Operation(summary = "Restore a soft-deleted permission (subject)")
    @PatchMapping(ApiPaths.Dev.PERMISSION_RESTORE)
    @PreAuthorize("hasRole('DEV_SUPER_ADMIN')")
    public ApiResponse<Void> restore(
            @Parameter(description = "Unique identifier of the permission.", required = true)
            @PathVariable @Positive Long id
    ) {
        permissionCommandService.restore(id);
        return ApiResponse.success(messageResolver.resolve("permission.restored"), null);
    }
}
