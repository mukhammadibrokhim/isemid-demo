package uz.uzinfocom.isemid.features.act.utils;

import uz.uzinfocom.isemid.features.act.models.act153.Act153;
import uz.uzinfocom.isemid.features.act.models.act154.Act154;
import uz.uzinfocom.isemid.features.act.models.act155.Act155;
import uz.uzinfocom.isemid.features.act.models.act156.Act156;
import uz.uzinfocom.isemid.features.act.models.act223.Act223;
import uz.uzinfocom.isemid.features.act.models.act224.Act224;

public class ActBindingUtil {
    public static void bindChildEntities(Act153 act) {
        if (act.getAct153Details() != null && !act.getAct153Details().isEmpty()) {
            act.getAct153Details().forEach(detail -> detail.setAct153(act));
        }
    }

    public static void bindChildEntities(Act154 act) {
        if (act.getAct154Details() != null && !act.getAct154Details().isEmpty()) {
            act.getAct154Details().forEach(detail -> detail.setAct154(act));
        }
    }

    public static void bindChildEntities(Act155 act) {
        if (act.getAct155Details() != null && !act.getAct155Details().isEmpty()) {
            act.getAct155Details().forEach(detail -> detail.setAct155(act));
        }
    }

    public static void bindChildEntities(Act156 act) {
        if (act.getAct156KitchenUtensils() != null && !act.getAct156KitchenUtensils().isEmpty()) {
            act.getAct156KitchenUtensils().forEach(detail -> detail.setAct156(act));
        }
        if (act.getAct156GroupDetails() != null && !act.getAct156GroupDetails().isEmpty()) {
            act.getAct156GroupDetails().forEach(detail -> detail.setAct156(act));
        }
    }

    public static void bindChildEntities(Act223 act) {
        if (act.getAct223Details() != null && !act.getAct223Details().isEmpty()) {
            act.getAct223Details().forEach(detail -> detail.setAct223(act));
        }
    }

    public static void bindChildEntities(Act224 act) {
        if (act.getRecommendedActions() != null) {
            act.getRecommendedActions().forEach(child -> child.setAct224(act));
        }
    }
}
