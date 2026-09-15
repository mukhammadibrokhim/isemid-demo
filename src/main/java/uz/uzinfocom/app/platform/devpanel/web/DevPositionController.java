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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.platform.devpanel.application.command.DevPositionCommandService;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevPositionCreateRequest;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevPositionUpdateRequest;
import uz.uzinfocom.app.platform.devpanel.application.query.DevPositionQueryService;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevPositionFilterRequest;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevPositionResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

/**
 * Dev-panel-only lookup ("spravochnik") of positions/departments, assignable
 * to a {@code DevUser}'s profile (see {@code DevUser.position}) - separate
 * from the org-facing, multi-language {@code modules.reference} dictionaries.
 * Reads are open to any authenticated dev-panel account (see
 * {@code DevPanelSecurityConfig}); create/update require {@code ROLE_DEV_ADMIN}
 * and delete requires {@code ROLE_DEV_SUPER_ADMIN} - same convention as every
 * other {@code /v1/dev/**} controller.
 */
@Tag(
        name = "Dev Panel - References",
        description = "Dev-panel-only lookups (positions/departments) assignable to a dev-panel account's profile."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Dev.ROOT)
@RequiredArgsConstructor
public class DevPositionController {

    private final DevPositionQueryService devPositionQueryService;
    private final DevPositionCommandService devPositionCommandService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(summary = "List dev-panel positions/departments")
    @GetMapping(ApiPaths.Dev.REF_POSITIONS)
    public PagedResponse<DevPositionResponse> findAll(
            @ParameterObject @Valid @ModelAttribute DevPositionFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<DevPositionResponse> page = devPositionQueryService.findAll(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(summary = "Get a dev-panel position/department by id")
    @GetMapping(ApiPaths.Dev.REF_POSITION_BY_ID)
    public ApiResponse<DevPositionResponse> getById(
            @Parameter(description = "Internal id of the position/department.", required = true)
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), devPositionQueryService.getById(id));
    }

    @Operation(summary = "Create a dev-panel position/department")
    @PostMapping(ApiPaths.Dev.REF_POSITIONS)
    @PreAuthorize("hasRole('DEV_ADMIN')")
    public ApiResponse<DevPositionResponse> create(@Valid @RequestBody DevPositionCreateRequest request) {
        return ApiResponse.success(messageResolver.resolve("common.created"), devPositionCommandService.create(request));
    }

    @Operation(summary = "Update a dev-panel position/department")
    @PutMapping(ApiPaths.Dev.REF_POSITION_BY_ID)
    @PreAuthorize("hasRole('DEV_ADMIN')")
    public ApiResponse<DevPositionResponse> update(
            @Parameter(description = "Internal id of the position/department.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody DevPositionUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.updated"), devPositionCommandService.update(id, request));
    }

    @Operation(
            summary = "Delete a dev-panel position/department",
            description = "Fails if the position is currently assigned to any dev-panel account."
    )
    @DeleteMapping(ApiPaths.Dev.REF_POSITION_BY_ID)
    @PreAuthorize("hasRole('DEV_SUPER_ADMIN')")
    public ApiResponse<Void> delete(
            @Parameter(description = "Internal id of the position/department.", required = true)
            @PathVariable @Positive Long id
    ) {
        devPositionCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"));
    }
}
