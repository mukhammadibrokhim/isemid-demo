package uz.uzinfocom.app.modules.iam.application.sync.mapper;

import uz.uzinfocom.app.modules.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.modules.iam.domain.enums.OrganizationLevel;
import uz.uzinfocom.app.modules.iam.domain.enums.ServiceType;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Maps DHP's national organization classifiers onto this system's enums.
 *
 * <p>DHP's {@code organizational-subordination-group} (I_1..I_6) and
 * {@code organizational-structure} (75 codes) classifiers are richer than
 * {@link OrganizationLevel} / {@link MedicalType}, so the match is by meaning,
 * not by code, and several DHP codes collapse into one value. Anything not
 * listed here is deliberately left unmapped (the caller falls back to
 * NOT_DEFINED / OTHER); the raw DHP codes are persisted separately, so this
 * table can be refined later without re-syncing.
 */
final class DhpOrganizationTypeMapping {

    private static final Map<String, OrganizationLevel> LEVEL_BY_SUBORDINATION_GROUP = Map.of(
            "I_1", OrganizationLevel.REPUBLICAN,
            "I_2", OrganizationLevel.REGIONAL,
            "I_3", OrganizationLevel.DISTRICT,
            // Ownership / affiliation, not a territorial level.
            "I_4", OrganizationLevel.NOT_DEFINED,
            "I_5", OrganizationLevel.NOT_DEFINED,
            "I_6", OrganizationLevel.NOT_DEFINED
    );

    private static final Map<String, ServiceType> SERVICE_TYPE_BY_SERVICE_GROUP = Map.of(
            "III_100", ServiceType.COMBINED,      // outpatient + inpatient departments
            "III_200", ServiceType.INPATIENT,
            "III_300", ServiceType.INPATIENT,     // day inpatient department
            "III_400", ServiceType.COMBINED,      // outpatient + day inpatient departments
            "III_500", ServiceType.OUTPATIENT,
            "III_600", ServiceType.OTHER_S
    );

    /** Broad nomenclature groups that identify a type on their own (II_100/300/500/900 are too broad). */
    private static final Map<String, MedicalType> MEDICAL_TYPE_BY_NOMENCLATURE = Map.of(
            "II_200", MedicalType.AMBULANCE_STATION,
            "II_400", MedicalType.SANATORIUM,
            "II_600", MedicalType.PATHOANATOMIC_SERVICE,
            "II_700", MedicalType.BLOOD_TRANSFUSION_CENTER,
            "II_800", MedicalType.SANEPID_SERVICE
    );

    private static final Map<String, MedicalType> MEDICAL_TYPE_BY_STRUCTURE = buildMedicalTypes();

    private DhpOrganizationTypeMapping() {
    }

    static Optional<OrganizationLevel> level(String subordinationGroupCode) {
        return subordinationGroupCode == null
                ? Optional.empty()
                : Optional.ofNullable(LEVEL_BY_SUBORDINATION_GROUP.get(subordinationGroupCode));
    }

    /**
     * The specific {@code organizational-structure} code wins. When it is absent,
     * unmapped or only the generic "other" (999), the broader
     * {@code nomenclature-group} can still pin the type down, which is how
     * sanitary-epidemiological, blood-transfusion and pathology bodies are told
     * apart (the structure classifier has no entry for them).
     */
    static Optional<MedicalType> medicalType(String organizationalStructureCode, String nomenclatureGroupCode) {
        MedicalType byStructure = organizationalStructureCode == null
                ? null : MEDICAL_TYPE_BY_STRUCTURE.get(organizationalStructureCode);
        if (byStructure != null && byStructure != MedicalType.OTHER) {
            return Optional.of(byStructure);
        }

        MedicalType byNomenclature = nomenclatureGroupCode == null
                ? null : MEDICAL_TYPE_BY_NOMENCLATURE.get(nomenclatureGroupCode);
        if (byNomenclature != null) {
            return Optional.of(byNomenclature);
        }

        return Optional.ofNullable(byStructure);
    }

    /**
     * DHP {@code organizational-service-group} codes (III_100..III_600) plus the
     * emergency / diagnostic hints carried by the nomenclature group and the
     * structure code. Returns an empty set when nothing is recognised.
     */
    static Set<ServiceType> serviceTypes(
            Collection<String> serviceGroupCodes,
            String nomenclatureGroupCode,
            String organizationalStructureCode
    ) {
        Set<ServiceType> result = new LinkedHashSet<>();
        for (String code : serviceGroupCodes) {
            ServiceType type = code == null ? null : SERVICE_TYPE_BY_SERVICE_GROUP.get(code);
            if (type != null) {
                result.add(type);
            }
        }
        if ("II_200".equals(nomenclatureGroupCode)) {
            result.add(ServiceType.EMERGENCY);
        }
        if ("343".equals(organizationalStructureCode)) {
            result.add(ServiceType.DIAGNOSTIC);
        }
        return result;
    }

    private static Map<String, MedicalType> buildMedicalTypes() {
        Map<String, MedicalType> map = new HashMap<>();

        put(map, MedicalType.SCIENTIFIC_PRACTICAL_CENTER, "110", "112", "114", "128", "136");
        put(map, MedicalType.HOSPITAL, "116", "132", "138");
        put(map, MedicalType.PEDIATRIC_MULTIDISCIPLINARY_HOSPITAL, "134");
        put(map, MedicalType.EMERGENCY_HOSPITAL, "113", "126");
        put(map, MedicalType.REHABILITATION_CENTER, "118", "415");
        put(map, MedicalType.PERINATAL_CENTER, "120", "122");
        put(map, MedicalType.MATERNITY_COMPLEX, "124");
        put(map, MedicalType.EDUCATIONAL_CLINIC, "130", "325");
        put(map, MedicalType.SPECIALIZED_HOSPITAL, "142");
        put(map, MedicalType.LEPROSARIUM, "144");
        put(map, MedicalType.HOSPITAL, "146", "140", "158");
        put(map, MedicalType.MEDICAL_ASSOCIATION, "148");
        put(map, MedicalType.DISPENSARY, "150");
        put(map, MedicalType.PEDIATRIC_HOSPITAL, "152", "154");
        put(map, MedicalType.PEDIATRIC_MULTIDISCIPLINARY_HOSPITAL, "156");

        put(map, MedicalType.AMBULANCE_STATION, "211", "213", "215");

        put(map, MedicalType.MULTIDISCIPLINARY_POLYCLINIC,
                "311", "313", "315", "317", "319", "321", "323", "327", "329", "331", "333", "335", "337",
                "339", "345", "349");
        put(map, MedicalType.DENTAL_POLYCLINIC, "341", "353");
        put(map, MedicalType.DIAGNOSTIC_CENTER, "343");
        put(map, MedicalType.CENTRAL_MULTIDISCIPLINARY_POLYCLINIC, "347");
        put(map, MedicalType.FAMILY_POLYCLINIC, "351");
        put(map, MedicalType.FAMILY_PHYSICIAN_POINT, "355", "357");

        put(map, MedicalType.SANATORIUM, "411", "413", "417", "419", "421");

        put(map, MedicalType.HEALTH_DEPARTMENT, "911", "913");
        put(map, MedicalType.SANEPID_SERVICE, "935");
        put(map, MedicalType.EDUCATIONAL_INSTITUTION, "915", "919", "921", "923");
        put(map, MedicalType.ADVANCED_TRAINING_INSTITUTE, "917");
        put(map, MedicalType.DISINFECTION_STATION, "933");

        // 999 = "other organizations of the health system".
        put(map, MedicalType.OTHER, "999");

        return Map.copyOf(map);
    }

    private static void put(Map<String, MedicalType> map, MedicalType type, String... codes) {
        for (String code : codes) {
            map.put(code, type);
        }
    }
}
