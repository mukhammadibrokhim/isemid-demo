package uz.uzinfocom.isemid.features.user.services;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.user.dtos.requests.UserFilterRequest;
import uz.uzinfocom.isemid.features.user.dtos.requests.UserRequest;
import uz.uzinfocom.isemid.features.user.models.Role;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserService {

    Page<User> filter(int page, int size, String nnuzb, String firstName, String lastName, String middleName,
                      String phoneNumber, Boolean active, User currentUser);

    Page<User> filter(UserFilterRequest filter, User currentUser);


    User update(long id, @Valid UserRequest userRequest);

    User getById(long id);

    List<User> getAllByIds(List<Long> userIds);

    Optional<User> findByUuid(UUID uuid);

    User fetchUserFromMisAndSave(UUID practitioner, String token, List<Organization> organizations, Set<Role> roles);

    User updateUserFromMis(UUID uuid, String token);

    User save(User existingUser);

    void removeRoleFromUser(Long id, Long roleId);
}