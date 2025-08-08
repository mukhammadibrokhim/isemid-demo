package uz.uzinfocom.isemid.features.organization.services;

import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.features.organization.dtos.requests.OrgRequest;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrganizationResponse;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationService {

    PagedResponse<?> getAllOrgByFilter(int page, int size, String name, String stateCode, String cityCode, String url, User user);

    ApiResponse<?> update(UUID uuid, OrgRequest orgRequest);

    Organization fetchOrganizationFromMisAndSave(UUID uuid, String tokenValue);

    ApiResponse<OrganizationResponse> getById(long id);

    ApiResponse<OrganizationResponse> updateUserFromMis(UUID uuid, User currentUser, String token);

    Optional<Organization> findById(UUID uuid);

    ApiResponse<List<Organization>> getChildrenByParentUuid(UUID uuid);

    ApiResponse<?> getAllOrgUsers(long id);

    Optional<Organization> findById(long id);

    Optional<Organization> findByTin(String tin);

    ApiResponse<?> getOrganizationsBySearch(String stateCode, String cityCode, MedicalType medicalType, String name);

    Organization save(Organization newOrg);

    List<Organization> getAllOrganizations();
}
