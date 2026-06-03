package uz.uzinfocom.app.platform.iam.web.organization;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.iam.application.organization.query.OrganizationQueryService;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.OrganizationDetailResponse;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.OrganizationFilerRequest;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.OrganizationLookupRequest;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.OrganizationLookupResponse;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.OrganizationTableResponse;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.OrganizationUserLookupRequest;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.OrganizationUserLookupResponse;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.response.ApiResponse;
import uz.uzinfocom.app.shared.response.ErrorResponse;
import uz.uzinfocom.app.shared.response.PagedResponse;

import java.util.List;

@Tag(name = "Organizations", description = "Organization lookup and membership APIs.")
@RestController
@RequestMapping(ApiPaths.Organization.BASE)
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationQueryService queryService;
    private final MessageResolver messageResolver;

    @Operation(summary = "Find organizations")
    @GetMapping
    public PagedResponse<OrganizationTableResponse> getAll(
            @ParameterObject @Valid @ModelAttribute OrganizationFilerRequest request
    ) {
        Page<OrganizationTableResponse> page = queryService.findTable(request);
        return PagedResponse.fromPage(page, messageResolver.resolve("common.success"));
    }

    @Operation(summary = "Get organization by id")
    @GetMapping(ApiPaths.Organization.BY_ID)
    public ApiResponse<OrganizationDetailResponse> get(
            @Parameter(description = "Organization id.", required = true)
            @PathVariable(ApiPaths.Organization.ID) Long id
    ) {
        OrganizationDetailResponse response = queryService.findDetail(id);
        return ApiResponse.success(messageResolver.resolve("common.success"), response);
    }

    @Operation(summary = "Find organizations for lookup")
    @GetMapping(ApiPaths.Organization.LOOKUP)
    public ApiResponse<List<OrganizationLookupResponse>> lookup(
            @ParameterObject @Valid @ModelAttribute OrganizationLookupRequest request
    ) {
        List<OrganizationLookupResponse> response = queryService.lookup(request);
        return ApiResponse.success(messageResolver.resolve("common.success"), response);
    }

    @Operation(summary = "Find organization users")
    @GetMapping(ApiPaths.Organization.USERS_BY_ORGANIZATION_ID)
    public PagedResponse<OrganizationUserLookupResponse> getUsersByOrganization(
            @Parameter(description = "Organization id.", required = true)
            @PathVariable(ApiPaths.Organization.ID) Long organizationId,
            @ParameterObject @Valid @ModelAttribute OrganizationUserLookupRequest request
    ) {
        Page<OrganizationUserLookupResponse> response =
                queryService.findUserLookupsByOrganizationId(organizationId, request);

        return PagedResponse.fromPage(response, messageResolver.resolve("common.success"));
    }
}
