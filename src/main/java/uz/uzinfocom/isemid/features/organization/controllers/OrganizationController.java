package uz.uzinfocom.isemid.features.organization.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.organization.dtos.requests.OrgRequest;
import uz.uzinfocom.isemid.features.organization.dtos.responses.MisOrganizationResponse;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrganizationResponse;
import uz.uzinfocom.isemid.features.organization.mappers.OrganizationMapper;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;
import uz.uzinfocom.isemid.features.organization.reposiotries.OrganizationRepository;
import uz.uzinfocom.isemid.features.organization.services.OrganizationService;
import uz.uzinfocom.isemid.features.user.annotation.CurrentUser;
import uz.uzinfocom.isemid.features.user.mappers.UserMapper;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_PAGE;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_SIZE;
import static uz.uzinfocom.isemid.commons.utils.HttpUtil.getRequestUrl;
import static uz.uzinfocom.isemid.commons.utils.RestClientUtil.makeRequest;

@Log4j2
@RestController
@RequestMapping(ApiPaths.Organization.ROOT)
@Tag(name = "Organization", description = "API для работы с организациями")
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationService organizationService;
    private final OrganizationRepository organizationRepository;
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

    @PostMapping("/init")
    public ApiResponse<?> initOrganization() {
        log.info("MIS sync started via @PostConstruct");

        List<Organization> organizations = organizationRepository.findAll();

        for (Organization org : organizations) {
            UUID uuid = org.getUuid();
            try {
                String tokenValue =
                        "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6ImRlNzZlNDI3ZDdhMjc3ZmJhN2Q5MGE4MDBkMmYwNzVhZGQ1MWE1YmRjNjJjMjZkNTBmY2U1NzA2Mzk2Y2FkYjVmYTI0MzI1M2VjYjNiN2NiIn0.eyJhdWQiOiI5NGY4MzY3MC1mODQxLTRlYzUtYTU5My03MmQyODczZjA1NGIiLCJqdGkiOiJkZTc2ZTQyN2Q3YTI3N2ZiYTdkOTBhODAwZDJmMDc1YWRkNTFhNWJkYzYyYzI2ZDUwZmNlNTcwNjM5NmNhZGI1ZmEyNDMyNTNlY2IzYjdjYiIsImlhdCI6MTc1NTg0NzE0NywibmJmIjoxNzU1ODQ3MTQ3LCJleHAiOjE3NTU4NTc5NDcsInN1YiI6IjEyMGU3M2YyLWNhOGQtNDM3Ni04NzgyLTQxYmRlYmM1MDJhMSIsInNjb3BlcyI6W10sInVzZXJfaW5mbyI6eyJuYW1lIjoiVkFTSUxFViBWQURJTSBYQU1aQVlFVklDSCIsIm9yZ2FuaXphdGlvbiI6W3sidXVpZCI6IjM3MDA3ODg5LWVhYmMtNDJhOS04M2EyLTEzNmU3YTEwNWNiOCIsIm5hbWUiOiJJVE1FRCBSZXNwdWJsaWthIiwiYWN0aXZlIjp0cnVlLCJwcmFjdGl0aW9uZXJfcm9sZSI6W3sidXVpZCI6ImNjMzM1MGRhLTM1YjQtNDFlOS05NTBmLWVmZDg3YzVkYTU2NyIsImNvZGUiOiI3MTQifV19XSwicHJhY3RpdGlvbmVyIjoiMzEwNGIyNmQtOTdhMy00MWIyLWI2ZDQtNmNmMzYxZWVkYzFhIiwicm9sZXMiOlsiY2FyZGlvX2FkbWluIiwiY2FyZGlvX2FkbWluX3JlZ2lvbiIsImNhcmRpb19tb25pdG9yaW5nX21pbmlzdHJ5IiwiY2FyZGlvX21vbml0b3JpbmdfY2FyZGlvbG9neV9jZW50ZXIiLCJjYXJkaW9faGVhZF9vdXRwYXRpZW50X3ByYWN0aXRpb25lciIsImNhcmRpb19yZWdpc3RhciIsImNhcmRpb19jYXJkaW9sb2dpc3QiLCJjYXJkaW9fcHJlX21lZGljYWxfcm9vbSIsImNhcmRpb19sYWJvcmF0b3J5X2Fzc2lzdGFudCIsImNhcmRpb19mdW5jdGlvbmFsX2RpYWdub3N0aWNzIiwiY2FyZGlvX2dlbmVyYWxfcHJhY3RpdGlvbmVyIiwibGlzX3JlZ2lzdHJhciIsImxpc19udXJzZSIsImxpc19sYWJyYW50IiwibGlzX2RpcmVjdG9yIiwibGlzX2V4dGVybmFsX3JlZ2lzdHJhciIsImdlcGF0aXRfdm9wIiwiZ2VwYXRpdF9pbmZlY3Rpb3VzX2Rpc2Vhc2UiLCJnZXBhdGl0X3VsdHJhc291bmQiLCJnZXBhdGl0X2xhYnJhbnQiLCJnZXBhdGl0X21vbml0b3JpbmciLCJnZXBhdGl0X2Zhcm1hemV2dCIsImJhZF9hZG1pbiIsImJhZF91c2VyIiwidmFjMl9hZG1pbiIsInZhYzJfdXNlciIsImlzZW1pZF9zdXBlcl9hZG1pbiIsImdwX2hlYWRfZG9jdG9yIl19LCJjbGllbnRfaWQiOiIxMjBlNzNmMi1jYThkLTQzNzYtODc4Mi00MWJkZWJjNTAyYTEifQ.EK4eFW7ftsyJrx4lTP53_uqrcZRtgP6aJFI4AK2HCzKnO4AJhCNW6xHeAFCyYvyAu2Vl3dKZ57V_z1Nm5dAY7gixkRRjiBgnlMxURGz5KaH988wq1B6Pcig33kUpuu6Kc8m_i6U8QdWpRLB-wX35ecDERnuehgMGdB8vdP-AAXdTHioFfouhfZHSptvhGGtSVefMpyVGoLJef3Qbu6Ofne-jiPFOj6fmiA0-9pivNLHL1eZtgBZUFmfhxwl5oEBFht4LqwQDFrKvl9xuzDmpJ-fc7Di7XsDWc67z06xEJ05LI3O3jSvgcL6cuEQaIvR_afd6hZAWWbylA1FNdLZPPTHdse11WSqc_ZK9Su0Qc7tIZvYEzVkKFUnpDZoMv_px06ABVTgkSPM8R2N1XI7szM_1TRiUzOkBUt4szhEnbAnShCaxJDeBXcL8OXPnJZ-aXEc2YrEl6beZU-jSDc3w-GjCF3u7jkGGErXo2bON3lsSGDZp8fp5kZRt4rVgpM_u1qATCYQtkm1ewyt8CGn8Na3uN8nAIGOgiyuQD4jSIH8EPUyLy2Gm4OOIpu6VnC8wAXJiSzs0BHtphtPq_sOu0lNtkpyECxBTXHRUfHshK2G22MwhJfV7ZpkNc55-oHJCJKqhsz9ZEVD8HHEDDa1dHZJxq9x7PvAcNIHoELJgfHo";
                MisOrganizationResponse body = makeRequest(
                        String.format(ApiPaths.ExternalApi.ORGANIZATION, uuid),
                        HttpMethod.GET,
                        tokenValue,
                        null,
                        MisOrganizationResponse.class,
                        messageUtil
                );

                if (body != null) {
                    Organization updated = organizationMapper.toOrganization(body);
                    updated.setId(org.getId());
                    updated.setUuid(org.getUuid());
                    organizationRepository.save(updated);
                    log.info("✅ Updated organization: {}", org.getName());
                } else {
                    log.warn("⚠️ No body for organization UUID={}", uuid);
                }

            } catch (Exception ex) {
                log.error("❌ Failed to fetch organization from MIS for UUID={}", uuid, ex);
            }
        }

        log.info("MIS sync finished");
        return ApiResponse.success(messageUtil, null);
    }
}
