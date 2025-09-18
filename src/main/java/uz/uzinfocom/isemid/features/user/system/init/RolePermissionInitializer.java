package uz.uzinfocom.isemid.features.user.system.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.commons.seed.ModuleDataInitializer;
import uz.uzinfocom.isemid.features.user.models.Permission;
import uz.uzinfocom.isemid.features.user.models.Role;
import uz.uzinfocom.isemid.features.user.reposiotries.PermissionRepository;
import uz.uzinfocom.isemid.features.user.reposiotries.RoleRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
//@ConditionalOnProperty(name = "runner.role_permission.init", havingValue = "true")
public class RolePermissionInitializer implements ModuleDataInitializer {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public void init() {
        log.info("♻️ Initializing default roles and permissions...");

//        List<String> allSubjects = List.of(
//                "ALL", "EMPLOYEES", "ORGANIZATIONS", "ROLES", "PERMISSIONS", "FORM058",
//                "FORM058_ATTACHED_CARDS", "FORM058_CHANGE_ORGANIZATION", "RECEIVED", "FORM058_COMPLETE_BY_SUPERVISOR",
//                "USER_INCOMINGS", "CARD_FULL_INFO", "MKB_LIST", "COUNTRIES", "STATES", "CITIES", "NEIGHBORHOODS",
//                "STREETS", "RESIDENCES", "FORM_CATEGORIES", "SICK_PATIENT_CATEGORIES", "DISEASE_PLACES",
//                "POPULATION_TYPES", "GRAPH1", "MANUAL_REPORT", "FORM1", "FORM12", "MAP_GRAPHIC"
//        );
//
//        Map<String, Map<String, String>> subjectDescriptions = Map.ofEntries(
//                Map.entry("ALL", Map.of("uz", "Barcha ruxsatlar", "ru", "Все разрешения", "uzCyril", "Барча рухсатлар", "kaa", "Bardıq ruqsatlar")),
//                Map.entry("EMPLOYEES", Map.of("uz", "Xodimlar", "ru", "Сотрудники", "uzCyril", "Ходимлар", "kaa", "Qızmetkerler")),
//                Map.entry("ORGANIZATIONS", Map.of("uz", "Tashkilotlar", "ru", "Организации", "uzCyril", "Ташкилотлар", "kaa", "Uyımlar")),
//                Map.entry("ROLES", Map.of("uz", "Rollar", "ru", "Роли", "uzCyril", "Роллар", "kaa", "Rollar")),
//                Map.entry("PERMISSIONS", Map.of("uz", "Ruxsatlar", "ru", "Разрешения", "uzCyril", "Рухсатлар", "kaa", "Ruqsatlar")),
//                Map.entry("FORM058", Map.of("uz", "Ekstren xabarnoma", "ru", "Экстренное извещение", "uzCyril", "Экстрен хабарнома", "kaa", "Täsirli xabarnama")),
//                Map.entry("FORM058_ATTACHED_CARDS", Map.of("uz", "Form058ga biriktirilgan kartalar", "ru", "Карты, прикрепленные к Form058", "uzCyril", "Форм058га бириктирилган карталар", "kaa", "Form058-ge baylanısqan kartalar")),
//                Map.entry("FORM058_CHANGE_ORGANIZATION", Map.of("uz", "Form058 organizatsiyasini o‘zgartirish", "ru", "Изменение организации Form058", "uzCyril", "Форм058 ташкилотини ўзгартириш", "kaa", "Form058 uyımın özgertiw")),
//                Map.entry("RECEIVED", Map.of("uz", "Qabul qilingan formalar", "ru", "Полученные формы", "uzCyril", "Қабул қилинган формалар", "kaa", "Qabıl etilgen formalár")),
//                Map.entry("FORM058_COMPLETE_BY_SUPERVISOR", Map.of("uz", "Form058ni rahbar tomonidan tasdiqlash", "ru", "Подтверждение Form058 руководителем", "uzCyril", "Форм058ни раҳбар томонидан тасдиқлаш", "kaa", "Form058 baslıq tastıyıqlawı arqalı")),
//                Map.entry("USER_INCOMINGS", Map.of("uz", "Menga biriktirilgan kartalar", "ru", "Карты, прикрепленные ко мне", "uzCyril", "Менга бириктирилган карталар", "kaa", "Maǵan baylanısqan kartalar")),
//                Map.entry("CARD_FULL_INFO", Map.of("uz", "Kartaning to‘liq ma’lumoti", "ru", "Дополнительная информация о карте", "uzCyril", "Картанинг тўлиқ маълумоти", "kaa", "Kartanın tolıq malıumatı")),
//                Map.entry("MKB_LIST", Map.of("uz", "MKBlаr ro‘yxati", "ru", "Список МКБ", "uzCyril", "МКБлар рўйхати", "kaa", "MKB-lar tizimi")),
//                Map.entry("COUNTRIES", Map.of("uz", "Davlatlar", "ru", "Страны", "uzCyril", "Давлатлар", "kaa", "Memleketler")),
//                Map.entry("STATES", Map.of("uz", "Viloyatlar", "ru", "Области", "uzCyril", "Вилоятлар", "kaa", "Oblıstar")),
//                Map.entry("CITIES", Map.of("uz", "Shaharlar", "ru", "Города", "uzCyril", "Шаҳарлар", "kaa", "Qalalar")),
//                Map.entry("NEIGHBORHOODS", Map.of("uz", "Tumanlar", "ru", "Районы", "uzCyril", "Туманлар", "kaa", "Awdanlar")),
//                Map.entry("STREETS", Map.of("uz", "Ko‘chalar", "ru", "Улицы", "uzCyril", "Кўчалар", "kaa", "Kósheler")),
//                Map.entry("RESIDENCES", Map.of("uz", "Yashash joyi", "ru", "Место жительства", "uzCyril", "Яшаш жойи", "kaa", "Turaq jayi")),
//                Map.entry("FORM_CATEGORIES", Map.of("uz", "Yagona forma", "ru", "Единая форма", "uzCyril", "Ягона форма", "kaa", "Birgeshe forma")),
//                Map.entry("SICK_PATIENT_CATEGORIES", Map.of("uz", "Kasallangan bemor toifasi", "ru", "Категория больного пациента", "uzCyril", "Касалланган бемор тоифаси", "kaa", "Aǵırǵan naǵras toifası")),
//                Map.entry("DISEASE_PLACES", Map.of("uz", "Kasallik joylari", "ru", "Места расположения больного", "uzCyril", "Касаллик жойлари", "kaa", "Aǵırǵan orınlar")),
//                Map.entry("POPULATION_TYPES", Map.of("uz", "Aholi turlari", "ru", "Тип населения", "uzCyril", "Аҳоли турлари", "kaa", "Xalıqtıń túrleri")),
//                Map.entry("GRAPH1", Map.of("uz", "1-Grafik", "ru", "Первый график", "uzCyril", "1-График", "kaa", "1-Grafik")),
//                Map.entry("MANUAL_REPORT", Map.of("uz", "Qo‘llanma hisoboti", "ru", "Отчет по руководствам", "uzCyril", "Қўлланма ҳисоботи", "kaa", "Qollanba esabatı")),
//                Map.entry("FORM1", Map.of("uz", "1-shakl", "ru", "Форма №1", "uzCyril", "1-шакл", "kaa", "1-shakil")),
//                Map.entry("FORM12", Map.of("uz", "12-shakl", "ru", "Форма №12", "uzCyril", "12-шакл", "kaa", "12-shakil")),
//                Map.entry("MAP_GRAPHIC", Map.of("uz", "Xarita grafikasi", "ru", "Тематический график", "uzCyril", "Харита графикаси", "kaa", "Harita grafikası"))
//        );
//
//
//        for (String subject : allSubjects) {
//            List<String> actions = subject.equals("ALL")
//                    ? List.of("manager", "manage", "create", "read", "view", "update", "delete")
//                    : List.of("read");
//
//            Map<String, String> desc = subjectDescriptions.getOrDefault(subject, Map.of());
//
//            Permission permission = permissionRepository.findBySubject(subject).orElse(null);
//
//            if (permission == null) {
//                permission = Permission.builder()
//                        .subject(subject)
//                        .actions(actions)
//                        .descriptionUz(desc.getOrDefault("uz", subject))
//                        .descriptionRu(desc.getOrDefault("ru", subject))
//                        .descriptionUzCyril(desc.getOrDefault("uzCyril", subject))
//                        .descriptionKaa(desc.getOrDefault("kaa", subject))
//                        .active(true)
//                        .build();
//
//                permissionRepository.save(permission);
//                log.info("➕ Permission created: {} (actions: {})", subject, actions);
//            } else {
//                boolean updated = false;
//
//                if (!permission.getActions().containsAll(actions)) {
//                    permission.setActions(actions);
//                    updated = true;
//                }
//
//                if (!Objects.equals(permission.getDescriptionUz(), desc.getOrDefault("uz", subject))) {
//                    permission.setDescriptionUz(desc.getOrDefault("uz", subject));
//                    updated = true;
//                }
//                if (!Objects.equals(permission.getDescriptionRu(), desc.getOrDefault("ru", subject))) {
//                    permission.setDescriptionRu(desc.getOrDefault("ru", subject));
//                    updated = true;
//                }
//                if (!Objects.equals(permission.getDescriptionUzCyril(), desc.getOrDefault("uzCyril", subject))) {
//                    permission.setDescriptionUzCyril(desc.getOrDefault("uzCyril", subject));
//                    updated = true;
//                }
//                if (!Objects.equals(permission.getDescriptionKaa(), desc.getOrDefault("kaa", subject))) {
//                    permission.setDescriptionKaa(desc.getOrDefault("kaa", subject));
//                    updated = true;
//                }
//
//                if (updated) {
//                    permissionRepository.save(permission);
//                    log.info("🔄 Permission updated: {}", subject);
//                } else {
//                    log.info("✅ Permission unchanged: {}", subject);
//                }
//            }
//        }
//
//        Map<String, Map<String, Object>> rolePermissions = Map.of(
//                "isemid_qr_user", Map.of(
//                        "descriptionUz", "QR foydalanuvchi",
//                        "descriptionUzCyril", "QR фойдаланувчи",
//                        "descriptionRu", "Пользователь QR",
//                        "descriptionKaa", "QR колдонучусу",
//                        "permissions", List.of()
//                ),
//                "isemid_epidemiologist", Map.of(
//                        "descriptionUz", "Epidemiolog",
//                        "descriptionUzCyril", "Эпидемиолог",
//                        "descriptionRu", "Эпидемиолог",
//                        "descriptionKaa", "Эпидемиолог",
//                        "permissions", List.of("ALL")
//                ),
//                "isemid_head_doctor", Map.of(
//                        "descriptionUz", "Bosh shifokor",
//                        "descriptionUzCyril", "Бош шифокор",
//                        "descriptionRu", "Главный врач",
//                        "descriptionKaa", "Башкы врач",
//                        "permissions", List.of()
//                ),
//                "isemid_epidim_head", Map.of(
//                        "descriptionUz", "Epidemiologiya va immunoprofilaktika bo‘limi mudiri",
//                        "descriptionUzCyril", "Эпидемиология ва иммунопрофилактика бўлими мудири",
//                        "descriptionRu", "Заведующий отделом эпидемиологии и иммунопрофилактики",
//                        "descriptionKaa", "Эпидемиология жана иммунопрофилактика бөлүмүнүн мудири",
//                        "permissions", List.of()
//                ),
//                "isemid_general_practitioner", Map.of(
//                        "descriptionUz", "Umumiy amaliyot shifokori",
//                        "descriptionUzCyril", "Умумий амалийот шифокори",
//                        "descriptionRu", "Врач общей практики",
//                        "descriptionKaa", "Жалпы практика дарыгери",
//                        "permissions", List.of()
//                ),
//                "isemid_assistant_epidemiologist", Map.of(
//                        "descriptionUz", "Epidemiolog yordamchisi",
//                        "descriptionUzCyril", "Эпидемиолог ёрдамчиси",
//                        "descriptionRu", "Помощник эпидемиолога",
//                        "descriptionKaa", "Эпидемиолог жардамчысы",
//                        "permissions", List.of()
//                ),
//                "isemid_labarant", Map.of(
//                        "descriptionUz", "Laborant",
//                        "descriptionUzCyril", "Лаборант",
//                        "descriptionRu", "Лаборант",
//                        "descriptionKaa", "Лаборант",
//                        "permissions", List.of()
//                ),
//                "isemid_super_admin", Map.of(
//                        "descriptionUz", "Super admin",
//                        "descriptionUzCyril", "Супер админ",
//                        "descriptionRu", "Супер администратор",
//                        "descriptionKaa", "Супер администратор",
//                        "permissions", List.of("ALL")
//                ),
//                "isemid_admin", Map.of(
//                        "descriptionUz", "Admin",
//                        "descriptionUzCyril", "Админ",
//                        "descriptionRu", "Администратор",
//                        "descriptionKaa", "Администратор",
//                        "permissions", List.of("ALL")
//                )
//        );
//
//
//        for (Map.Entry<String, Map<String, Object>> entry : rolePermissions.entrySet()) {
//            String roleName = entry.getKey();
//            Map<String, Object> data = entry.getValue();
//
//            String descriptionUz = (String) data.get("descriptionUz");
//            String descriptionUzCyril = (String) data.get("descriptionUzCyril");
//            String descriptionRu = (String) data.get("descriptionRu");
//            String descriptionKaa = (String) data.get("descriptionKaa");
//            List<String> permissionSubjects = (List<String>) data.getOrDefault("permissions", List.of());
//
//            Set<Permission> permissions;
//
//            if (permissionSubjects.contains("ALL")) {
//                permissions = new HashSet<>(permissionRepository.findAll());
//            } else {
//                permissions = permissionSubjects.stream()
//                        .map(subject -> permissionRepository.findBySubject(subject)
//                                .orElseThrow(() -> new RuntimeException("Permission not found for subject: " + subject)))
//                        .collect(Collectors.toSet());
//            }
//
//            Role role = roleRepository.findByName(roleName).orElseGet(() ->
//                    roleRepository.save(Role.builder()
//                            .name(roleName)
//                            .isActive(true)
//                            .permissions(new HashSet<>()) // default bo‘sh set
//                            .build())
//            );
//
//            // Null bo‘lmasligi uchun eski permission set tekshiriladi
//            Set<Permission> existingPermissions = role.getPermissions() == null ? new HashSet<>() : role.getPermissions();
//
//            boolean updated = false;
//
//            if (!existingPermissions.equals(permissions)) {
//                role.setPermissions(permissions);
//                updated = true;
//            }
//
//            if (!Objects.equals(role.getDescriptionUz(), descriptionUz)) {
//                role.setDescriptionUz(descriptionUz);
//                updated = true;
//            }
//            if (!Objects.equals(role.getDescriptionUzCyril(), descriptionUzCyril)) {
//                role.setDescriptionUzCyril(descriptionUzCyril);
//                updated = true;
//            }
//            if (!Objects.equals(role.getDescriptionRu(), descriptionRu)) {
//                role.setDescriptionRu(descriptionRu);
//                updated = true;
//            }
//            if (!Objects.equals(role.getDescriptionKaa(), descriptionKaa)) {
//                role.setDescriptionKaa(descriptionKaa);
//                updated = true;
//            }
//
//            if (updated) {
//                roleRepository.save(role);
//                log.info("🔄 Role updated: {}", roleName);
//            } else {
//                log.info("✅ Role exists and unchanged: {}", roleName);
//            }
//        }
//
        log.info("✅ Default roles and permissions initialized successfully.");
    }
}
