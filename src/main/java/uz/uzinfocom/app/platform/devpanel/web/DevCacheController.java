package uz.uzinfocom.app.platform.devpanel.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.app.platform.devpanel.application.command.DevCacheCommandService;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevCacheClearResponse;
import uz.uzinfocom.app.platform.devpanel.application.query.DevCacheQueryService;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevCacheResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.util.List;

@Tag(
        name = "Dev Panel - Caches",
        description = "Inspect and clear the application's in-memory caches (the shared securityCacheManager). "
                + "Listing is open to any dev-panel account; clearing one cache requires ROLE_DEV_ADMIN, "
                + "clearing all requires ROLE_DEV_SUPER_ADMIN. The revoked-token blacklist is never cleared."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Dev.ROOT)
@RequiredArgsConstructor
public class DevCacheController {

    private final DevCacheQueryService queryService;
    private final DevCacheCommandService commandService;
    private final MessageResolver messageResolver;

    @Operation(summary = "List all caches with size and hit/miss stats")
    @GetMapping(ApiPaths.Dev.CACHES)
    public ApiResponse<List<DevCacheResponse>> findAll() {
        return ApiResponse.success(messageResolver.resolve("common.success"), queryService.findAll());
    }

    @Operation(summary = "Clear a single cache")
    @DeleteMapping(ApiPaths.Dev.CACHE_BY_NAME)
    @PreAuthorize("hasRole('DEV_ADMIN')")
    public ApiResponse<DevCacheClearResponse> clear(
            @Parameter(description = "Cache name from the list endpoint.", required = true)
            @PathVariable @NotBlank String name
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), commandService.clear(name));
    }

    @Operation(summary = "Clear all caches (except protected ones)")
    @DeleteMapping(ApiPaths.Dev.CACHES)
    @PreAuthorize("hasRole('DEV_SUPER_ADMIN')")
    public ApiResponse<DevCacheClearResponse> clearAll() {
        return ApiResponse.success(messageResolver.resolve("common.success"), commandService.clearAll());
    }
}
