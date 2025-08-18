package uz.uzinfocom.isemid.features.act.services.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.repositores.ActRepository;
import uz.uzinfocom.isemid.features.act.services.ActService;
import uz.uzinfocom.isemid.features.act.strategy.impls.UpdateActService;

import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ActServiceImpl implements ActService {
    private final ActRepository actRepository;
    private final UpdateActService updateActService;
    private final MessageUtil messageUtil;

    @Override
    public List<Act> getAllByCardId(Long cardId) {
        return actRepository.findAllByCardId(cardId);
    }

    @Override
    public Act getById(long id) {
        return actRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
    }

    @Transactional
    @Override
    public Act update(long id, ActRequest reqAct) {
        return updateActService.updateAct(id, reqAct);
    }

    @Override
    public void deleteById(Long id) {
        try {
            actRepository.deleteById(id);
        } catch (Exception e) {
            throw new ApiException(INTERNAL_SERVER_ERROR, messageUtil.get("error.internal_server"), e);
        }
    }
}

