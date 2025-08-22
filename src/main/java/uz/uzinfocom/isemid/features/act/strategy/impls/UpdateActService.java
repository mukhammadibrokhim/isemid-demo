package uz.uzinfocom.isemid.features.act.strategy.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.enums.ActType;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.strategy.UpdateActStrategy;

import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateActService {
    private final Map<ActType, UpdateActStrategy> updateCardStrategies;
    private final MessageUtil messageUtil;

    public Act updateAct(long id, ActRequest reqAct) {
        ActType actType = reqAct.getType();

        UpdateActStrategy strategy = updateCardStrategies.get(actType);
        if (strategy == null) {
            throw new ApiException(BAD_REQUEST, messageUtil.get("error.unsupported_act_type"));
        }
        return strategy.updateAct(id, reqAct);
    }
}
