package uz.uzinfocom.isemid.features.organization.services.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.commons.utils.PagedResponseUtil;
import uz.uzinfocom.isemid.features.organization.dtos.requests.OrgRequest;
import uz.uzinfocom.isemid.features.organization.dtos.responses.MisOrganizationResponse;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrganizationResponse;
import uz.uzinfocom.isemid.features.organization.mappers.OrganizationMapper;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;
import uz.uzinfocom.isemid.features.organization.reposiotries.OrganizationRepository;
import uz.uzinfocom.isemid.features.organization.services.OrganizationService;
import uz.uzinfocom.isemid.features.organization.specifications.OrganizationSpecification;
import uz.uzinfocom.isemid.features.user.dtos.responses.UserMiniResponse;
import uz.uzinfocom.isemid.features.user.mappers.UserMapper;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableSortByIdDesc;
import static uz.uzinfocom.isemid.commons.utils.RestClientUtil.makeRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final MessageUtil messageUtil;
    private final OrganizationMapper mapper;
    private final UserMapper userMapper;

    @Override
    public PagedResponse<?> getAllOrgByFilter(int page, int size, String name, String stateCode, String cityCode, String url, User user) {
        Specification<Organization> spec = OrganizationSpecification.build(name, stateCode, cityCode, user);
        Page<OrganizationResponse> organizationPage = organizationRepository.findAll(spec, pageableSortByIdDesc(page, size))
                .map(mapper::toResponse);
        return PagedResponseUtil.build(page, organizationPage, url, messageUtil);
    }

    @Override
    public Organization fetchOrganizationFromMisAndSave(UUID uuid, String tokenValue) {
        String json = makeRequest(String.format(ApiPaths.ExternalApi.ORGANIZATION, uuid), HttpMethod.GET, tokenValue, null, String.class);
        MisOrganizationResponse response;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
            response = mapper.readValue(json, MisOrganizationResponse.class);
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "JSON parsing failed", e);
        }
        return organizationRepository.saveAndFlush(mapper.toOrganization(response));
    }

    @Override
    public ApiResponse<OrganizationResponse> getById(long id) {
        Organization organization = organizationRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
        return ApiResponse.success(messageUtil.get("message.success"), mapper.toResponse(organization));
    }

    @Override
    public ApiResponse<OrganizationResponse> updateUserFromMis(UUID uuid, User currentUser, String token) {
        MisOrganizationResponse response = makeRequest(String.format(ApiPaths.ExternalApi.ORGANIZATION, uuid), HttpMethod.GET, token, null, MisOrganizationResponse.class);
        Organization organization = mapper.toOrganization(response);
        return ApiResponse.success(messageUtil.get("message.success"), mapper.toResponse(organizationRepository.save(organization)));

    }

    @Override
    public Optional<Organization> findById(UUID uuid) {
        return organizationRepository.findByUuid(uuid);
    }

    @Override
    public ApiResponse<?> update(UUID uuid, OrgRequest orgRequest) {
        Organization existingOrg = organizationRepository.findByUuid(uuid).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", uuid)));
        mapper.updateFromRequest(orgRequest, existingOrg);

        if (orgRequest.getParentUuid() != null) {
            Organization parentOrg = organizationRepository.findByUuid(orgRequest.getParentUuid())
                    .orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", orgRequest.getParentUuid())));

            parentOrg.getChildren().add(existingOrg);
            existingOrg.setParent(parentOrg);
            existingOrg.setParentId(parentOrg.getId());
        } else {
            existingOrg.setParent(null);
            existingOrg.setParentId(null);
        }

        return ApiResponse.success(messageUtil.get("message.success"), mapper.toResponse(organizationRepository.save(existingOrg)));
    }

    @Override
    public ApiResponse<List<Organization>> getChildrenByParentUuid(UUID uuid) {
        Organization organization = organizationRepository.findByUuid(uuid).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", uuid)));
        return ApiResponse.success(messageUtil.get("message.success"), organization.getChildren());
    }

    @Override
    public ApiResponse<?> getAllOrgUsers(long id) {
        Organization organization = organizationRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
        List<UserMiniResponse> userResponses = organization.getUsers()
                .stream().map(userMapper::toMiniResponse).toList();
        return ApiResponse.success(messageUtil.get("message.success"), userResponses);
    }

    @Override
    public Optional<Organization> findById(long id) {
        return organizationRepository.findById(id);
    }

    @Override
    public Optional<Organization> findByTin(String tin) {
        return organizationRepository.findByTin(tin);
    }

    @Cacheable(value = "organizationSearchCache", key = "#stateCode + '-' + #cityCode + '-' + (#medicalType != null ? #medicalType.name() : 'null') + '-' + #name")
    @Override
    public ApiResponse<?> getOrganizationsBySearch(String stateCode, String cityCode, MedicalType medicalType, String name) {
        return ApiResponse.success(messageUtil.get("message.success"), organizationRepository.searchByFilter(stateCode, cityCode, medicalType != null ? medicalType.name() : null, name)
                .stream()
                .map(mapper::toResponse).toList());
    }

    @Override
    public Organization save(Organization newOrg) {
        return organizationRepository.save(newOrg);
    }

    @Override
    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

//    @PostConstruct
//    @Transactional
//    public void syncOrganizationsFromMISOnStartup() {
//        log.info("MIS sync started via @PostConstruct");
//
//        List<Organization> organizations = organizationRepository.findAll();
//
//        for (Organization org : organizations) {
//            UUID uuid = org.getUuid();
//            try {
//                String tokenValue =
//                        "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6IjRkOTQ0NjUyNTI3OTU3YTY2NmQ5ODg4MjFmM2FjYzQyZWNhOTY5MzU0NDQ4ZWZkZjc2YjE3OWQ3MjIzMDQ4MWViYTFhMjg2ODA0MTBhNDI3In0.eyJhdWQiOiI5NGY4MzY3MC1mODQxLTRlYzUtYTU5My03MmQyODczZjA1NGIiLCJqdGkiOiI0ZDk0NDY1MjUyNzk1N2E2NjZkOTg4ODIxZjNhY2M0MmVjYTk2OTM1NDQ0OGVmZGY3NmIxNzlkNzIyMzA0ODFlYmExYTI4NjgwNDEwYTQyNyIsImlhdCI6MTc1NDMwODgyNiwibmJmIjoxNzU0MzA4ODI2LCJleHAiOjE3NTQzMTk2MjYsInN1YiI6Ijc2YTZmMjY3LWViMzgtNGYwZi04Y2MwLTRlMTg0ZGI0YzAxZSIsInNjb3BlcyI6W10sInVzZXJfaW5mbyI6eyJuYW1lIjoiVVNNT05PViBGQVJLSE9EIEFCRFVHQU5JWUVWSUNIIiwib3JnYW5pemF0aW9uIjpbeyJ1dWlkIjoiMzBhN2Y1NDktNGYwYS00ZjM0LWJjNzYtMDAzMTAwZDA2YzcwIiwibmFtZSI6IlNhcmRvYmEgdHVtYW4ga28ncCB0YXJtb3FsaSBtYXJrYXppeSBwb2xpa2xpbmlrYSIsImFjdGl2ZSI6dHJ1ZSwicHJhY3RpdGlvbmVyX3JvbGUiOlt7InV1aWQiOiI4MmI3OTA4NC0wMGU1LTQ2OWYtYWVjNC1mNTU0ODkzNWJlOWYiLCJjb2RlIjoiMzM5MyJ9XX0seyJ1dWlkIjoiNWE0NDFmZWItN2I0ZC00ZDVlLTk5N2ItZDc0Yzg3MTRiZTQ0IiwibmFtZSI6IlNhcmRvYmEgdHVtYW4gdGliYml5b3QgYmlybGFzaG1hc2kiLCJhY3RpdmUiOnRydWUsInByYWN0aXRpb25lcl9yb2xlIjpbeyJ1dWlkIjoiZjVhMTI3MGEtNjNhZC00MjkxLTk0YjktOGQ5NmJkOTgxYjc0IiwiY29kZSI6IjM1MTcifV19LHsidXVpZCI6Ijk2N2E2N2IzLWQwMjQtNDI1Ni1hNzg5LWZiZjE2ODBjMjYwNCIsIm5hbWUiOiJNYXJnJ2xpb24gc2hhaGFyIGtvJ3AgdGFybW9xbGkgbWFya2F6aXkgcG9saWtsaW5pa2FzaSIsImFjdGl2ZSI6dHJ1ZSwicHJhY3RpdGlvbmVyX3JvbGUiOlt7InV1aWQiOiI1NWI5ZjY2My0xMzgyLTRhMzgtYThkMC0zZGY4OGEzY2JlYzYiLCJjb2RlIjoiMiJ9XX0seyJ1dWlkIjoiNGMyOTk4MGUtYzBhNi00ODZiLWI2OGEtOGE5ZWRjYWM3NGEyIiwibmFtZSI6Ik5hcnBheSB0dW1hbiB0aWJiaXlvdCBiaXJsYXNobWFzaSIsImFjdGl2ZSI6dHJ1ZSwicHJhY3RpdGlvbmVyX3JvbGUiOlt7InV1aWQiOiJmYTI1M2Y3Yi1iZTdhLTRlMzYtOWI1Ny1lYjNiMzFhNmE5NjMiLCJjb2RlIjoiNSJ9XX1dLCJwcmFjdGl0aW9uZXIiOiJmODg3ZDBkYi05ZjM4LTRiMmEtYjg5NS03ZmQ4MTQ5OThiNjMiLCJyb2xlcyI6WyJzdXBlcl9hZG1pbiIsImNvbW1pc3Npb25fMV9yb2xlIiwiaGVhZF9pbnBhdGllbnQiLCJvcmRlcl9jYW5jZWxlZCIsInNwZWNpYWxfb3JkZXJfcHJhY3RpdGlvbmVyIiwiZm9ybV8wODMiLCJzaWNrX2xlYXZlX2NvbmZpcm1lciIsImludmVudG9yeV9zZXJ2aWNlX2hlYWQiLCJpbnZlbnRvcnlfaGVhZF9kb2N0b3IiLCJpbnZlbnRvcnlfbWVkaWNhbF9hY2NvdW50YW50IiwiaW52ZW50b3J5X2Rpc3BlbnNlciIsImludmVudG9yeV9jb250cmFjdG9yIiwiaW52ZW50b3J5X21vbml0b3JpbmciLCJpbnZlbnRvcnlfc2VuaW9yX251cnNlIiwiaW52ZW50b3J5X2FkbWluIiwiaW52ZW50b3J5X3JlZ19hZG1pbiIsImludmVudG9yeV9yZXBfbW9uaXRvcmluZyIsImludmVudG9yeV9yZWdfbW9uaXRvcmluZyIsImxpc19yZWdpc3RyYXIiLCJsaXNfbnVyc2UiLCJsaXNfbGFicmFudCIsImxpc19kaXJlY3RvciIsImdlcGF0aXRfdm9wIiwiZ2VwYXRpdF9pbmZlY3Rpb3VzX2Rpc2Vhc2UiLCJnZXBhdGl0X3VsdHJhc291bmQiLCJnZXBhdGl0X2xhYnJhbnQiLCJsaXNfZXh0ZXJuYWxfcmVnaXN0cmFyIiwiYmFkX2FkbWluIiwiYmFkX3VzZXIiLCJnZXBhdGl0X21vbml0b3JpbmciLCJnZXBhdGl0X2Zhcm1hemV2dCIsImxpc19wYXRpZW50IiwibGlzX3BhdGllbnRfbGVnYWwiLCJsaXNfYWRtaW5pc3RyYXRvciIsImxpc19hZmZpcm1hdG9yeSIsImxpc19sb2NhbF9tb25pdG9yaW5nIiwibGlzX2Nhc2hpZXIiLCJsaXNfd2FyZWhvdXNlX2VtcGxveWVlIiwibGlzX3dhcmVob3VzZV9zdXBlcnZpc29yIiwibGlzX3NlbGVjdGlvbl9hY3QiLCJyaXNrX2Fzc2Vzc21lbnRfaW5zcGVjdG9yIiwiaXNlbWlkX2FkbWluIiwidmFjMl9hZG1pbiJdLCJwaG9uZV92ZXJpZmllZCI6bnVsbH19.sOKCDfFSyhwObslrzTfhQuIHByxdL7TNMWOBhIrL_W9p41hq_8bIQm9lmObn3ud9VKz2O1iidqihuT08XFnaTCSxbfN6_mZen1h86rCPo49u6QfdboX04ujD9ZUInMywzDWdTH4BUG9pFbdtfjW_tA4mZT8HlHiT0lioKY18AYZNRTab0EBQkza4Ji5Ir_yndoXertm3jcmK3Pxfq--y02klWw_LY5QK_1ZP89CpswQSrJAujWGq2y8QhMfSx_UWLAvQYmH9ZdQ6mxFQMLfVwmgJ7Wf54PCY5wbqJ4h-C6Rktroy8zD8IDZqM0dX9M1dTR9_W3yXMXerkgiO_HmcG6rzCIrJhpGDyZj3ut2dfEQuWA_VioSdlHVRJNdoxsE35veHXxk7XQSMO4R3iN3PDCZlPozAMJ6rOJBP2-zwTJd4eA0Neruy_pPpUZawnzcy2k2nAVnlX_UgFBFsZGfVsDouKqWzci2dIktbXB6XP050SK9CgpuQivZRtsmNvFWsjkSYjIu1FqHHBqayvYIncxgLBCS31DoWEbG30WD-oXfmpjwI85cNFANHTrepRMKuQlLa5cMDbxdDr5-JfSe1Hg_D5xRtM9ZpmEvfpKmIsifHg_thiBJc5SOMasJcTgDFCsu_smMFRH03PJZ1fDbhNsAW9YyZK5holxY2ZuDDQk4";
//                MisOrganizationResponse body = makeRequest(
//                        String.format(ApiPaths.ExternalApi.ORGANIZATION, uuid),
//                        HttpMethod.GET,
//                        tokenValue,
//                        null,
//                        MisOrganizationResponse.class
//                );
//
//                if (body != null) {
//                    Organization updated = mapper.toOrganization(body);
//                    updated.setId(org.getId());
//                    updated.setUuid(org.getUuid());
//                    organizationRepository.save(updated);
//                    log.info("✅ Updated organization: {}", org.getName());
//                } else {
//                    log.warn("⚠️ No body for organization UUID={}", uuid);
//                }
//
//            } catch (Exception ex) {
//                log.error("❌ Failed to fetch organization from MIS for UUID={}", uuid, ex);
//            }
//        }
//
//        log.info("MIS sync finished");
//    }
}
