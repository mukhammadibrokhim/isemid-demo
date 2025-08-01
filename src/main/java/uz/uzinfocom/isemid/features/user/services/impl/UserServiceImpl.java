package uz.uzinfocom.isemid.features.user.services.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.commons.utils.PagedResponseUtil;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.user.dtos.requests.UserRequest;
import uz.uzinfocom.isemid.features.user.dtos.responses.MisUserResponse;
import uz.uzinfocom.isemid.features.user.dtos.responses.UserMeResponse;
import uz.uzinfocom.isemid.features.user.dtos.responses.UserResponse;
import uz.uzinfocom.isemid.features.user.mappers.UserMapper;
import uz.uzinfocom.isemid.features.user.models.Role;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.reposiotries.UserRepository;
import uz.uzinfocom.isemid.features.user.services.UserService;
import uz.uzinfocom.isemid.features.user.specifications.UserSpecification;

import java.util.*;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableSortByIdDesc;
import static uz.uzinfocom.isemid.commons.utils.RestClientUtil.makeRequest;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final MessageUtil messageUtil;

    @Override
    public UserDetails loadUserByUsername(String nnuzb) throws UsernameNotFoundException {
        return userRepository.findByNnuzb(nnuzb).orElseThrow(() -> new UsernameNotFoundException(nnuzb));
    }

    @Override
    public PagedResponse<?> getFilteredUsers(int page, int size, String nnuzb, String firstName, String lastName, String middleName, String phoneNumber, String accessLevelCode, Boolean active, String url, User currentUser) {
        Specification<User> spec = UserSpecification.build(nnuzb, firstName, lastName, middleName, phoneNumber, accessLevelCode, active, currentUser);
        Page<UserResponse> userPage = userRepository.findAll(spec, pageableSortByIdDesc(page, size))
                .map(userMapper::toResponse);
        return PagedResponseUtil.build(page, userPage, url, messageUtil);
    }

    @Override
    public Optional<User> findByUuid(UUID practitioner) {
        return userRepository.findByUuid(practitioner);
    }

    @Override
    public User fetchUserFromMisAndSave(UUID uuid, String token, List<Organization> organizations, Set<Role> roles) {
        MisUserResponse response = makeRequest(String.format(ApiPaths.ExternalApi.PRACTITIONER, uuid), GET, token, null, MisUserResponse.class);
        return userRepository.saveAndFlush(Objects.requireNonNull(convertToUser(response, organizations, roles)));
    }

    private User convertToUser(MisUserResponse response, List<Organization> organizations, Set<Role> roles) {
        User user = userMapper.toUser(response);
        user.setRoles(roles);
        user.setOrganizations(organizations);
        return user;
    }

    @Override
    public ApiResponse<UserResponse> getById(long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
        return ApiResponse.success(messageUtil.get("message.success"), userMapper.toResponse(user));
    }

    @Override
    public ApiResponse<UserResponse> updateUserFromMis(UUID uuid, UserRequest user, String token) {
        return ApiResponse.success(messageUtil.get("message.success"), null);
    }

    @Override
    public User save(User existingUser) {
        return userRepository.save(existingUser);
    }

    @Override
    public ApiResponse<UserMeResponse> getByUser(User currentUser) {
        return ApiResponse.success(messageUtil.get("message.success"), userMapper.toUserMeResponse(currentUser));
    }

    @Override
    public List<User> getAllByIds(List<Long> userIds) {
        return userRepository.findAllById(userIds);
    }

    @Override
    public ApiResponse<UserResponse> update(long id, @Valid UserRequest userRequest) {
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
        userMapper.updateUser(userRequest, user);
        return ApiResponse.success(messageUtil.get("message.success"), userMapper.toResponse(userRepository.save(user)));
    }
}
