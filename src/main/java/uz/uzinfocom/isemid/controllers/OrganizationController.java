package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.models.entities.Organization;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.annotations.CurrentUser;
import uz.uzinfocom.isemid.models.payload.requests.ReqOrganization;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.services.OrganizationService;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uz.uzinfocom.isemid.utils.ApiUrlConstant.ORGANIZATION;
import static uz.uzinfocom.isemid.utils.AppConstant.PAGE;
import static uz.uzinfocom.isemid.utils.AppConstant.SIZE;
@RestController
@RequestMapping(ORGANIZATION)
@Tag(name = "Organization", description = "API для работы с организациями")
@RequiredArgsConstructor
public class OrganizationController {
    final OrganizationService organizationService;

    @Operation(
            summary = "Получить все организации",
            description = "Получает список всех организаций с возможностью пагинации.")
    @GetMapping("/all")
    public ApiResponsePage getAllOrganizations(
            @RequestParam(defaultValue = PAGE) int page,
            @RequestParam(defaultValue = SIZE) int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String stateCode,
            @RequestParam(required = false) String cityCode,
            HttpServletRequest request
    ) {
        return organizationService.getAllOrganizationsByFilter(page, size, name, stateCode, cityCode, request);
    }

    @Operation(
            summary = "Получить организацию по ID",
            description = "Получает организацию по заданному идентификатору (ID).",
            parameters = {
                    @Parameter(name = "id", description = "ID организации", required = true)})
    @GetMapping("/{id}")
    public ApiResponse<Organization> getOrganizationById(@PathVariable long id) {
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
    public ApiResponse<Organization> updateOrganization(@PathVariable UUID uuid, @RequestBody ReqOrganization reqOrganization, @CurrentUser User currentUser) {
        return organizationService.update(uuid, reqOrganization, currentUser);
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
            @RequestParam String stateCode,
            @RequestParam String cityCode,
            @RequestParam String name
    ) {
        return organizationService.getOrganizationsBySearch(stateCode,cityCode,name);
    }
}
