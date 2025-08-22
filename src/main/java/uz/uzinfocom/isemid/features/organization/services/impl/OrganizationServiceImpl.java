package uz.uzinfocom.isemid.features.organization.services.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.organization.dtos.requests.OrgRequest;
import uz.uzinfocom.isemid.features.organization.dtos.responses.MisOrganizationResponse;
import uz.uzinfocom.isemid.features.organization.mappers.OrganizationMapper;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;
import uz.uzinfocom.isemid.features.organization.reposiotries.OrganizationRepository;
import uz.uzinfocom.isemid.features.organization.services.OrganizationService;
import uz.uzinfocom.isemid.features.organization.specifications.OrganizationSpecification;
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

    @Override
    public Page<Organization> filter(int page, int size, String name, String stateCode, String cityCode, User user) {
        Specification<Organization> spec = OrganizationSpecification.build(name, stateCode, cityCode, user);
        return organizationRepository.findAll(spec, pageableSortByIdDesc(page, size));
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
    public Organization getById(long id) {
        return organizationRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
    }

    @Override
    public Organization updateUserFromMis(UUID uuid, User currentUser, String token) {
        MisOrganizationResponse response = makeRequest(String.format(ApiPaths.ExternalApi.ORGANIZATION, uuid), HttpMethod.GET, token, null, MisOrganizationResponse.class);
        Organization organization = mapper.toOrganization(response);
        return organizationRepository.save(organization);

    }

    @Override
    public Optional<Organization> findById(UUID uuid) {
        return organizationRepository.findByUuid(uuid);
    }

    @Override
    public Organization update(UUID uuid, OrgRequest orgRequest) {
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

        return organizationRepository.save(existingOrg);
    }

    @Override
    public List<Organization> getChildrenByParentUuid(UUID uuid) {
        Organization organization = organizationRepository.findByUuid(uuid).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", uuid)));
        return organization.getChildren();
    }

    @Override
    public List<User> getAllOrgUsers(long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ApiException(NOT_FOUND,
                        messageUtil.get("error.not_found_by_id", id)));

        return organization.getUsers();
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
    public List<Organization> getOrganizationsBySearch(String stateCode, String cityCode, MedicalType medicalType, String name) {
        return organizationRepository.searchByFilter(stateCode, cityCode, medicalType != null ? medicalType.name() : null, name);
    }

    @Override
    public Organization save(Organization newOrg) {
        return organizationRepository.save(newOrg);
    }

    @Override
    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    @PostConstruct
    @Transactional
    public void syncOrganizationsFromMISOnStartup() {
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
                    Organization updated = mapper.toOrganization(body);
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
    }
}
