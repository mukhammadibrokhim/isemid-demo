package uz.uzinfocom.isemid.features.act.strategy.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.dtos.requests.act154.Act154Request;
import uz.uzinfocom.isemid.features.act.mappers.ActMapper;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.models.act154.Act154;
import uz.uzinfocom.isemid.features.act.repositores.ActRepository;
import uz.uzinfocom.isemid.features.act.strategy.UpdateActStrategy;

import static uz.uzinfocom.isemid.features.act.utils.ActUtil.validateAct;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateAct154Strategy implements UpdateActStrategy {
    private final ActRepository actRepository;
    private final MessageUtil messageUtil;
    private final ActMapper mapper;

    @Override
    public ApiResponse<?> updateAct(long id, ActRequest reqAct) {

        Act act = validateAct(id, reqAct, actRepository, messageUtil);
        Act154 existingAct = (Act154) act;

        Act154Request reqAct154 = (Act154Request) reqAct;
        mapper.update(existingAct, reqAct154);

        return ApiResponse.success(messageUtil.get("message.updated"), mapper.toResponse(actRepository.save(act)));
    }
}
