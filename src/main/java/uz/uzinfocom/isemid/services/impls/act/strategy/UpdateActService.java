package uz.uzinfocom.isemid.services.impls.act.strategy;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.enums.ActType;
import uz.uzinfocom.isemid.models.payload.requests.act.ReqAct;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.services.act.strategy.UpdateActStrategy;

import java.util.Map;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateActService {
    final Map<ActType, UpdateActStrategy> updateCardStrategies;

    public ApiResponse<?> updateAct(long id, ReqAct reqAct, User currentUser) {
        ActType actType = reqAct.getActType();

        UpdateActStrategy strategy = updateCardStrategies.get(actType);
        if (strategy == null) {
            throw new ApiException(BAD_REQUEST, "Unsupported act type: " + actType);
        }
        return strategy.updateAct(id, reqAct, currentUser);
    }
}
