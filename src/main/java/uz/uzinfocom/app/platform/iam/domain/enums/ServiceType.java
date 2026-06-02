package uz.uzinfocom.app.platform.iam.domain.enums;

import lombok.Getter;

@Getter
public enum ServiceType {
    OUTPATIENT("1", "Outpatient"),
    INPATIENT("2", "Inpatient"),
    EMERGENCY("3", "Emergency medicine"),
    COMBINED("4", "Combined"),
    DIAGNOSTIC("5", "Diagnostic"),
    OTHER_S("6", "Other");

    private final String code;
    private final String displayName;

    ServiceType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static ServiceType fromCode(String code) {
        for (ServiceType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
