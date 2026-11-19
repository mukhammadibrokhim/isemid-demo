package uz.uzinfocom.app.modules.iam.application.sync.mapper;

import uz.uzinfocom.app.modules.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.modules.iam.domain.enums.OrganizationLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    private static final Map<String, MedicalType> MEDICAL_TYPE_BY_STRUCTURE = buildMedicalTypes();

    private DhpOrganizationTypeMapping() {
    }

    static Optional<OrganizationLevel> level(String subordinationGroupCode) {
        return Optional.ofNullable(LEVEL_BY_SUBORDINATION_GROUP.get(subordinationGroupCode));
    }

    static Optional<MedicalType> medicalType(String organizationalStructureCode) {
        return Optional.ofNullable(MEDICAL_TYPE_BY_STRUCTURE.get(organizationalStructureCode));
    }

    private static Map<String, MedicalType> buildMedicalTypes() {
        Map<String, MedicalType> map = new HashMap<>();

        put(map, MedicalType.SCIENTIFIC_PRACTICAL_CENTER, "110", "112");
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

        put(map, MedicalType.HEALTH_DEPARTMENT, "911");
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
