package uz.uzinfocom.isemid.features.act.strategy.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.dtos.requests.act155.Act155Request;
import uz.uzinfocom.isemid.features.act.mappers.ActMapperFactory;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.models.act155.Act155;
import uz.uzinfocom.isemid.features.act.repositores.ActRepository;
import uz.uzinfocom.isemid.features.act.strategy.UpdateActStrategy;

import static uz.uzinfocom.isemid.features.act.utils.ActBindingUtil.bindChildEntities;
import static uz.uzinfocom.isemid.features.act.utils.ActUtil.validateAct;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateAct155Strategy implements UpdateActStrategy {
    private final ActRepository actRepository;
    private final MessageUtil messageUtil;
    private final ActMapperFactory mapper;

    @Override
    public Act updateAct(long id, ActRequest reqAct) {
        Act act = validateAct(id, reqAct, actRepository, messageUtil);

        Act155 existingAct = (Act155) act;
        Act155Request reqAct155 = (Act155Request) reqAct;

        mapper.update(existingAct, reqAct155);
        bindChildEntities(existingAct);
        return actRepository.save(act);
    }
}
