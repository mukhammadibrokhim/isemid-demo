package uz.uzinfocom.app.platform.devpanel.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.modules.iam.application.organization.query.OrganizationQueryService;
import uz.uzinfocom.app.modules.iam.application.organization.query.dto.request.OrganizationLookupRequest;
import uz.uzinfocom.app.modules.iam.application.organization.query.dto.response.OrganizationLookupResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.util.List;

/**
 * Dev-panel-only organization lookup - reuses {@link OrganizationQueryService#lookup}
 * (the same data as {@code OrganizationController.lookup}), but reachable from the
 * {@code DevUser} Basic-Auth chain instead of the SSO/DHP bearer session the main
 * {@code /v1/organizations/**} endpoints require. Without this, a dev-panel account
 * has no way to resolve an organization's uuid to a name (or search by name to find
 * the uuid) - e.g. when registering an {@code IntegrationClient}, whose create request
 * takes the organization's business {@code uuid} by hand (see
 * {@code IntegrationClientCreateRequest}). Read-only, open to any authenticated
 * dev-panel account - same as the positions lookup (see {@code DevPositionController}).
 */
@Tag(
        name = "Dev Panel - References",
        description = "Dev-panel-only lookups: positions/departments and organizations."
)
@RestController
@RequestMapping(ApiPaths.Dev.ROOT)
@RequiredArgsConstructor
public class DevOrganizationController {

    private final OrganizationQueryService organizationQueryService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Search organizations for selection",
            description = "Short list of organizations (id, uuid, name, ...) for dropdowns/selection fields "
                    + "- e.g. resolving the uuid typed into IntegrationClientCreateRequest.organizationId to "
                    + "a display name, or searching by name to find the uuid to submit."
    )
    @GetMapping(ApiPaths.Dev.REF_ORGANIZATIONS)
    public ApiResponse<List<OrganizationLookupResponse>> lookup(
            @ParameterObject @Valid @ModelAttribute OrganizationLookupRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), organizationQueryService.lookup(request));
    }
}
