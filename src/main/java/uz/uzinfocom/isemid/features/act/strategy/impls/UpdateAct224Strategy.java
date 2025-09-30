package uz.uzinfocom.isemid.features.act.strategy.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.dtos.requests.act224.Act224Request;
import uz.uzinfocom.isemid.features.act.mappers.ActMapperFactory;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.models.act224.Act224;
import uz.uzinfocom.isemid.features.act.repositores.ActRepository;
import uz.uzinfocom.isemid.features.act.strategy.UpdateActStrategy;

import static uz.uzinfocom.isemid.features.act.utils.ActBindingUtil.bindChildEntities;
import static uz.uzinfocom.isemid.features.act.utils.ActUtil.validateAct;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateAct224Strategy implements UpdateActStrategy {
    private final ActRepository actRepository;
    private final MessageUtil messageUtil;
    private final ActMapperFactory mapper;

    @Override
    @Transactional
    public Act updateAct(long id, ActRequest request) {
        Act224 existingAct = (Act224) validateAct(id, request, actRepository, messageUtil);
        Act224Request request224 = (Act224Request) request;

        mapper.update(existingAct, request224);
//        bindChildEntities(existingAct);

        return actRepository.save(existingAct);
    }
}
