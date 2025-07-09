package uz.uzinfocom.isemid.features.organization.services.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
import uz.uzinfocom.isemid.features.organization.dtos.requests.OrgRequest;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrgResponse;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrganizationResponse;
import uz.uzinfocom.isemid.features.organization.mappers.OrganizationMapper;
import uz.uzinfocom.isemid.features.organization.models.Organization;
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
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.createApiResponsePage;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableSortByIdDesc;
import static uz.uzinfocom.isemid.commons.utils.RestClientUtil.makeRequest;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper mapper;
    private final UserMapper userMapper;

    @Override
    public PagedResponse<?> getAllOrgByFilter(int page, int size, String name, String stateCode, String cityCode, String url) {
        Specification<Organization> spec = OrganizationSpecification.getFilteredSpecification(name, stateCode, cityCode);
        Page<Organization> organizationPage = organizationRepository.findAll(spec, pageableSortByIdDesc(page, size));
        Page<OrgResponse> orgResponses = organizationPage.map(mapper::toResponse);
        return createApiResponsePage(page, orgResponses, url);
    }

    @Override
    public Organization fetchOrganizationFromMisAndSave(UUID uuid, String tokenValue) {
        String json = makeRequest(String.format(ApiPaths.ExternalApi.ORGANIZATION, uuid), HttpMethod.GET, tokenValue, null, String.class);
        OrganizationResponse response;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
            response = mapper.readValue(json, OrganizationResponse.class);
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "JSON parsing failed", e);
        }
        return organizationRepository.saveAndFlush(mapper.toOrganization(response));
    }

    @Override
    public ApiResponse<OrgResponse> getById(long id) {
        Organization organization = organizationRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, MessageUtil.NOT_FOUND));
        return new ApiResponse<>(true, MessageUtil.FETCHED_SUCCESS, mapper.toResponse(organization));
    }

    @Override
    public ApiResponse<Organization> updateUserFromMis(UUID uuid, User currentUser, String token) {
        OrganizationResponse response = makeRequest(String.format(ApiPaths.ExternalApi.ORGANIZATION, uuid), HttpMethod.GET, token, null, OrganizationResponse.class);
        System.out.println(response);
        return new ApiResponse<>(true, MessageUtil.UPDATED_SUCCESS, null);
    }

    @Override
    public Optional<Organization> findById(UUID uuid) {
        return organizationRepository.findByUuid(uuid);
    }

    @Override
    public ApiResponse<?> update(UUID uuid, OrgRequest orgRequest) {
        Organization existingOrg = organizationRepository.findByUuid(uuid).orElseThrow(() -> new ApiException(NOT_FOUND, MessageUtil.NOT_FOUND));
        mapper.updateFromRequest(orgRequest, existingOrg);

        if (orgRequest.getParentUuid() != null) {
            Organization parentOrg = organizationRepository.findByUuid(orgRequest.getParentUuid())
                    .orElseThrow(() -> new ApiException(NOT_FOUND, MessageUtil.NOT_FOUND));

            parentOrg.getChildren().add(existingOrg);
            existingOrg.setParent(parentOrg);
            existingOrg.setParentId(parentOrg.getId());
        } else {
            existingOrg.setParent(null);
            existingOrg.setParentId(null);
        }

        return new ApiResponse<>(true, MessageUtil.UPDATED_SUCCESS, mapper.toResponse(organizationRepository.save(existingOrg)));
    }

    @Override
    public ApiResponse<List<Organization>> getChildrenByParentUuid(UUID uuid) {
        Organization organization = organizationRepository.findByUuid(uuid).orElseThrow(() -> new ApiException(NOT_FOUND, MessageUtil.NOT_FOUND));
        return new ApiResponse<>(true, MessageUtil.FETCHED_SUCCESS, organization.getChildren());
    }

    @Override
    public ApiResponse<?> getAllOrgUsers(long id) {
        Organization organization = organizationRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, MessageUtil.NOT_FOUND));
        List<UserMiniResponse> userResponses = organization.getUsers()
                .stream().map(userMapper::toMiniResponse).toList();
        return new ApiResponse<>(true, MessageUtil.OPERATION_SUCCESS, userResponses);
    }

    @Override
    public Optional<Organization> findById(long id) {
        return organizationRepository.findById(id);
    }

    @Override
    public Optional<Organization> findByTin(String tin) {
        return organizationRepository.findByTin(tin);
    }

    @Override
    public ApiResponse<?> getOrganizationsBySearch(String stateCode, String cityCode, String organizationType, String name) {
        return new ApiResponse<>(true, MessageUtil.OPERATION_SUCCESS, organizationRepository.searchByFilter(stateCode, cityCode, name, organizationType)
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
}
