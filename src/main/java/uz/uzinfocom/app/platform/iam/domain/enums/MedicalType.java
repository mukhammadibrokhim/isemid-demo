package uz.uzinfocom.app.platform.iam.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum MedicalType {
    PHARMACY("1", "Pharmacy"),
    SOCIAL_PHARMACY("2", "Social Pharmacy"),
    HOSPITAL("3", "Hospital"),
    PEDIATRIC_HOSPITAL("4", "Pediatric Hospital"),
    PEDIATRIC_MULTIDISCIPLINARY_HOSPITAL("5", "Pediatric Multidisciplinary Hospital"),
    PSYCHONEUROLOGICAL_HOSPITAL("6", "Psychoneurological Hospital"),
    EMERGENCY_HOSPITAL("7", "Emergency Hospital"),
    SPECIALIZED_HOSPITAL("8", "Specialized Hospital"),
    DISINFECTION_STATION("9", "Disinfection station"),
    DISPENSARY("10", "Dispensary"),
    NURSING_HOME("11", "Nursing home"),
    ADVANCED_TRAINING_INSTITUTE("12", "Institute of Advanced Training"),
    EDUCATIONAL_CLINIC("13", "Clinic at an educational institution"),
    MATERNITY_COMPLEX("14", "Maternity complex"),
    LEPROSARIUM("15", "Leprosarium"),
    MEDICAL_ASSOCIATION("16", "Medical Association"),
    SCIENTIFIC_PRODUCTION_ASSOCIATION("17", "Scientific and Production Association"),
    BOARDING_HOUSE("18", "Boarding house"),
    PATHOANATOMIC_SERVICE("19", "Pathoanatomic Service"),
    MULTIDISCIPLINARY_POLYCLINIC("20", "Multidisciplinary polyclinic"),
    FAMILY_POLYCLINIC("21", "Family polyclinic"),
    DENTAL_POLYCLINIC("22", "Dental polyclinic"),
    FAMILY_PHYSICIAN_POINT("23", "Family physicians point"),
    SANATORIUM("24", "Sanatorium"),
    BLOOD_TRANSFUSION_STATION("25", "Blood Transfusion station"),
    AMBULANCE_STATION("26", "Ambulance station"),
    HEALTH_DEPARTMENT("27", "Health Department"),
    EDUCATIONAL_INSTITUTION("28", "Educational institution"),
    BLOOD_TRANSFUSION_FACILITY("29", "Blood transfusion facility"),
    SANITARY_EDUCATIONAL_INSTITUTION("30", "Sanitary and educational institution"),
    SANEPID_SERVICE("31", "Establishment of sanitary and epidemiological service"),
    HEMOSTASIOLOGY_CENTER("32", "Center of Hemostasiology"),
    DIAGNOSTIC_CENTER("33", "Diagnostic Center"),
    PEDIATRIC_DIAGNOSTIC_CENTER("34", "Pediatric diagnostic center"),
    SCIENTIFIC_PRACTICAL_CENTER("35", "Scientific and Practical Center"),
    BLOOD_TRANSFUSION_CENTER("36", "Blood Transfusion Center"),
    PERINATAL_CENTER("37", "Perinatal Center"),
    REHABILITATION_CENTER("38", "Rehabilitation center"),
    REPRODUCTIVE_HEALTH_CENTER("39", "Reproductive Health Center"),
    SCREENING_CENTER("40", "Screening Center"),
    AIDS_CENTER("41", "AIDS Center"),
    CENTRAL_MULTIDISCIPLINARY_POLYCLINIC("42", "Central multidisciplinary polyclinic"),
    CENTRAL_MULTIDISCIPLINARY_POLYCLINIC_BRANCH("43", "Central multidisciplinary polyclinic (branch)"),
    FAMILY_POLYCLINIC_BRANCH("44", "Family polyclinic (branch)"),
    FAMILY_PHYSICIAN_POINT_BRANCH("45", "Family physicians point (branch)"),

    /**
     * Fallback for organizations whose remote code is missing or not
     * (yet) known to this system. Never matched by {@link #fromCode},
     * only ever assigned explicitly by callers as a default.
     */
    OTHER(null, "Other / not specified");

    private final String code;
    private final String display;

    public static Optional<MedicalType> fromCode(String code) {
        return Arrays.stream(values())
                .filter(type -> type.code != null && type.code.equals(code))
                .findFirst();
    }
}
