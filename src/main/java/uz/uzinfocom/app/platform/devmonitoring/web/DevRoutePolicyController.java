package uz.uzinfocom.app.platform.devmonitoring.web;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.settings.application.command.RouteAccessPolicyCommandService;
import uz.uzinfocom.app.platform.settings.application.dto.RouteAccessPolicyCreateRequest;
import uz.uzinfocom.app.platform.settings.application.dto.RouteAccessPolicyUpdateRequest;
import uz.uzinfocom.app.platform.settings.application.query.RouteAccessPolicyQueryService;
import uz.uzinfocom.app.platform.settings.application.query.dto.RouteAccessPolicyFilterRequest;
import uz.uzinfocom.app.platform.settings.application.query.dto.RouteAccessPolicyResponse;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

/**
 * Dev-panel mirror of {@code RouteAccessPolicyController} - same command/query
 * services and the same DB table, reached via the {@code DevUser} Basic-Auth
 * chain instead of an SSO admin token. No {@code @PreAuthorize} needed: the
 * whole {@code /v1/dev/**} chain already requires authentication
 * (see {@code DevPanelSecurityConfig}).
 */
@Tag(
        name = "Dev Monitoring - Route Policies",
        description = "Runtime route-access policy (public/authenticated, org-header, role validation) reachable "
                + "from the developer monitoring panel - same table as /v1/admin/route-policies, replaces the old "
                + "restart-only SecurityRouteCatalog."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Dev.ROOT)
@RequiredArgsConstructor
public class DevRoutePolicyController {

    private final RouteAccessPolicyQueryService routeAccessPolicyQueryService;
    private final RouteAccessPolicyCommandService routeAccessPolicyCommandService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @GetMapping(ApiPaths.Dev.ROUTE_POLICIES)
    public PagedResponse<RouteAccessPolicyResponse> findAll(
            @ParameterObject @Valid @ModelAttribute RouteAccessPolicyFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<RouteAccessPolicyResponse> page = routeAccessPolicyQueryService.findAll(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @PostMapping(ApiPaths.Dev.ROUTE_POLICIES)
    public ApiResponse<RouteAccessPolicyResponse> create(@Valid @RequestBody RouteAccessPolicyCreateRequest request) {
        return ApiResponse.success(
                messageResolver.resolve("common.created"), routeAccessPolicyCommandService.create(request)
        );
    }

    @GetMapping(ApiPaths.Dev.ROUTE_POLICY_BY_ID)
    public ApiResponse<RouteAccessPolicyResponse> getById(
            @Parameter(description = "Internal id of the route-access policy row.", required = true)
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), routeAccessPolicyQueryService.getById(id));
    }

    @PutMapping(ApiPaths.Dev.ROUTE_POLICY_BY_ID)
    public ApiResponse<RouteAccessPolicyResponse> update(
            @Parameter(description = "Internal id of the route-access policy row.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody RouteAccessPolicyUpdateRequest request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.updated"), routeAccessPolicyCommandService.update(id, request)
        );
    }

    @DeleteMapping(ApiPaths.Dev.ROUTE_POLICY_BY_ID)
    public ApiResponse<Void> delete(
            @Parameter(description = "Internal id of the route-access policy row.", required = true)
            @PathVariable @Positive Long id
    ) {
        routeAccessPolicyCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"));
    }
}
