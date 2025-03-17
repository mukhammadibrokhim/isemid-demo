package uz.uzinfocom.isemid.services.act;

import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.payload.requests.act.ReqAct;
import uz.uzinfocom.isemid.models.payload.requests.act.ReqCardAssignAct;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;

public interface ActService {

   ApiResponse<?> getById(long id);

    ApiResponse<?> updateAct(long id, ReqAct reqAct, User currentUser);

    ApiResponse<?> deleteById(Long id);
}
