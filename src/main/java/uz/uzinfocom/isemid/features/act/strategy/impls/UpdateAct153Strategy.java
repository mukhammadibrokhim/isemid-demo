package uz.uzinfocom.isemid.features.act.strategy.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.dtos.requests.act153.Act153Request;
import uz.uzinfocom.isemid.features.act.enums.ActStatus;
import uz.uzinfocom.isemid.features.act.mappers.ActMapper;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.models.act153.Act153;
import uz.uzinfocom.isemid.features.act.repositores.ActRepository;
import uz.uzinfocom.isemid.features.act.strategy.UpdateActStrategy;

import static uz.uzinfocom.isemid.features.act.utils.ActUtil.validateAct;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateAct153Strategy implements UpdateActStrategy {
    private final ActRepository actRepository;
    private final MessageUtil messageUtil;
    private final ActMapper actMapper;

    @Override
    public ApiResponse<?> updateAct(long id, ActRequest request) {
        Act act = validateAct(id, request, actRepository, messageUtil);

        Act153 existingAct = (Act153) act;
        Act153Request request153 = (Act153Request) request;
        existingAct.setActStatus(ActStatus.IN_PROGRESS);

        actMapper.update(existingAct, request153);

        return ApiResponse.success(messageUtil.get("message.updated"), actMapper.toResponse(actRepository.save(act)));
    }
}
