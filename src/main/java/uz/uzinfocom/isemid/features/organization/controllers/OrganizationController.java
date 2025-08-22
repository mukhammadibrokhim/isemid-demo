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
                        "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6IjhmZTVlZDgwMTVkNjZjMGRhMDE2ODVjMWU3ZjI0MTI5NTM5NTRmMGI4OWM5ZjMyMTYwYTIyYzJhYmVhMjIzN2NkZWM2NjhlM2U3ZTMyNzA3In0.eyJhdWQiOiI5NGY4MzY3MC1mODQxLTRlYzUtYTU5My03MmQyODczZjA1NGIiLCJqdGkiOiI4ZmU1ZWQ4MDE1ZDY2YzBkYTAxNjg1YzFlN2YyNDEyOTUzOTU0ZjBiODljOWYzMjE2MGEyMmMyYWJlYTIyMzdjZGVjNjY4ZTNlN2UzMjcwNyIsImlhdCI6MTc1NTg0MzAzNywibmJmIjoxNzU1ODQzMDM3LCJleHAiOjE3NTU4NTM4MzcsInN1YiI6IjEyMGU3M2YyLWNhOGQtNDM3Ni04NzgyLTQxYmRlYmM1MDJhMSIsInNjb3BlcyI6W10sInVzZXJfaW5mbyI6eyJuYW1lIjoiVkFTSUxFViBWQURJTSBYQU1aQVlFVklDSCIsIm9yZ2FuaXphdGlvbiI6W3sidXVpZCI6IjM3MDA3ODg5LWVhYmMtNDJhOS04M2EyLTEzNmU3YTEwNWNiOCIsIm5hbWUiOiJJVE1FRCBSZXNwdWJsaWthIiwiYWN0aXZlIjp0cnVlLCJwcmFjdGl0aW9uZXJfcm9sZSI6W3sidXVpZCI6ImNjMzM1MGRhLTM1YjQtNDFlOS05NTBmLWVmZDg3YzVkYTU2NyIsImNvZGUiOiI3MTQifV19XSwicHJhY3RpdGlvbmVyIjoiMzEwNGIyNmQtOTdhMy00MWIyLWI2ZDQtNmNmMzYxZWVkYzFhIiwicm9sZXMiOlsic3VwZXJfYWRtaW4iLCJjYXJkaW9fYWRtaW4iLCJjYXJkaW9fYWRtaW5fcmVnaW9uIiwiY2FyZGlvX21vbml0b3JpbmdfbWluaXN0cnkiLCJjYXJkaW9fbW9uaXRvcmluZ19jYXJkaW9sb2d5X2NlbnRlciIsImNhcmRpb19oZWFkX291dHBhdGllbnRfcHJhY3RpdGlvbmVyIiwiY2FyZGlvX3JlZ2lzdGFyIiwiY2FyZGlvX2NhcmRpb2xvZ2lzdCIsImNhcmRpb19wcmVfbWVkaWNhbF9yb29tIiwiY2FyZGlvX2xhYm9yYXRvcnlfYXNzaXN0YW50IiwiY2FyZGlvX2Z1bmN0aW9uYWxfZGlhZ25vc3RpY3MiLCJjYXJkaW9fZ2VuZXJhbF9wcmFjdGl0aW9uZXIiLCJsaXNfcmVnaXN0cmFyIiwibGlzX251cnNlIiwibGlzX2xhYnJhbnQiLCJsaXNfZGlyZWN0b3IiLCJsaXNfZXh0ZXJuYWxfcmVnaXN0cmFyIiwiZ2VwYXRpdF92b3AiLCJnZXBhdGl0X2luZmVjdGlvdXNfZGlzZWFzZSIsImdlcGF0aXRfdWx0cmFzb3VuZCIsImdlcGF0aXRfbGFicmFudCIsImdlcGF0aXRfbW9uaXRvcmluZyIsImdlcGF0aXRfZmFybWF6ZXZ0IiwiYmFkX2FkbWluIiwiYmFkX3VzZXIiLCJ2YWMyX2FkbWluIiwidmFjMl91c2VyIiwiaXNlbWlkX3N1cGVyX2FkbWluIiwiZ3Bfd2FyZWhvdXNlX21hbmFnZXIiLCJncF9oZWFkX2RvY3RvciIsImdwX2F1ZGl0b3IiXX0sImNsaWVudF9pZCI6IjEyMGU3M2YyLWNhOGQtNDM3Ni04NzgyLTQxYmRlYmM1MDJhMSJ9.w2-v6jucm8rMelJoXJPxpdhmEQ3yq5diF5kx4NBy3r1ejpm7eowU7kWg4jBq68o6Y4h5P7tPm692T1upe2niMtVdc9dJiQHCwCyamEGbqy27LiuKjAzEf81kDQQ-Z1sqNmP2-LFCNdP71gBojREdfbewVSWKLTUVYiQNaBJ-2Am7-ursCWwxH4P8jPQzx9E6X6Z_VMJeYBTM-t--YfGn6DU6gMSklvc_eZXuyo5aA8-zipLJ72BvnDwX9eLQBkgnDy9caiKOdwpQK3rmi63bRl2AydMydMNaXERZJbSQjj80KcBm71BaQPrE4oRUqEr0Zg-KQT58z2Z3QtJZ6SK6QJYFgUY5uIhrchkddWvsz9FvFXoJ-YjOn6bjaR9QG7y8Dc7Nyz1DOy7nv_8cFgQpg8hDW6N3p4OgnUujRyreNmjDe_73-qnRfaj9i0uHFhWAfPDCezRpxmveT9rcrwD5cPisdKamY0vjzS1ui8-UtbCHhSvQh_UnWhrKq7192ltncuJhL1haWter4IiiOyltlEVW2iBaIZgp3KQkVyYgxYZeK29pS2MSsHkDp1LBO_MgiEce-fbe_bjm1jc65nFhSFTSShVzo2xUh2AFGhnKInLzm3-K594xDmQ47ui1h16cxFktR9bmrQWkPz6R5wtsiNuXCFed1XV8tABMfbvfISc";
                MisOrganizationResponse body = makeRequest(
                        String.format(ApiPaths.ExternalApi.ORGANIZATION, uuid),
                        HttpMethod.GET,
                        tokenValue,
                        null,
                        MisOrganizationResponse.class
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
