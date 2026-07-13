package uz.uzinfocom.isemid.features.user.services.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.integration.IntegrationUrlFactory;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.user.dtos.requests.UserFilterRequest;
import uz.uzinfocom.isemid.features.user.dtos.requests.UserRequest;
import uz.uzinfocom.isemid.features.user.dtos.responses.MisUserResponse;
import uz.uzinfocom.isemid.features.user.mappers.UserMapper;
import uz.uzinfocom.isemid.features.user.models.Role;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.reposiotries.UserRepository;
import uz.uzinfocom.isemid.features.user.services.RoleService;
import uz.uzinfocom.isemid.features.user.services.UserService;
import uz.uzinfocom.isemid.features.user.specifications.UserSpecification;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.springframework.beans.support.PagedListHolder.DEFAULT_PAGE_SIZE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.MAX_PAGE_SIZE;
import static uz.uzinfocom.isemid.commons.utils.RestClientUtil.makeRequest;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final MessageUtil messageUtil;
    private final RoleService roleService;
    private final IntegrationUrlFactory integrationUrlFactory;

    @Override
    public UserDetails loadUserByUsername(String nnuzb) throws UsernameNotFoundException {
        return userRepository.findByNnuzb(nnuzb).orElseThrow(() -> new UsernameNotFoundException(nnuzb));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> filter(
            UserFilterRequest filter,
            User currentUser
    ) {
        int page = normalizePage(filter.getPage());
        int size = normalizeSize(filter.getSize());

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.asc("lastName"),
                        Sort.Order.asc("firstName"),
                        Sort.Order.asc("middleName"),
                        Sort.Order.desc("id")
                )
        );
        return userRepository.findAll(UserSpecification.build(filter, currentUser), pageable);
    }

    /**
     * Loyihada sahifalash 1 dan boshlanadi (bkz. {@code PageableUtils.pageableSortByIdDesc}),
     * Spring Data esa 0 dan. Shu sabab bu yerda ham {@code page - 1} qilinadi -
     * aks holda page=1 (birinchi sahifa) Spring uchun ikkinchi sahifa bo'lib qolib,
     * natijalar noto'g'ri (yoki bo'sh) chiqadi.
     */
    private int normalizePage(int page) {
        return Math.max(page - 1, 0);
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }

        return Math.min(size, MAX_PAGE_SIZE);
    }
    @Override
    public Optional<User> findByUuid(UUID practitioner) {
        return userRepository.findByUuid(practitioner);
    }

    @Override
    public User fetchUserFromMisAndSave(UUID uuid, String token, List<Organization> organizations, Set<Role> roles) {
        URI uri = integrationUrlFactory.practitionerUrl(uuid.toString());
        MisUserResponse response = makeRequest(String.valueOf(uri), GET, token, null, MisUserResponse.class, messageUtil);
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
    public User updateUserFromMis(UUID uuid, String token) {
        User exustingUser = userRepository.findByUuid(uuid).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", uuid)));
        URI uri = integrationUrlFactory.practitionerUrl(uuid.toString());
        MisUserResponse response = makeRequest(String.valueOf(uri), GET, token, null, MisUserResponse.class, messageUtil);
        userMapper.updateFromMis(exustingUser, response);
        return userRepository.save(exustingUser);
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
    public Page<User> filter(int page, int size, String nnuzb, String firstName, String lastName, String middleName,
                             String phoneNumber, Boolean active, User currentUser) {
        return null;
    }

    @Override
    public User update(long id, @Valid UserRequest userRequest) {
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
        userMapper.update(userRequest, user);
        return userRepository.save(user);
    }
}
