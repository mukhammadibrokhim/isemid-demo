package uz.uzinfocom.isemid.features.act.strategy.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.enums.ActType;
import uz.uzinfocom.isemid.features.act.strategy.UpdateActStrategy;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateActService {
    private final Map<ActType, UpdateActStrategy> updateCardStrategies;

    public ApiResponse<?> updateAct(long id, ActRequest reqAct, User currentUser) {
        ActType actType = reqAct.getActType();

        UpdateActStrategy strategy = updateCardStrategies.get(actType);
        if (strategy == null) {
            throw new ApiException(BAD_REQUEST, "Неподдерживаемый тип действия: " + actType);
        }
        return strategy.updateAct(id, reqAct, currentUser);
    }
}
