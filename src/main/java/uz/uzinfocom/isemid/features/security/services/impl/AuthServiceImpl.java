package uz.uzinfocom.isemid.features.security.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.services.OrganizationService;
import uz.uzinfocom.isemid.features.security.dtos.requests.RegisterRequest;
import uz.uzinfocom.isemid.features.security.services.AuthService;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.reposiotries.UserRepository;
import uz.uzinfocom.isemid.features.user.services.RoleService;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static uz.uzinfocom.isemid.commons.utils.CommonUtil.checkNnuzb;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final OrganizationService organizationService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ApiResponse<?> signUp(RegisterRequest request) {
        if (!checkNnuzb(request.getNnuzb())) throw new ApiException(BAD_REQUEST, MessageUtil.BAD_REQUEST + " Nnuzb");

        if (userRepository.findByNnuzb(request.getNnuzb()).isPresent()) {
            throw new ApiException(BAD_REQUEST, MessageUtil.ALREADY_EXISTS + ": " + request.getNnuzb());
        }

        Organization organization = organizationService.findByTin(request.getOrganization().getTin())
                .orElseGet(() -> {
                    Organization newOrg = Organization.builder()
                            .uuid(UUID.randomUUID())
                            .name(request.getOrganization().getName())
                            .active(true)
                            .phone(request.getOrganization().getPhone())
                            .email(request.getOrganization().getEmail())
                            .tin(request.getOrganization().getTin())
                            .countryCode(request.getCountryCode())
                            .stateCode(request.getStateCode())
                            .cityCode(request.getCityCode())
                            .serviceAreaCode(request.getStateCode())
                            .line(request.getOrganization().getLine())
                            .build();
                    return organizationService.save(newOrg);
                });
        User user = User.builder()
                .uuid(UUID.randomUUID())
                .active(true)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .middleName(request.getMiddleName())
                .nnuzb(request.getNnuzb())
                .password(passwordEncoder.encode(request.getPassword()))
                .ppn(request.getPpn())
                .birthDate(request.getBirthDate())
                .genderCode(request.getGenderCode())
                .countryCode(request.getCountryCode())
                .stateCode(request.getStateCode())
                .cityCode(request.getCityCode())
                .positionCode(request.getPositionCode())
                .phoneNumber(request.getPhoneNumber())
                .line(request.getLine())
                .organizations(List.of(organization))
                .roles(Set.of(Objects.requireNonNull(roleService.getAllRoles().stream().filter(role -> role.getName().equals("isemid_qr_user")).findFirst().orElse(null))))
                .build();

        userRepository.save(user);
        return new ApiResponse<>(true, MessageUtil.OPERATION_SUCCESS, null);
    }
}
