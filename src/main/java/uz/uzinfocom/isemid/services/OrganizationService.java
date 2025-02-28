package uz.uzinfocom.isemid.services;

import jakarta.servlet.http.HttpServletRequest;
import uz.uzinfocom.isemid.models.entities.Organization;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.payload.requests.ReqOrganization;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationService {

    ApiResponsePage getAllOrganizationsByFilter(int page, int size, String name, String stateCode, String cityCode, HttpServletRequest request);

    Organization fetchOrganizationFromMisAndSave(UUID uuid, String tokenValue);

    ApiResponse<Organization> getById(long id);

    ApiResponse<Organization> updateUserFromMis(UUID uuid, User currentUser, String token);

    Optional<Organization> findById(UUID uuid);

    ApiResponse<Organization> update(UUID uuid, ReqOrganization reqOrganization, User currentUser);

    ApiResponse<List<Organization>> getChildrenByParentUuid(UUID uuid);

    ApiResponse<?> getAllOrgUsers(long id);

    Optional<Organization> findById(long id);

    ApiResponse<?> getOrganizationsBySearch(String stateCode, String cityCode, String name);

}
