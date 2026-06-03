package uz.uzinfocom.isemid.features.organization.models.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LevelType {
    REPUBLICAN("1", "Republican"),
    REGIONAL("2", "Regional"),
    URBAN("3", "Urban"),
    AREA("4", "Area"),
    DISTRICT("5", "District"),
    INTERDISTRICT("6", "Interdistrict"),
    NOT_DEFINED("9", "Not defined");

    private final String code;
    private final String display;

    public static LevelType fromCode(String code) {
        for (LevelType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }

    /**
     * The code level returning in the method below does not correspond
     * to the ones specified via constructor above.
     * Business requirement is unclear
     *
     * @return numeric representation of this LevelType
     */
    public int numericForm() {
        return switch (this) {
            case REPUBLICAN -> 1;
            case REGIONAL, URBAN -> 2;
            case AREA, DISTRICT, INTERDISTRICT, NOT_DEFINED -> 3;
        };
    }

}
