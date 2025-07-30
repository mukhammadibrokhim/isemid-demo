package uz.uzinfocom.isemid.features.act.strategy.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.enums.ActType;
import uz.uzinfocom.isemid.features.act.strategy.UpdateActStrategy;

import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateActService {
    private final Map<ActType, UpdateActStrategy> updateCardStrategies;
    private final MessageUtil messageUtil;

    public ApiResponse<?> updateAct(long id, ActRequest reqAct) {
        ActType actType = reqAct.getActType();

        UpdateActStrategy strategy = updateCardStrategies.get(actType);
        if (strategy == null) {
            throw new ApiException(BAD_REQUEST, messageUtil.get("error.unsupported_act_type"));
        }
        return strategy.updateAct(id, reqAct);
    }
}
