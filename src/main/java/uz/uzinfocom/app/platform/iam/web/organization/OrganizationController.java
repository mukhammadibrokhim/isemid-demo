package uz.uzinfocom.app.platform.iam.web.organization;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.iam.application.organization.query.OrganizationQueryService;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.request.OrganizationFilerRequest;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.request.OrganizationLookupRequest;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.request.OrganizationUserLookupRequest;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.response.OrganizationDetailResponse;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.response.OrganizationLookupResponse;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.response.OrganizationTableResponse;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.response.OrganizationUserLookupResponse;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.response.ApiResponse;
import uz.uzinfocom.app.shared.response.PagedResponse;
import uz.uzinfocom.app.shared.response.PagedResponseAssembler;

import java.util.List;

@Tag(name = "Organizations", description = "API для поиска организаций и просмотра их сотрудников.")
@RestController
@RequestMapping(ApiPaths.Organization.ROOT)
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationQueryService queryService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Получить список организаций",
            description = "Возвращает постраничный список организаций с возможностью фильтрации."
    )
    @GetMapping
    public PagedResponse<OrganizationTableResponse> getAll(
            @ParameterObject @Valid @ModelAttribute OrganizationFilerRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<OrganizationTableResponse> page = queryService.findTable(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Получить организацию по идентификатору",
            description = "Возвращает детальную информацию об организации."
    )
    @GetMapping(ApiPaths.Organization.BY_ID)
    public ApiResponse<OrganizationDetailResponse> get(
            @Parameter(description = "Идентификатор организации.", required = true)
            @PathVariable(ApiPaths.Organization.ID) Long id
    ) {
        OrganizationDetailResponse response = queryService.findDetail(id);
        return ApiResponse.success(messageResolver.resolve("common.success"), response);
    }

    @Operation(
            summary = "Получить организации для выбора",
            description = "Возвращает краткий список организаций, предназначенный для выпадающих списков и полей выбора."
    )
    @GetMapping(ApiPaths.Organization.LOOKUP)
    public ApiResponse<List<OrganizationLookupResponse>> lookup(
            @ParameterObject @Valid @ModelAttribute OrganizationLookupRequest request
    ) {
        List<OrganizationLookupResponse> response = queryService.lookup(request);
        return ApiResponse.success(messageResolver.resolve("common.success"), response);
    }

    @Operation(
            summary = "Получить сотрудников организации",
            description = "Возвращает постраничный список сотрудников указанной организации."
    )
    @GetMapping(ApiPaths.Organization.USERS_BY_ORGANIZATION_ID)
    public PagedResponse<OrganizationUserLookupResponse> getUsersByOrganization(
            @Parameter(description = "Идентификатор организации.", required = true)
            @PathVariable(ApiPaths.Organization.ID) Long organizationId,
            @ParameterObject @Valid @ModelAttribute OrganizationUserLookupRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<OrganizationUserLookupResponse> response =
                queryService.findUserLookupsByOrganizationId(organizationId, request);

        return pagedResponseAssembler.toResponse(response, messageResolver.resolve("common.success"), httpRequest);
    }
}
