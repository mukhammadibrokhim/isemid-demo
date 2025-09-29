package uz.uzinfocom.isemid.features.act.strategy.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.dtos.requests.act156.Act156Request;
import uz.uzinfocom.isemid.features.act.mappers.ActMapperFactory;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.models.act156.Act156;
import uz.uzinfocom.isemid.features.act.repositores.ActRepository;
import uz.uzinfocom.isemid.features.act.strategy.UpdateActStrategy;

import static uz.uzinfocom.isemid.features.act.utils.ActBindingUtil.bindChildEntities;
import static uz.uzinfocom.isemid.features.act.utils.ActUtil.validateAct;


@Service
@RequiredArgsConstructor
@Transactional
public class UpdateAct156Strategy implements UpdateActStrategy {
    private final ActRepository actRepository;
    private final MessageUtil messageUtil;
    private final ActMapperFactory mapper;

    @Override
    public Act updateAct(long id, ActRequest reqAct) {
        Act156 existingAct = (Act156) validateAct(id, reqAct, actRepository, messageUtil);
        Act156Request reqAct156 = (Act156Request) reqAct;

        mapper.update(existingAct, reqAct156);
        bindChildEntities(existingAct);
        return actRepository.save(existingAct);
    }
}