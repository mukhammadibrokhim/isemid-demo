package uz.uzinfocom.isemid.features.act.services.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.dtos.responses.ActResponse;
import uz.uzinfocom.isemid.features.act.mappers.ActMapper;
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
    private final ActMapper actMapper;

    @Override
    public ApiResponse<?> getAllByCardId(Long cardId) {
        List<ActResponse> acts = actRepository.findAllByCardId(cardId)
                .stream().map(actMapper::toResponse).toList();
        return ApiResponse.success(messageUtil.get("message.success"), acts);
    }

    @Override
    public ApiResponse<?> getById(long id) {
        Act act = actRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
        return ApiResponse.success(messageUtil.get("message.success"), actMapper.toResponse(act));
    }

    @Transactional
    @Override
    public ApiResponse<?> updateAct(long id, ActRequest reqAct) {
        return updateActService.updateAct(id, reqAct);
    }

    @Override
    public ApiResponse<?> deleteById(Long id) {
        try {
            actRepository.deleteById(id);
        } catch (Exception e) {
            throw new ApiException(INTERNAL_SERVER_ERROR, messageUtil.get("error.internal_server"), e);
        }
        return ApiResponse.success(messageUtil.get("message.deleted"));
    }
}

