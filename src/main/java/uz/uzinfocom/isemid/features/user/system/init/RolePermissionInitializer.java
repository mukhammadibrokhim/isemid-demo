package uz.uzinfocom.isemid.features.user.system.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.user.models.Permission;
import uz.uzinfocom.isemid.features.user.models.Role;
import uz.uzinfocom.isemid.features.user.reposiotries.PermissionRepository;
import uz.uzinfocom.isemid.features.user.reposiotries.RoleRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class RolePermissionInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public void run(String... args) {
        log.info("♻️ Initializing default roles and permissions...");

        List<String> allSubjects = List.of(
                "ALL", "EMPLOYEES", "ORGANIZATIONS", "ROLES", "PERMISSIONS", "FORM058",
                "FORM058_ATTACHED_CARDS", "FORM058_CHANGE_ORGANIZATION", "RECEIVED", "FORM058_COMPLETE_BY_SUPERVISOR",
                "USER_INCOMINGS", "CARD_FULL_INFO", "MKB_LIST", "COUNTRIES", "STATES", "CITIES", "NEIGHBORHOODS",
                "STREETS", "RESIDENCES", "FORM_CATEGORIES", "SICK_PATIENT_CATEGORIES", "DISEASE_PLACES",
                "POPULATION_TYPES", "GRAPH1", "MANUAL_REPORT", "FORM1", "FORM12", "MAP_GRAPHIC"
        );

        // 1. Create permissions if not exist
        for (String subject : allSubjects) {
            permissionRepository.findBySubject(subject).orElseGet(() -> {
                List<String> actions = subject.equals("ALL")
                        ? List.of("manager", "manage", "create", "read", "view", "update", "delete")
                        : List.of("read");

                Permission permission = Permission.builder()
                        .subject(subject)
                        .actions(actions)
                        .descriptionUz("test")
                        .descriptionRu("test")
                        .descriptionUzCyril("test")
                        .descriptionKaa("test")
                        .active(true)
                        .build();

                log.info("➕ Permission created: {} (actions: {})", subject, actions);
                return permissionRepository.save(permission);
            });
        }

        Map<String, Map<String, Object>> rolePermissions = Map.of(
                "isemid_qr_user", Map.of(
                        "descriptionUz", "QR foydalanuvchi",
                        "descriptionUzCyril", "QR фойдаланувчи",
                        "descriptionRu", "Пользователь QR",
                        "descriptionKaa", "QR колдонучусу",
                        "permissions", List.of()
                ),
                "isemid_epidemiologist", Map.of(
                        "descriptionUz", "Epidemiolog",
                        "descriptionUzCyril", "Эпидемиолог",
                        "descriptionRu", "Эпидемиолог",
                        "descriptionKaa", "Эпидемиолог",
                        "permissions", List.of("ALL")
                ),
                "isemid_head_doctor", Map.of(
                        "descriptionUz", "Bosh shifokor",
                        "descriptionUzCyril", "Бош шифокор",
                        "descriptionRu", "Главный врач",
                        "descriptionKaa", "Башкы врач",
                        "permissions", List.of()
                ),
                "isemid_epidim_head", Map.of(
                        "descriptionUz", "Epidemiologiya va immunoprofilaktika bo‘limi mudiri",
                        "descriptionUzCyril", "Эпидемиология ва иммунопрофилактика бўлими мудири",
                        "descriptionRu", "Заведующий отделом эпидемиологии и иммунопрофилактики",
                        "descriptionKaa", "Эпидемиология жана иммунопрофилактика бөлүмүнүн мудири",
                        "permissions", List.of()
                ),
                "isemid_general_practitioner", Map.of(
                        "descriptionUz", "Umumiy amaliyot shifokori",
                        "descriptionUzCyril", "Умумий амалийот шифокори",
                        "descriptionRu", "Врач общей практики",
                        "descriptionKaa", "Жалпы практика дарыгери",
                        "permissions", List.of()
                ),
                "isemid_assistant_epidemiologist", Map.of(
                        "descriptionUz", "Epidemiolog yordamchisi",
                        "descriptionUzCyril", "Эпидемиолог ёрдамчиси",
                        "descriptionRu", "Помощник эпидемиолога",
                        "descriptionKaa", "Эпидемиолог жардамчысы",
                        "permissions", List.of()
                ),
                "isemid_labarant", Map.of(
                        "descriptionUz", "Laborant",
                        "descriptionUzCyril", "Лаборант",
                        "descriptionRu", "Лаборант",
                        "descriptionKaa", "Лаборант",
                        "permissions", List.of()
                ),
                "isemid_super_admin", Map.of(
                        "descriptionUz", "Super admin",
                        "descriptionUzCyril", "Супер админ",
                        "descriptionRu", "Супер администратор",
                        "descriptionKaa", "Супер администратор",
                        "permissions", List.of("ALL")
                ),
                "isemid_admin", Map.of(
                        "descriptionUz", "Admin",
                        "descriptionUzCyril", "Админ",
                        "descriptionRu", "Администратор",
                        "descriptionKaa", "Администратор",
                        "permissions", List.of("ALL")
                )
        );


        for (Map.Entry<String, Map<String, Object>> entry : rolePermissions.entrySet()) {
            String roleName = entry.getKey();
            Map<String, Object> data = entry.getValue();

            String descriptionUz = (String) data.get("descriptionUz");
            String descriptionUzCyril = (String) data.get("descriptionUzCyril");
            String descriptionRu = (String) data.get("descriptionRu");
            String descriptionKaa = (String) data.get("descriptionKaa");
            List<String> permissionSubjects = (List<String>) data.getOrDefault("permissions", List.of());

            Set<Permission> permissions;

            if (permissionSubjects.contains("ALL")) {
                permissions = new HashSet<>(permissionRepository.findAll());
            } else {
                permissions = permissionSubjects.stream()
                        .map(subject -> permissionRepository.findBySubject(subject)
                                .orElseThrow(() -> new RuntimeException("Permission not found for subject: " + subject)))
                        .collect(Collectors.toSet());
            }

            Role role = roleRepository.findByName(roleName).orElseGet(() ->
                    roleRepository.save(Role.builder()
                            .name(roleName)
                            .isActive(true)
                            .permissions(new HashSet<>()) // default bo‘sh set
                            .build())
            );

            // Null bo‘lmasligi uchun eski permission set tekshiriladi
            Set<Permission> existingPermissions = role.getPermissions() == null ? new HashSet<>() : role.getPermissions();

            boolean updated = false;

            if (!existingPermissions.equals(permissions)) {
                role.setPermissions(permissions);
                updated = true;
            }

            if (!Objects.equals(role.getDescriptionUz(), descriptionUz)) {
                role.setDescriptionUz(descriptionUz);
                updated = true;
            }
            if (!Objects.equals(role.getDescriptionUzCyril(), descriptionUzCyril)) {
                role.setDescriptionUzCyril(descriptionUzCyril);
                updated = true;
            }
            if (!Objects.equals(role.getDescriptionRu(), descriptionRu)) {
                role.setDescriptionRu(descriptionRu);
                updated = true;
            }
            if (!Objects.equals(role.getDescriptionKaa(), descriptionKaa)) {
                role.setDescriptionKaa(descriptionKaa);
                updated = true;
            }

            if (updated) {
                roleRepository.save(role);
                log.info("🔄 Role updated: {}", roleName);
            } else {
                log.info("✅ Role exists and unchanged: {}", roleName);
            }
        }

        log.info("✅ Default roles and permissions initialized successfully.");
    }
}
