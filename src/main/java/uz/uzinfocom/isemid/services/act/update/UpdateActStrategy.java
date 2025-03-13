package uz.uzinfocom.isemid.services.act.update;

import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.payload.requests.act.ReqAct;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;


public interface UpdateActStrategy {
    ApiResponse<?> updateAct(long id, ReqAct reqAct, User currentUser);
}
