package uz.uzinfocom.isemid.services;

import jakarta.servlet.http.HttpServletRequest;
import uz.uzinfocom.isemid.models.entities.Organization;
import uz.uzinfocom.isemid.models.entities.Role;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserService {
    ApiResponsePage getAllUsers(int page, int size, HttpServletRequest request);

    Optional<User> findByUuid(UUID practitioner);

    User fetchUserFromMisAndSave(UUID practitioner, String token, List<Organization> organizations, Set<Role> roles);

    ApiResponse<User> getUserById(long id);

    ApiResponse<User> updateUserFromMis(UUID uuid, User user, String token);

    ApiResponse<User> getByUuid(UUID uuid);
}
