package uz.uzinfocom.app.platform.settings.web;

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
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.settings.application.command.RouteAccessPolicyCommandService;
import uz.uzinfocom.app.platform.settings.application.dto.RouteAccessPolicyCreateRequest;
import uz.uzinfocom.app.platform.settings.application.dto.RouteAccessPolicyUpdateRequest;
import uz.uzinfocom.app.platform.settings.application.query.RouteAccessPolicyQueryService;
import uz.uzinfocom.app.platform.settings.application.query.dto.RouteAccessPolicyDetailResponse;
import uz.uzinfocom.app.platform.settings.application.query.dto.RouteAccessPolicyFilterRequest;
import uz.uzinfocom.app.platform.settings.application.query.dto.RouteAccessPolicyResponse;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

@Tag(
        name = "Admin - Route Access Policy",
        description = "Runtime-editable route-access rules (public/authenticated, org-header, role validation) "
                + "- replaces the old restart-only SecurityRouteCatalog."
)
@Validated
@RestController
@RequestMapping(ApiPaths.RouteAccessPolicy.ROOT)
@RequiredArgsConstructor
public class RouteAccessPolicyController {

    private final RouteAccessPolicyQueryService routeAccessPolicyQueryService;
    private final RouteAccessPolicyCommandService routeAccessPolicyCommandService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<RouteAccessPolicyResponse> findAll(
            @ParameterObject @Valid @ModelAttribute RouteAccessPolicyFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<RouteAccessPolicyResponse> page = routeAccessPolicyQueryService.findAll(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @PostMapping
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<RouteAccessPolicyResponse> create(@Valid @RequestBody RouteAccessPolicyCreateRequest request) {
        return ApiResponse.success(
                messageResolver.resolve("common.created"), routeAccessPolicyCommandService.create(request)
        );
    }

    @GetMapping(ApiPaths.RouteAccessPolicy.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<RouteAccessPolicyDetailResponse> getById(
            @Parameter(description = "Internal id of the route-access policy row.", required = true)
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), routeAccessPolicyQueryService.getById(id));
    }

    @PutMapping(ApiPaths.RouteAccessPolicy.BY_ID)
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<RouteAccessPolicyResponse> update(
            @Parameter(description = "Internal id of the route-access policy row.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody RouteAccessPolicyUpdateRequest request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.updated"), routeAccessPolicyCommandService.update(id, request)
        );
    }

    @DeleteMapping(ApiPaths.RouteAccessPolicy.BY_ID)
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<Void> delete(
            @Parameter(description = "Internal id of the route-access policy row.", required = true)
            @PathVariable @Positive Long id
    ) {
        routeAccessPolicyCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"));
    }
}
