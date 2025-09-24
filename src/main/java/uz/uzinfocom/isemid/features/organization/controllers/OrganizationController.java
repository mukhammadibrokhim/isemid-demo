package uz.uzinfocom.isemid.features.organization.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.organization.dtos.requests.OrgRequest;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrganizationResponse;
import uz.uzinfocom.isemid.features.organization.mappers.OrganizationMapper;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;
import uz.uzinfocom.isemid.features.organization.services.OrganizationService;
import uz.uzinfocom.isemid.features.user.models.annotation.CurrentUser;
import uz.uzinfocom.isemid.features.user.mappers.UserMapper;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_PAGE;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_SIZE;
import static uz.uzinfocom.isemid.commons.utils.HttpUtil.getRequestUrl;

@Log4j2
@RestController
@RequestMapping(ApiPaths.Organization.ROOT)
@Tag(name = "Organization", description = "API для работы с организациями")
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationService organizationService;
    private final OrganizationMapper organizationMapper;
    private final MessageUtil messageUtil;
    private final UserMapper userMapper;

    @Operation(summary = "Получить все организации", description = "Получает список всех организаций с возможностью пагинации и фильтрации по имени, коду региона и города.")
    @GetMapping("/all")
    public PagedResponse<?> getAllOrganizations(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String stateCode,
            @RequestParam(required = false) String cityCode,
            @CurrentUser User user,
            HttpServletRequest request
    ) {
        Page<Organization> organizationPage = organizationService.filter(page, size, name, stateCode, cityCode, user);
        return PagedResponse.build(page, organizationPage.map(organizationMapper::toResponse), getRequestUrl(request), messageUtil);
    }

    @Operation(summary = "Получить организацию по ID", description = "Получает организацию по заданному идентификатору (ID).")
    @GetMapping("/{id}")
    public ApiResponse<OrganizationResponse> getOrganizationById(@PathVariable long id) {
        return ApiResponse.success(messageUtil, organizationMapper.toResponse(organizationService.getById(id)));
    }

    @Operation(summary = "Получить дочерние организации по UUID родительской организации", description = "Возвращает список дочерних организаций для организации с указанным UUID.")
    @GetMapping("/department/{uuid}")
    public ApiResponse<List<Organization>> getOrganizationChildren(@PathVariable UUID uuid) {
        return ApiResponse.success(messageUtil, organizationService.getChildrenByParentUuid(uuid));
    }

    @Operation(summary = "Обновить организацию из MIS", description = "Обновляет информацию об организации, используя данные из MIS по UUID организации. Требуется авторизация.")
    @PutMapping("/{uuid}/update-from-mis")
    public ApiResponse<OrganizationResponse> updateOrganizationFromMis(
            @PathVariable UUID uuid,
            @CurrentUser User currentUser,
            @Parameter(hidden = true) @RequestHeader(AUTHORIZATION) String token
    ) {
        return ApiResponse.success(messageUtil, organizationMapper.toResponse(organizationService.updateUserFromMis(uuid, currentUser, token)));
    }
    @Operation(summary = "Обновить организацию", description = "Обновляет данные организации по UUID с новыми данными из тела запроса.")
    @PutMapping("/{uuid}/update")
    public ApiResponse<?> updateOrganization(
            @PathVariable UUID uuid,
            @RequestBody OrgRequest request
    ) {
        return ApiResponse.success(messageUtil, organizationMapper.toResponse(organizationService.update(uuid, request)));
    }

    @Operation(summary = "Получить всех пользователей организации", description = "Извлекает всех пользователей, связанных с конкретной организацией по её ID.")
    @GetMapping("/{id}/users")
    public ApiResponse<?> getOrganizationUsers(@PathVariable long id) {
        return ApiResponse.success(messageUtil, organizationService.getAllOrgUsers(id).stream().map(userMapper::toMiniResponse).toList());
    }

    @Operation(summary = "Поиск организаций по фильтрам", description = "Поиск организаций с возможностью фильтрации по коду региона, коду города и имени организации.")
    @GetMapping("/search")
    public ApiResponse<?> getOrganizationsByFilter(
            @RequestParam(required = false) String stateCode,
            @RequestParam(required = false) String cityCode,
            @RequestParam(required = false) MedicalType medicalType,
            @RequestParam(required = false) String name
    ) {
        return ApiResponse.success(messageUtil, organizationService.getOrganizationsBySearch(stateCode, cityCode, medicalType, name)
                .stream().map(organizationMapper::toOrgMiniResponse).collect(Collectors.toSet()));
    }
}
