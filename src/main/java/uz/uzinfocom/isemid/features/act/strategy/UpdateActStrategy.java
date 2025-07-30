package uz.uzinfocom.isemid.features.act.strategy;

import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;

public interface UpdateActStrategy {
    ApiResponse<?> updateAct(long id, ActRequest reqAct);
}
