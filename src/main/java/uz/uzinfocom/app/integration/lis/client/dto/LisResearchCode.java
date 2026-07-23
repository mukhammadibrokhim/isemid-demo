package uz.uzinfocom.app.integration.lis.client.dto;

import uz.uzinfocom.app.integration.lis.common.exception.LisUnsupportedActTypeException;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;

/**
 * The laboratory research families LIS runs, and the mapping from our act
 * types onto them.
 *
 * <p>Only the three sample-collection acts have a LIS counterpart. ACT155
 * (pesticide residues), ACT156 (environmental swabs) and ACT224 (sanitary
 * inspection) are inspection documents that never produce a LIS submission —
 * asking for one is a caller error, not a missing mapping.
 */
public enum LisResearchCode {
    WATER,
    FOOD,
    SOIL;

    public static LisResearchCode of(ActType actType) {
        return switch (actType) {
            case ACT153 -> WATER;
            case ACT154 -> FOOD;
            case ACT223 -> SOIL;
            case ACT155, ACT156, ACT224 -> throw new LisUnsupportedActTypeException(actType);
        };
    }

    /**
     * Whether this act type can be sent to LIS at all — lets callers check
     * without provoking an exception.
     */
    public static boolean isSupported(ActType actType) {
        return actType == ActType.ACT153 || actType == ActType.ACT154 || actType == ActType.ACT223;
    }
}
