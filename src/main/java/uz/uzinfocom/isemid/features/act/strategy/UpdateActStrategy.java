package uz.uzinfocom.isemid.features.act.strategy;

import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.models.Act;

public interface UpdateActStrategy {
    Act updateAct(long id, ActRequest reqAct);
}
