package uz.uzinfocom.isemid.features.act.strategy;


import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.user.models.User;

public interface UpdateActStrategy {
    ApiResponse<?> updateAct(long id, ActRequest reqAct, User currentUser);
}
