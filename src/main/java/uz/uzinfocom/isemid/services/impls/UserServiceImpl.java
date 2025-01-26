package uz.uzinfocom.isemid.services.impls;

import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.Organization;
import uz.uzinfocom.isemid.models.entities.Role;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.payload.responses.RespPractitioner;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.repositories.UserRepository;
import uz.uzinfocom.isemid.services.UserService;

import java.util.*;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uz.uzinfocom.isemid.utils.ApiUrlConstant.PRACTITIONER;
import static uz.uzinfocom.isemid.utils.CommonUtils.*;
import static uz.uzinfocom.isemid.utils.RestTemplateUtil.makeRequest;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    final UserRepository userRepository;

    @Override
    public ApiResponsePage getAllUsers(int page, int size, HttpServletRequest request) {
        Specification<User> spec = getFilteredSpecification();
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

            user.setCountry(address.getCountry());
            user.setState(address.getState());
            user.setCity(address.getCity());
            user.setDistrict(address.getDistrict());
            user.setLine(address.getLine());
        }

        user.setBirthDate(respPractitioner.getBirthDate());
        user.setActive(respPractitioner.isActive());
        user.setGender(respPractitioner.getGender());
        user.setRoles(roles);
        user.setOrganizations(organizations);
        return user;
    }

    public static Specification<User> getFilteredSpecification(
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
