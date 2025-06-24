package uz.uzinfocom.isemid.features.user.services;

import jakarta.validation.Valid;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.user.dtos.requests.UserRequest;
import uz.uzinfocom.isemid.features.user.dtos.responses.UserMeResponse;
import uz.uzinfocom.isemid.features.user.dtos.responses.UserResponse;
import uz.uzinfocom.isemid.features.user.models.Role;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserService {
    PagedResponse<?> getFilteredUsers(int page, int size, String nnuzb, String firstName, String lastName, String middleName, String phoneNumber, String accessLevelCode, Boolean active, String url);

    ApiResponse<UserResponse> update(long id, @Valid UserRequest userRequest);

    ApiResponse<UserResponse> getById(long id);

    ApiResponse<UserMeResponse> getByUuid(User user);

    List<User> getAllByIds(List<Long> userIds);

    Optional<User> findByUuid(UUID uuid);

    User fetchUserFromMisAndSave(UUID practitioner, String token, List<Organization> organizations, Set<Role> roles);

    ApiResponse<UserResponse> updateUserFromMis(UUID uuid, UserRequest user, String token);
}