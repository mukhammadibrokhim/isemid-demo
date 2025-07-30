package uz.uzinfocom.isemid.features.act.strategy.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.dtos.requests.act223.Act223Request;
import uz.uzinfocom.isemid.features.act.mappers.ActMapper;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.models.act223.Act223;
import uz.uzinfocom.isemid.features.act.repositores.ActRepository;
import uz.uzinfocom.isemid.features.act.strategy.UpdateActStrategy;

import static uz.uzinfocom.isemid.features.act.utils.ActUtil.validateAct;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateAct223Strategy implements UpdateActStrategy {
    private final ActRepository actRepository;
    private final MessageUtil messageUtil;
    private final ActMapper mapper;

    @Override
    public ApiResponse<?> updateAct(long id, ActRequest reqAct) {
        Act act = validateAct(id, reqAct, actRepository, messageUtil);

        Act223 existingAct = (Act223) act;
        Act223Request reqAct223 = (Act223Request) reqAct;
        mapper.update(existingAct, reqAct223);
        return ApiResponse.success(messageUtil.get("message.updated"), mapper.toResponse(actRepository.save(act)));
    }
}
