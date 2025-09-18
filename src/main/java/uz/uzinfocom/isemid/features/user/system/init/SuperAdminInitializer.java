package uz.uzinfocom.isemid.features.user.system.init;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.isemid.commons.seed.ModuleDataInitializer;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;
import uz.uzinfocom.isemid.features.organization.models.enums.ServiceType;
import uz.uzinfocom.isemid.features.organization.services.OrganizationService;
import uz.uzinfocom.isemid.features.patient.enums.Gender;
import uz.uzinfocom.isemid.features.user.models.Role;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.reposiotries.RoleRepository;
import uz.uzinfocom.isemid.features.user.reposiotries.UserRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Order(4)
@Component
@RequiredArgsConstructor
public class SuperAdminInitializer implements ModuleDataInitializer {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrganizationService organizationService;

    @Override
    @Transactional
    public void init() {
        Role superAdminRole = roleRepository.findByName("isemid_super_admin")
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .name("isemid_super_admin")
                                .isActive(true)
                                .build()
                ));
        Organization organization = organizationService.findById(UUID.fromString("37007889-eabc-42a9-83a2-136e7a105cb8"))
                .orElseGet(() -> Organization.builder()
                        .uuid(UUID.fromString("37007889-eabc-42a9-83a2-136e7a105cb8"))
                        .name("ITMED Respublika")
                        .tin("987987923")
                        .countryCode("UZB")
                        .stateCode("UZ-TK")
                        .cityCode("TK-273")
                        .district("TK-273012")
                        .levelType(LevelType.REPUBLICAN)
                        .medicalType(MedicalType.SANEPID_SERVICE)
                        .serviceTypes(List.of(ServiceType.OTHER))
                        .serviceAreaCode("UZ-TK")
                        .phone("900000000")
                        .email("test@bk.ru")
                        .line("2- МАССИВ 45/15")
                        .build());
        organizationService.save(organization);
        boolean exists = userRepository.existsByRoleName("isemid_super_admin");
        if (!exists) {
            User superAdmin = userRepository.findByUuid(UUID.fromString("c2f1be85-7688-4eae-82ac-20dc956933e9"))
                    .orElseGet(() -> User.builder()
                            .uuid(UUID.fromString("c2f1be85-7688-4eae-82ac-20dc956933e9"))
                            .organizations(List.of(organization))
                            .active(true)
                            .nnuzb("12345678901234")
                            .genderCode(Gender.male.name())
                            .ppn("")
                            .countryCode("UZB")
                            .stateCode("UZ-TK")
                            .cityCode("TK-269")
                            .line("Mo'minova 7/11")
                            .roles(Set.of(superAdminRole))
                            .build());
            userRepository.save(superAdmin);
            System.out.println("✅ Super Admin initialized successfully.");
        } else {
            System.out.println("ℹ️ Super Admin already exists, skipping initialization.");
        }
    }

}
