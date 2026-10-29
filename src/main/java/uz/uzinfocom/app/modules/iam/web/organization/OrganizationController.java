package uz.uzinfocom.app.modules.iam.web.organization;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;
import uz.uzinfocom.app.modules.iam.application.organization.query.OrganizationQueryService;
import uz.uzinfocom.app.modules.iam.application.organization.query.dto.request.OrganizationFilerRequest;
import uz.uzinfocom.app.modules.iam.application.organization.query.dto.request.OrganizationLookupRequest;
import uz.uzinfocom.app.modules.iam.application.organization.query.dto.request.OrganizationUserLookupRequest;
import uz.uzinfocom.app.modules.iam.application.organization.query.dto.response.OrganizationDetailResponse;
import uz.uzinfocom.app.modules.iam.application.organization.query.dto.response.OrganizationLookupResponse;
import uz.uzinfocom.app.modules.iam.application.organization.query.dto.response.OrganizationShortResponse;
import uz.uzinfocom.app.modules.iam.application.organization.query.dto.response.OrganizationTableResponse;
import uz.uzinfocom.app.modules.iam.application.organization.query.dto.response.OrganizationUserLookupResponse;
import uz.uzinfocom.app.modules.iam.application.sync.OrganizationSyncService;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

import java.util.List;
import java.util.UUID;

@Tag(name = "Organizations", description = "API для поиска организаций и просмотра их сотрудников.")
@RestController
@RequestMapping(ApiPaths.Organization.ROOT)
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationQueryService queryService;
    private final OrganizationSyncService syncService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;
    private final CurrentUserProvider currentUserProvider;

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
            summary = "Получить иерархию организации",
            description = "Возвращает цепочку организаций от корневой до указанной (для вкладки «Иерархия»)."
    )
    @GetMapping(ApiPaths.Organization.HIERARCHY_BY_ORGANIZATION_ID)
    public ApiResponse<List<OrganizationShortResponse>> getHierarchy(
            @Parameter(description = "Идентификатор организации.", required = true)
            @PathVariable(ApiPaths.Organization.ID) Long id
    ) {
        List<OrganizationShortResponse> response = queryService.findHierarchy(id);
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

    @Operation(
            summary = "Синхронизировать организацию по uuid",
            description = "Запрашивает актуальные данные организации у внешнего IAM-провайдера (api2.ssv.uz / "
                    + "fhir.dhp.uz) по её uuid и обновляет запись; если организация ещё не существует локально, "
                    + "создаёт её."
    )
    @PostMapping(ApiPaths.Organization.SYNC_BY_UUID)
    @PreAuthorize("@adminAccessGuard.isSuperAdmin()")
    public ApiResponse<OrganizationDetailResponse> sync(
            @Parameter(description = "UUID организации во внешней IAM-системе.", required = true)
            @PathVariable(ApiPaths.Organization.UUID) UUID uuid,
            @Parameter(description = "Ключ провайдера (sso/dhp). Необязателен, если организация уже известна.")
            @RequestParam(required = false) String providerKey
    ) {
        String rawToken = currentUserProvider.rawTokenOrNull();
        if (rawToken == null) {
            throw new IllegalStateException("No bearer token on the current request; cannot sync organization");
        }

        String callerProviderKey = currentUserProvider.currentProviderKeyOrNull();
        Organization organization = syncService.syncByUuid(uuid, providerKey, callerProviderKey, rawToken);
        OrganizationDetailResponse response = queryService.findDetail(organization.getId());
        return ApiResponse.success(messageResolver.resolve("common.success"), response);
    }
}
