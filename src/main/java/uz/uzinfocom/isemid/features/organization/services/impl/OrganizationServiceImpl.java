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
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.integration.IntegrationUrlFactory;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.commons.utils.TextNormalizationUtils;
import uz.uzinfocom.isemid.features.organization.dtos.requests.OrgRequest;
import uz.uzinfocom.isemid.features.organization.dtos.responses.MisOrganizationResponse;
import uz.uzinfocom.isemid.features.organization.mappers.OrganizationMapper;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;
import uz.uzinfocom.isemid.features.organization.reposiotries.OrganizationRepository;
import uz.uzinfocom.isemid.features.organization.services.OrganizationService;
import uz.uzinfocom.isemid.features.organization.specifications.OrganizationSpecification;
import uz.uzinfocom.isemid.features.user.models.User;

import java.net.URI;
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
    private final IntegrationUrlFactory integrationUrlFactory;

    @Override
    public Page<Organization> filter(int page, int size, String name, String stateCode, String cityCode, User user) {
        Specification<Organization> spec = OrganizationSpecification.build(name, stateCode, cityCode, user);
        return organizationRepository.findAll(spec, pageableSortByIdDesc(page, size));
    }

    @Override
    public Organization fetchOrganizationFromMisAndSave(UUID uuid, String tokenValue) {
        return organizationRepository.saveAndFlush(mapper.toOrganization(fetchOrganization(uuid, tokenValue)));
    }

    @Override
    public Organization getById(long id) {
        return organizationRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
    }

    @Override
    public Organization updateFromMis(UUID uuid, User currentUser, String token) {
        Organization existingOrganization = organizationRepository.findByUuid(uuid)
                .orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", uuid)));

        MisOrganizationResponse response = fetchOrganization(uuid, token);

        mapper.updateOrganizationFromMis(existingOrganization, response);

        return organizationRepository.save(existingOrganization);
    }


    private MisOrganizationResponse fetchOrganization(UUID uuid, String token) {
        URI uri = integrationUrlFactory.organizationUrl(String.valueOf(uuid));
        String json = makeRequest(String.valueOf(uri), HttpMethod.GET, token, null, String.class, messageUtil);
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
            return mapper.readValue(json, MisOrganizationResponse.class);
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "JSON parsing failed", e);
        }
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
        String normalizedName = name != null ? TextNormalizationUtils.stripApostrophes(name.toLowerCase()) : null;
        return organizationRepository.searchByFilter(stateCode, cityCode, medicalType != null ? medicalType.name() : null, normalizedName);
    }

    @Override
    public Organization save(Organization newOrg) {
        return organizationRepository.save(newOrg);
    }

    @Override
    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }
}
