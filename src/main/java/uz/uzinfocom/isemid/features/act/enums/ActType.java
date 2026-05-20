package uz.uzinfocom.isemid.features.act.enums;

import lombok.Getter;

@Getter
public enum ActType {
    ACT153,
    ACT154,
    ACT155,
    ACT156,
    ACT223,
    ACT224;

    public static LisResearchCode mapToLisResearchCode(ActType actType) {
        return switch (actType) {
            case ACT153 -> LisResearchCode.WATER;
            case ACT154 -> LisResearchCode.FOOD;
            case ACT223 -> LisResearchCode.SOIL;
            default -> throw new IllegalArgumentException("Unknown actType: " + actType);
        };
    }
}

