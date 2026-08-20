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
import uz.uzinfocom.app.modules.iam.application.action.command.ActionCommandService;
import uz.uzinfocom.app.modules.iam.application.action.command.dto.ActionCreateRequest;
import uz.uzinfocom.app.modules.iam.application.action.command.dto.ActionUpdateRequest;
import uz.uzinfocom.app.modules.iam.application.action.query.ActionQueryService;
import uz.uzinfocom.app.modules.iam.application.action.query.dto.ActionDetailResponse;
import uz.uzinfocom.app.modules.iam.application.action.query.dto.ActionFilterRequest;
import uz.uzinfocom.app.modules.iam.application.action.query.dto.ActionTableResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

/**
 * Dev-panel access to the RBAC {@code Action} entity - delegates directly to
 * the same {@link ActionQueryService} / {@link ActionCommandService} used by
 * the SSO-facing {@code ActionController}, since {@code Action} is a shared
 * production entity, not a dev-panel-only lookup. Reads are open to any
 * authenticated dev-panel account; create/update require
 * {@code ROLE_DEV_ADMIN}; delete/restore require {@code ROLE_DEV_SUPER_ADMIN}
 * - same convention as every other {@code /v1/dev/**} controller.
 */
@Tag(
        name = "Dev Panel - Action",
        description = "Dev-panel CRUD for RBAC actions."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Dev.ROOT)
@RequiredArgsConstructor
public class DevActionController {

    private final ActionQueryService actionQueryService;
    private final ActionCommandService actionCommandService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(summary = "List actions")
    @GetMapping(ApiPaths.Dev.ACTIONS)
    public PagedResponse<ActionTableResponse> findAll(
            @ParameterObject @Valid @ModelAttribute ActionFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<ActionTableResponse> page = actionQueryService.findTable(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(summary = "Get an action by id")
    @GetMapping(ApiPaths.Dev.ACTION_BY_ID)
    public ApiResponse<ActionDetailResponse> getById(
            @Parameter(description = "Unique identifier of the action.", required = true)
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), actionQueryService.getById(id));
    }

    @Operation(summary = "Create an action")
    @PostMapping(ApiPaths.Dev.ACTIONS)
    @PreAuthorize("hasRole('DEV_ADMIN')")
    public ApiResponse<ActionTableResponse> create(@Valid @RequestBody ActionCreateRequest request) {
        return ApiResponse.success(messageResolver.resolve("action.created"), actionCommandService.create(request));
    }

    @Operation(summary = "Update an action")
    @PutMapping(ApiPaths.Dev.ACTION_BY_ID)
    @PreAuthorize("hasRole('DEV_ADMIN')")
    public ApiResponse<ActionTableResponse> update(
            @Parameter(description = "Unique identifier of the action.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody ActionUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("action.updated"), actionCommandService.update(id, request));
    }

    @Operation(
            summary = "Delete an action",
            description = "Soft-deletes the action; it stops being used during authorization."
    )
    @DeleteMapping(ApiPaths.Dev.ACTION_BY_ID)
    @PreAuthorize("hasRole('DEV_SUPER_ADMIN')")
    public ApiResponse<Void> delete(
            @Parameter(description = "Unique identifier of the action.", required = true)
            @PathVariable @Positive Long id
    ) {
        actionCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("action.deleted"), null);
    }

    @Operation(summary = "Restore a soft-deleted action")
    @PatchMapping(ApiPaths.Dev.ACTION_RESTORE)
    @PreAuthorize("hasRole('DEV_SUPER_ADMIN')")
    public ApiResponse<Void> restore(
            @Parameter(description = "Unique identifier of the action.", required = true)
            @PathVariable @Positive Long id
    ) {
        actionCommandService.restore(id);
        return ApiResponse.success(messageResolver.resolve("action.restored"), null);
    }
}
