package uz.uzinfocom.isemid.features.organization.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.ApiResponsePage;
import uz.uzinfocom.isemid.features.organization.dtos.requests.OrgRequest;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrgResponse;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.services.OrganizationService;
import uz.uzinfocom.isemid.features.user.annotation.CurrentUser;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_PAGE;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_SIZE;
import static uz.uzinfocom.isemid.commons.utils.HttpUtil.getRequestUrl;

@RestController
@RequestMapping(ApiPaths.Organization.ROOT)
@Tag(name = "Organization", description = "API для работы с организациями")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @Operation(
            summary = "Получить все организации",
            description = "Получает список всех организаций с возможностью пагинации.")
    @GetMapping("/all")
    public ApiResponsePage getAllOrganizations(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String stateCode,
            @RequestParam(required = false) String cityCode,
            HttpServletRequest request
    ) {
        return organizationService.getAllOrgByFilter(page, size, name, stateCode, cityCode, getRequestUrl(request));
    }

    @Operation(
            summary = "Получить организацию по ID",
            description = "Получает организацию по заданному идентификатору (ID).",
            parameters = {
                    @Parameter(name = "id", description = "ID организации", required = true)})
    @GetMapping("/{id}")
    public ApiResponse<OrgResponse> getOrganizationById(@PathVariable long id) {
        return organizationService.getById(id);
    }

    @GetMapping("/department/{uuid}")
    public ApiResponse<List<Organization>> getOrganizationChildren(@PathVariable UUID uuid) {
        return organizationService.getChildrenByParentUuid(uuid);
    }

    @Operation(
            summary = "Обновить организацию из MIS",
            description = "Обновляет информацию об организации, используя данные из MIS по UUID организации.",
            parameters = {
                    @Parameter(name = "uuid", description = "UUID организации для обновления", required = true),
                    @Parameter(name = "Authorization", description = "JWT токен авторизации", required = true)
            })
    @PutMapping("/{uuid}/update-from-mis")
    public ApiResponse<Organization> updateOrganization(
            @PathVariable UUID uuid,
            @CurrentUser User currentUser,
            @Parameter(hidden = true) @RequestHeader(AUTHORIZATION) String token
    ) {
        return organizationService.updateUserFromMis(uuid, currentUser, token);
    }

    @PutMapping("/{uuid}/update")
    public ApiResponse<?> updateOrganization(@PathVariable UUID uuid, @RequestBody OrgRequest orgRequest) {
        return organizationService.update(uuid, orgRequest);
    }

    @Operation(
            summary = "Получить всех пользователей организации",
            description = "Этот API извлекает всех пользователей, связанных с конкретной организацией по её ID.")
    @GetMapping("/{id}/users")
    public ApiResponse<?> getOrganization(@PathVariable long id) {
        return organizationService.getAllOrgUsers(id);
    }

    @GetMapping("/search")
    public ApiResponse<?> getOrganizationsByFilter(
            @RequestParam(required = false) String stateCode,
            @RequestParam(required = false) String cityCode,
            @RequestParam(required = false) String name
    ) {
        return organizationService.getOrganizationsBySearch(stateCode, cityCode, name);
    }
}
