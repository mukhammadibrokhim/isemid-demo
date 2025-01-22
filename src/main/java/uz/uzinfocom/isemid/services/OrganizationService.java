package uz.uzinfocom.isemid.services;

import jakarta.servlet.http.HttpServletRequest;
import uz.uzinfocom.isemid.models.entities.Organization;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationService {

    ApiResponsePage getAllOrganizationsByFilter(int page, int size, HttpServletRequest request);

    Organization fetchOrganizationFromMisAndSave(UUID uuid, String tokenValue);

    Optional<Organization> findByUuid(UUID uuid);

    ApiResponse<Organization> getById(long id);

    ApiResponse<Organization> updateUserFromMis(UUID uuid, User currentUser, String token);

    Optional<Organization> findById(UUID uuid);
}
