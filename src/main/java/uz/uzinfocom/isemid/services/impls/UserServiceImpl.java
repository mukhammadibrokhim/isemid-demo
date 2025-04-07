package uz.uzinfocom.isemid.services.impls;

import jakarta.persistence.Transient;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.Organization;
import uz.uzinfocom.isemid.models.entities.Role;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.enums.CatalogType;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;
import uz.uzinfocom.isemid.models.payload.requests.ReqRegister;
import uz.uzinfocom.isemid.models.payload.requests.ReqUser;
import uz.uzinfocom.isemid.models.payload.responses.RespPractitioner;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.repositories.UserRepository;
import uz.uzinfocom.isemid.services.OrganizationService;
import uz.uzinfocom.isemid.services.RoleService;
import uz.uzinfocom.isemid.services.UserService;
import uz.uzinfocom.isemid.services.value_set.CatalogService;

import java.util.*;
import java.util.stream.Stream;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.util.StringUtils.hasText;
import static uz.uzinfocom.isemid.models.entities.enums.CatalogType.*;
import static uz.uzinfocom.isemid.utils.ApiUrlConstant.PRACTITIONER;
import static uz.uzinfocom.isemid.utils.CommonUtils.*;
import static uz.uzinfocom.isemid.utils.RestTemplateUtil.makeRequest;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    final UserRepository userRepository;
    final OrganizationService organizationService;
    final CatalogService catalogService;
    final PasswordEncoder passwordEncoder;
    final RoleService roleService;

    public UserServiceImpl(UserRepository userRepository, OrganizationService organizationService, CatalogService catalogService, @Lazy PasswordEncoder passwordEncoder, RoleService roleService) {
        this.userRepository = userRepository;
        this.organizationService = organizationService;
        this.catalogService = catalogService;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    private Catalog mapCatalog(CatalogType type, String code) {
        return catalogService.findByTypeAndCode(type, code).orElse(null);
    }

    @Override
    public UserDetails loadUserByUsername(String nnuzb) throws UsernameNotFoundException {
        return userRepository.findByNnuzb(nnuzb).orElseThrow(() -> new UsernameNotFoundException(nnuzb));
    }

    @Override
    public ApiResponsePage getAllUsers(int page, int size, String nnuzb, String firstName, String lastName, String middleName, String phoneNumber, String accessLevelCode, Boolean active, HttpServletRequest request) {
        Specification<User> spec = getFilteredSpecification(nnuzb, firstName, lastName, middleName, phoneNumber, accessLevelCode, active);
        Page<User> userPage = userRepository.findAll(spec, pageableSortByIdDesc(page, size));
        return createApiResponsePage(page, userPage, getFullURL(request));
    }

    @Override
    public Optional<User> findByUuid(UUID practitioner) {
        return userRepository.findByUuid(practitioner);
    }

    @Override
    public User fetchUserFromMisAndSave(UUID uuid, String token, List<Organization> organizations, Set<Role> roles) {
        RespPractitioner response = makeRequest(String.format(PRACTITIONER, uuid), GET, token, null, RespPractitioner.class);
        return userRepository.save(mappingPractitionerToUser(response, organizations, roles));
    }

    @Override
    public ApiResponse<User> getUserById(long id) {
        return new ApiResponse<>(true, "User fetch successfully.", userRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "User with id " + id + " not found")));
    }

    @Override
    public ApiResponse<User> updateUserFromMis(UUID uuid, User user, String token) {

        return new ApiResponse<>(true, "User successfully update.", null);
    }

    @Override
    public ApiResponse<User> getByUuid(UUID uuid) {
        return new ApiResponse<>(true, "User fetch successfully.",
                userRepository.findByUuid(uuid).orElseThrow(()-> new ApiException(NOT_FOUND, "User not found for userUuid: " + uuid)));
    }

    @Override
    public ApiResponse<User> updateUser(long id, @Valid ReqUser reqUser) {
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "User with id " + id + " not found"));
        Optional.ofNullable(reqUser.getFirstName()).ifPresent(user::setFirstName);
        Optional.ofNullable(reqUser.getLastName()).ifPresent(user::setLastName);
        Optional.ofNullable(reqUser.getMiddleName()).ifPresent(user::setMiddleName);
        Optional.ofNullable(reqUser.getNnuzb()).ifPresent(user::setNnuzb);
        Optional.ofNullable(reqUser.getPpn()).ifPresent(user::setPpn);
        Optional.ofNullable(reqUser.getBirthDate()).ifPresent(user::setBirthDate);
        Optional.of(reqUser.isActive()).ifPresent(user::setActive);
        Optional.ofNullable(reqUser.getGender()).ifPresent(u -> catalogService.findByTypeAndCode(CatalogType.gender, u.name()));
        Optional.ofNullable(reqUser.getPhoneNumber()).ifPresent(user::setPhoneNumber);
        Optional.ofNullable(reqUser.getCountryCode()).ifPresent(c -> catalogService.findByTypeAndCode(CatalogType.country, c));
        Optional.ofNullable(reqUser.getStateCode()).ifPresent(c -> catalogService.findByTypeAndCode(CatalogType.state, c));
        Optional.ofNullable(reqUser.getCityCode()).ifPresent(c -> catalogService.findByTypeAndCode(CatalogType.city, c));
        Optional.ofNullable(reqUser.getDistrict()).ifPresent(user::setDistrict);
        Optional.ofNullable(reqUser.getLine()).ifPresent(user::setLine);
        Optional.ofNullable(reqUser.getAccessLevelCode()).ifPresent(c -> catalogService.findByTypeAndCode(level, c));
        return new ApiResponse<>(true, "Success", user);
    }

    @Override
    public List<User> getAllByIds(List<Long> userIds) {
        return userRepository.findAllById(userIds);
    }

    @Transient
    @Override
    public ApiResponse<?> signUpUser(ReqRegister reqRegister) {

        if (!checkNnuzb(reqRegister.getNnuzb())) throw new ApiException(BAD_REQUEST, "Invalid nnuzb.");

        if (userRepository.findByNnuzb(reqRegister.getNnuzb()).isPresent()) {
            throw new ApiException(BAD_REQUEST, "Nnuzb already exists.");
        }

        Organization organization = organizationService.findByTin(reqRegister.getOrganization().getTin())
                .orElseGet(() -> {
                    Organization newOrg = Organization.builder()
                            .uuid(UUID.randomUUID())
                            .name(reqRegister.getOrganization().getName())
                            .active(true)
                            .phone(reqRegister.getOrganization().getPhone())
                            .email(reqRegister.getOrganization().getEmail())
                            .tin(reqRegister.getOrganization().getTin())
                            .country(mapCatalog(country, reqRegister.getOrganization().getCountryCode()))
                            .state(mapCatalog(state, reqRegister.getOrganization().getStateCode()))
                            .city(mapCatalog(city, reqRegister.getOrganization().getCityCode()))
                            .serviceArea(mapCatalog(state, reqRegister.getOrganization().getServiceAreaCode()))
                            .line(reqRegister.getOrganization().getLine())
                            .build();
                    return organizationService.save(newOrg);
                });
        User user = User.builder()
                .uuid(UUID.randomUUID())
                .active(true)
                .firstName(reqRegister.getFirstName())
                .lastName(reqRegister.getLastName())
                .middleName(reqRegister.getMiddleName())
                .nnuzb(reqRegister.getNnuzb())
                .password(passwordEncoder.encode(reqRegister.getNnuzb()))
                .ppn(reqRegister.getPpn())
                .birthDate(reqRegister.getBirthDate())
                .gender(mapCatalog(gender, reqRegister.getGenderCode()))
                .phoneNumber(reqRegister.getPhoneNumber())
                .country(mapCatalog(country, reqRegister.getCountryCode()))
                .state(mapCatalog(state, reqRegister.getStateCode()))
                .city(mapCatalog(city, reqRegister.getCityCode()))
                .line(reqRegister.getLine())
                .position(mapCatalog(position, reqRegister.getPositionCode()))
                .organizations(List.of(organization))
                .roles(Set.of(Objects.requireNonNull(roleService.getAllRoles().stream().filter(role -> role.getName().equals("isemid_qr_user")).findFirst().orElse(null))))
                .build();

        return new ApiResponse<>(true, "Success", userRepository.save(user));
    }

    private User mappingPractitionerToUser(RespPractitioner respPractitioner, List<Organization> organizations, Set<Role> roles) {
        User user = new User();
        user.setUuid(UUID.fromString(respPractitioner.getId()));
        user.setNnuzb(getIdentifierValueByCode(respPractitioner.getIdentifier(), "NNUZB"));
        user.setPpn(getIdentifierValueByCode(respPractitioner.getIdentifier(), "PPN"));
        user.setFirstName(respPractitioner.getName().getFirstName());
        user.setLastName(respPractitioner.getName().getLastName());
        user.setMiddleName(respPractitioner.getName().getPatronymic());
        RespPractitioner.Address address;
        if (respPractitioner.getAddress() != null && !respPractitioner.getAddress().isEmpty()) {
            if (respPractitioner.getAddress().size() == 1) {
                address = respPractitioner.getAddress().getFirst();
            } else {
                address = respPractitioner.getAddress().stream()
                        .filter(addr -> "both".equalsIgnoreCase(addr.getType()))
                        .findFirst()
                        .orElse(respPractitioner.getAddress().getFirst());
            }

            user.setCountry(catalogService.findByTypeAndCode(CatalogType.country, address.getCountry()).orElse(null));
            user.setState(catalogService.findByTypeAndCode(CatalogType.state, address.getState()).orElse(null));
            user.setCity(catalogService.findByTypeAndCode(CatalogType.city, address.getCity()).orElse(null));
            user.setDistrict(address.getDistrict());
            user.setLine(address.getLine());
        }

        if (!respPractitioner.getTelecom().isEmpty()) {
            user.setPhoneNumber(respPractitioner.getTelecom().stream()
                    .filter(telecom -> telecom.getSystem().equals("phone"))
                    .map(RespPractitioner.Telecom::getValue).findFirst().orElse(null));
        }

        user.setBirthDate(respPractitioner.getBirthDate());
        user.setActive(respPractitioner.isActive());
        user.setGender(catalogService.findByTypeAndCode(CatalogType.gender, respPractitioner.getGender()).orElse(null));
        user.setRoles(roles);
        user.setOrganizations(organizations);
        return user;
    }

    public static Specification<User> getFilteredSpecification(
            final String nnuzb,
            final String firstName,
            final String lastName,
            final String middleName,
            final String phoneNumber,
            final String accessLevelCode,
            final Boolean active
    ) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                Stream.of(
                                hasText(nnuzb) ? criteriaBuilder.equal(root.get("nnuzb"), nnuzb) : null,
                                hasText(firstName) ? criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%") : null,
                                hasText(lastName) ? criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%") : null,
                                hasText(middleName) ? criteriaBuilder.like(criteriaBuilder.lower(root.get("middleName")), "%" + middleName.toLowerCase() + "%") : null,
                                hasText(phoneNumber) ? criteriaBuilder.equal(root.get("phoneNumber"), phoneNumber) : null,
                                hasText(accessLevelCode) ? criteriaBuilder.equal(root.join("accessLevel", JoinType.LEFT).get("code"), accessLevelCode) : null,
                                active != null ? criteriaBuilder.equal(root.get("active"), active) : null
                        )
                        .filter(Objects::nonNull)
                        .toArray(Predicate[]::new)
        );
    }

}
