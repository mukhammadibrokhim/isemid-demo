package uz.uzinfocom.app.platform.devmonitoring.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.platform.devmonitoring.application.command.DevUserCommandService;
import uz.uzinfocom.app.platform.devmonitoring.application.command.dto.DevUserCreateRequest;
import uz.uzinfocom.app.platform.devmonitoring.application.command.dto.DevUserCreateResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

@Tag(
        name = "Admin - Dev Monitoring Accounts",
        description = "Provisioning for developer-monitoring-panel logins (/v1/dev/**) - a separate, "
                + "local credential system for an internal ops tool, distinct from the SSO/DHP-issued "
                + "bearer tokens every other endpoint requires."
)
@RestController
@RequestMapping(ApiPaths.DevUser.ROOT)
@RequiredArgsConstructor
public class DevUserAdminController {

    private final DevUserCommandService devUserCommandService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Provision a dev-panel account",
            description = "Creates a new local login for the developer monitoring panel and returns a "
                    + "one-time password - it is never stored in retrievable form and cannot be shown again."
    )
    @PostMapping
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<DevUserCreateResponse> create(@Valid @RequestBody DevUserCreateRequest request) {
        return ApiResponse.success(messageResolver.resolve("common.created"), devUserCommandService.create(request));
    }

    @Operation(
            summary = "Revoke a dev-panel account",
            description = "Disables a dev-panel account; it can no longer authenticate against /v1/dev/**."
    )
    @PatchMapping(ApiPaths.DevUser.REVOKE)
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<Void> revoke(
            @Parameter(description = "Internal id of the dev-panel account.", required = true)
            @PathVariable @Positive Long id
    ) {
        devUserCommandService.revoke(id);
        return ApiResponse.success(messageResolver.resolve("common.updated"));
    }
}
