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
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.user.dtos.requests.UserRequest;
import uz.uzinfocom.isemid.features.user.dtos.responses.PractitionerResponse;
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
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.createApiResponsePage;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableSortByIdDesc;
import static uz.uzinfocom.isemid.commons.utils.RestClientUtil.makeRequest;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String nnuzb) throws UsernameNotFoundException {
        return userRepository.findByNnuzb(nnuzb).orElseThrow(() -> new UsernameNotFoundException(nnuzb));
    }

    @Override
    public PagedResponse<?> getFilteredUsers(int page, int size, String nnuzb, String firstName, String lastName, String middleName, String phoneNumber, String accessLevelCode, Boolean active, String url) {
        Specification<User> spec = UserSpecification.getFilteredSpecification(nnuzb, firstName, lastName, middleName, phoneNumber, accessLevelCode, active);
        Page<User> userPage = userRepository.findAll(spec, pageableSortByIdDesc(page, size));
        Page<UserResponse> userResponsePage = userPage.map(userMapper::toResponse);
        return createApiResponsePage(page, userResponsePage, url);
    }

    @Override
    public Optional<User> findByUuid(UUID practitioner) {
        return userRepository.findByUuid(practitioner);
    }

    @Override
    public User fetchUserFromMisAndSave(UUID uuid, String token, List<Organization> organizations, Set<Role> roles) {
        PractitionerResponse response = makeRequest(String.format(ApiPaths.ExternalApi.PRACTITIONER, uuid), GET, token, null, PractitionerResponse.class);
        return userRepository.save(Objects.requireNonNull(convertToUser(response, organizations, roles)));
    }

    private User convertToUser(PractitionerResponse response, List<Organization> organizations, Set<Role> roles) {
        User user = userMapper.toUser(response);
        user.setRoles(roles);
        user.setOrganizations(organizations);
        return user;
    }

    @Override
    public ApiResponse<UserResponse> getById(long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, String.format(MessageUtil.NOT_FOUND_BY_ID, id)));
        return new ApiResponse<>(true, MessageUtil.FETCHED_SUCCESS, userMapper.toResponse(user));
    }

    @Override
    public ApiResponse<UserResponse> updateUserFromMis(UUID uuid, UserRequest user, String token) {
        return new ApiResponse<>(true, MessageUtil.UPDATED_SUCCESS, null);
    }

    @Override
    public ApiResponse<UserMeResponse> getByUuid(User currentUser) {
        User user = userRepository.findByUuid(currentUser.getUuid()).orElseThrow(() -> new ApiException(NOT_FOUND, String.format(MessageUtil.NOT_FOUND_BY_ID, currentUser.getUuid())));
        user.setCurrentOrganization(currentUser.getCurrentOrganization());
        return new ApiResponse<>(true, MessageUtil.FETCHED_SUCCESS, userMapper.toUserMeResponse(user));
    }

    @Override
    public List<User> getAllByIds(List<Long> userIds) {
        return userRepository.findAllById(userIds);
    }

    @Override
    public ApiResponse<UserResponse> update(long id, @Valid UserRequest userRequest) {
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, String.format(MessageUtil.NOT_FOUND_BY_ID, id)));
        userMapper.updateUser(userRequest, user);
        return new ApiResponse<>(true, MessageUtil.UPDATED_SUCCESS, userMapper.toResponse(userRepository.save(user)));
    }
}
