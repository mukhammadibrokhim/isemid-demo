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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.platform.devpanel.application.command.DevUserCommandService;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevUserCreateRequest;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevUserCreateResponse;
import uz.uzinfocom.app.platform.devpanel.application.query.DevUserQueryService;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevUserDetailResponse;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevUserFilterRequest;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

/**
 * Provisioning for developer-monitoring-panel logins ({@code /v1/dev/**}) -
 * a separate, local credential system for an internal ops tool, distinct
 * from the SSO/DHP-issued bearer tokens every other endpoint requires.
 *
 * <p>Deliberately reached and gated entirely within the {@code /v1/dev/**}
 * chain (see {@code DevPanelSecurityConfig}) rather than the SSO-admin
 * surface: every method here requires {@code ROLE_DEV_ROOT} (see
 * {@code DevUserPrincipal}), granted only to a {@code DevUser} with
 * {@code root = true}. Not even an SSO {@code isemid_super_admin} can
 * create, list, or revoke a dev-panel account - only another root
 * dev-user can.
 */
@Tag(
        name = "Dev Monitoring - Accounts",
        description = "Provisioning for developer-monitoring-panel logins (/v1/dev/**) - a separate, "
                + "local credential system for an internal ops tool, distinct from the SSO/DHP-issued "
                + "bearer tokens every other endpoint requires. Restricted to root dev-panel accounts."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Dev.ROOT)
@RequiredArgsConstructor
public class DevUserController {

    private final DevUserQueryService devUserQueryService;
    private final DevUserCommandService devUserCommandService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(summary = "List dev-panel accounts")
    @GetMapping(ApiPaths.Dev.DEV_USERS)
    @PreAuthorize("hasRole('DEV_ROOT')")
    public PagedResponse<DevUserDetailResponse> findAll(
            @ParameterObject @Valid @ModelAttribute DevUserFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<DevUserDetailResponse> page = devUserQueryService.findAll(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(summary = "Get a dev-panel account by id")
    @GetMapping(ApiPaths.Dev.DEV_USER_BY_ID)
    @PreAuthorize("hasRole('DEV_ROOT')")
    public ApiResponse<DevUserDetailResponse> getById(
            @Parameter(description = "Internal id of the dev-panel account.", required = true)
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), devUserQueryService.getById(id));
    }

    @Operation(
            summary = "Provision a dev-panel account",
            description = "Creates a new local login for the developer monitoring panel and returns a "
                    + "one-time password - it is never stored in retrievable form and cannot be shown again."
    )
    @PostMapping(ApiPaths.Dev.DEV_USERS)
    @PreAuthorize("hasRole('DEV_ROOT')")
    public ApiResponse<DevUserCreateResponse> create(@Valid @RequestBody DevUserCreateRequest request) {
        return ApiResponse.success(messageResolver.resolve("common.created"), devUserCommandService.create(request));
    }

    @Operation(
            summary = "Revoke a dev-panel account",
            description = "Disables a dev-panel account; it can no longer authenticate against /v1/dev/**."
    )
    @PatchMapping(ApiPaths.Dev.DEV_USER_REVOKE)
    @PreAuthorize("hasRole('DEV_ROOT')")
    public ApiResponse<Void> revoke(
            @Parameter(description = "Internal id of the dev-panel account.", required = true)
            @PathVariable @Positive Long id
    ) {
        devUserCommandService.revoke(id);
        return ApiResponse.success(messageResolver.resolve("common.updated"));
    }
}
