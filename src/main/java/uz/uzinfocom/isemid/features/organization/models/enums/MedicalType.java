package uz.uzinfocom.isemid.features.organization.models.enums;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;

@Getter
public enum MedicalType {
    PHARMACY("1", "Pharmacy", false),
    SOCIAL_PHARMACY("2", "Social Pharmacy", false),
    HOSPITAL("3", "Hospital", true),
    PEDIATRIC_HOSPITAL("4", "Pediatric Hospital", true),
    PEDIATRIC_MULTIDISCIPLINARY_HOSPITAL("5", "Pediatric Multidisciplinary Hospital", false),
    PSYCHONEUROLOGICAL_HOSPITAL("6", "Psychoneurological Hospital", true),
    EMERGENCY_HOSPITAL("7", "Emergency Hospital", true),
    SPECIALIZED_HOSPITAL("8", "Specialized Hospital", true),
    DISINFECTION_STATION("9", "Disinfection station", false),
    DISPENSARY("10", "Dispensary", true),
    NURSING_HOME("11", "Nursing home", false),
    ADVANCED_TRAINING_INSTITUTE("12", "Institute of Advanced Training", false),
    EDUCATIONAL_CLINIC("13", "Clinic at an educational institution", false),
    MATERNITY_COMPLEX("14", "Maternity complex", true),
    LEPROSARIUM("15", "Leprosarium", false),
    MEDICAL_ASSOCIATION("16", "Medical Association", false),
    SCIENTIFIC_PRODUCTION_ASSOCIATION("17", "Scientific and Production Association", false),
    BOARDING_HOUSE("18", "Boarding house", false),
    PATHOANATOMIC_SERVICE("19", "Pathoanatomic Service", false),
    MULTIDISCIPLINARY_POLYCLINIC("20", "Multidisciplinary polyclinic", true),
    FAMILY_POLYCLINIC("21", "Family polyclinic", true),
    DENTAL_POLYCLINIC("22", "Dental polyclinic", true),
    FAMILY_PHYSICIAN_POINT("23", "Family physicians point", true),
    SANATORIUM("24", "Sanatorium", true),
    BLOOD_TRANSFUSION_STATION("25", "Blood Transfusion station", false),
    AMBULANCE_STATION("26", "Ambulance station", true),
    HEALTH_DEPARTMENT("27", "Health Department", false),
    EDUCATIONAL_INSTITUTION("28", "Educational institution", false),
    BLOOD_TRANSFUSION_FACILITY("29", "Blood transfusion facility", true),
    SANITARY_EDUCATIONAL_INSTITUTION("30", "Sanitary and educational institution", false),
    SANEPID_SERVICE("31", "Establishment of sanitary and epidemiological service", false),
    HEMOSTASIOLOGY_CENTER("32", "Center of Hemostasiology", false),
    DIAGNOSTIC_CENTER("33", "Diagnostic Center", true),
    PEDIATRIC_DIAGNOSTIC_CENTER("34", "Pediatric diagnostic center", false),
    SCIENTIFIC_PRACTICAL_CENTER("35", "Scientific and Practical Center", true),
    BLOOD_TRANSFUSION_CENTER("36", "Blood Transfusion Center", false),
    PERINATAL_CENTER("37", "Perinatal Center", true),
    REHABILITATION_CENTER("38", "Rehabilitation center", true),
    REPRODUCTIVE_HEALTH_CENTER("39", "Reproductive Health Center", false),
    SCREENING_CENTER("40", "Screening Center", true),
    AIDS_CENTER("41", "AIDS Center", false),
    CENTRAL_MULTIDISCIPLINARY_POLYCLINIC("42", "Central multidisciplinary polyclinic", true),
    CENTRAL_MULTIDISCIPLINARY_POLYCLINIC_BRANCH("43", "Central multidisciplinary polyclinic (branch)", false),
    FAMILY_POLYCLINIC_BRANCH("44", "Family polyclinic (branch)", false),
    FAMILY_PHYSICIAN_POINT_BRANCH("45", "Family physicians point (branch)", false);

    private final String code;
    private final String display;
    private final boolean isHealthcareFacility;

    private static final Map<String, MedicalType> CODE_MAP = new HashMap<>();
    private static final Set<String> healthcareFacilities = new HashSet<>();
    static {
        for (MedicalType type : values()) {
            CODE_MAP.put(type.code, type);
            if (type.isHealthcareFacility) {
                healthcareFacilities.add(type.name());
            }
        }
    }

    MedicalType(String code, String display, boolean isHealthcareFacility) {
        this.code = code;
        this.display = display;
        this.isHealthcareFacility = isHealthcareFacility;
    }

    public static MedicalType fromCode(String code) {
        MedicalType type = CODE_MAP.get(code);
        if (type == null) {
            throw new IllegalArgumentException("Unknown MedicalType code: " + code);
        }
        return type;
    }

    /**
     * determines if given object of enum is healthcare facility
     * @return true for healthcare facility / false for NOT healthcare facility
     *
     */
    public boolean isHealthcareFacility() {
        return healthcareFacilities.contains(name());
    }
}
