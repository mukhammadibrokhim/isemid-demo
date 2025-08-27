package uz.uzinfocom.isemid.features.user.services.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.user.dtos.requests.UserRequest;
import uz.uzinfocom.isemid.features.user.dtos.responses.MisUserResponse;
import uz.uzinfocom.isemid.features.user.mappers.UserMapper;
import uz.uzinfocom.isemid.features.user.models.Role;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.reposiotries.UserRepository;
import uz.uzinfocom.isemid.features.user.services.RoleService;
import uz.uzinfocom.isemid.features.user.services.UserService;
import uz.uzinfocom.isemid.features.user.specifications.UserSpecification;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableSortByIdDesc;
import static uz.uzinfocom.isemid.commons.utils.RestClientUtil.makeRequest;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final MessageUtil messageUtil;
    private final RoleService roleService;

    @Override
    public UserDetails loadUserByUsername(String nnuzb) throws UsernameNotFoundException {
        return userRepository.findByNnuzb(nnuzb).orElseThrow(() -> new UsernameNotFoundException(nnuzb));
    }

    @Override
    public Page<User> filter(int page, int size, String nnuzb, String firstName, String lastName, String middleName, String phoneNumber, String accessLevelCode, Boolean active, User currentUser) {
        Specification<User> spec = UserSpecification.build(nnuzb, firstName, lastName, middleName, phoneNumber, accessLevelCode, active, currentUser);
        return userRepository.findAll(spec, pageableSortByIdDesc(page, size));
    }

    @Override
    public Optional<User> findByUuid(UUID practitioner) {
        return userRepository.findByUuid(practitioner);
    }

    @Override
    public User fetchUserFromMisAndSave(UUID uuid, String token, List<Organization> organizations, Set<Role> roles) {
        MisUserResponse response = makeRequest(String.format(ApiPaths.ExternalApi.PRACTITIONER, uuid), GET, token, null, MisUserResponse.class);
        User user = convertToUser(response, organizations, roles);
        return userRepository.saveAndFlush(user);
    }

    private User convertToUser(MisUserResponse response, List<Organization> organizations, Set<Role> roles) {
        User user = userMapper.toUser(response);
        user.setRoles(roles);
        user.setOrganizations(organizations);
        return user;
    }

    @Override
    public User getById(long id) {
        return userRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
    }

    @Override
    public User updateUserFromMis(UUID uuid, UserRequest user, String token) {
        return new User();
    }

    @Override
    public User save(User existingUser) {
        return userRepository.save(existingUser);
    }

    @Override
    public void removeRoleFromUser(Long id, Long roleId) {
        User user = this.getById(id);
        Role role = roleService.getById(roleId);
        if (!user.getRoles().contains(role)) {
            throw new ApiException(BAD_REQUEST, messageUtil.get("error.user_no_role"));
        }
        userRepository.removeRoleFromUser(id, roleId);

    }

    @Override
    public List<User> getAllByIds(List<Long> userIds) {
        return userRepository.findAllById(userIds);
    }

    @Override
    public User update(long id, @Valid UserRequest userRequest) {
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
        userMapper.update(userRequest, user);
        return userRepository.save(user);
    }
}
