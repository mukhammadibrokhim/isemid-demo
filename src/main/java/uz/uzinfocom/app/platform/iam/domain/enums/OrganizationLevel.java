package uz.uzinfocom.app.platform.iam.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrganizationLevel {
    REPUBLICAN("1", "Republican"),
    REGIONAL("2", "Regional"),
    URBAN("3", "Urban"),
    AREA("4", "Area"),
    DISTRICT("5", "District"),
    INTERDISTRICT("6", "Interdistrict"),
    NOT_DEFINED("9", "Not defined");

    private final String code;
    private final String display;

    public static OrganizationLevel fromCode(String code) {
        for (OrganizationLevel type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}
