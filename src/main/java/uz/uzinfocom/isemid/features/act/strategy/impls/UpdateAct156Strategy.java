package uz.uzinfocom.isemid.features.act.strategy.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.dtos.requests.act156.Act156Request;
import uz.uzinfocom.isemid.features.act.mappers.ActMapperFactory;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.models.act156.Act156;
import uz.uzinfocom.isemid.features.act.repositores.ActRepository;
import uz.uzinfocom.isemid.features.act.strategy.UpdateActStrategy;
import uz.uzinfocom.isemid.features.act.utils.ActUtil;

import static uz.uzinfocom.isemid.features.act.utils.ActBindingUtil.bindChildEntities;


@Service
@RequiredArgsConstructor
@Transactional
public class UpdateAct156Strategy implements UpdateActStrategy {
    private final ActRepository actRepository;
    private final MessageUtil messageUtil;
    private final ActMapperFactory mapper;

    @Override
    public ApiResponse<?> updateAct(long id, ActRequest reqAct) {
        Act act = ActUtil.validateAct(id, reqAct, actRepository, messageUtil);

        Act156 existingAct = (Act156) act;
        Act156Request reqAct156 = (Act156Request) reqAct;

        mapper.update(existingAct, reqAct156);
        bindChildEntities(existingAct);
        return ApiResponse.success(messageUtil.get("message.updated"), mapper.toResponse(actRepository.save(act)));
    }
}