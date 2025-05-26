package uz.uzinfocom.isemid.features.act.services.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.mappers.ActMapper;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.repositores.ActRepository;
import uz.uzinfocom.isemid.features.act.services.ActService;
import uz.uzinfocom.isemid.features.act.strategy.impls.UpdateActService;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ActServiceImpl implements ActService {

    private final ActRepository actRepository;
    private final UpdateActService updateActService;
    private final ActMapper actMapper;

    @Override
    public ApiResponse<?> getAllByCardId(Long cardId) {
        List<Act> acts = actRepository.findAllByCardId(cardId);
        return new ApiResponse<>(true, MessageUtil.FETCHED_SUCCESS, acts);
    }

    @Override
    public ApiResponse<?> getById(long id) {
        Act act = actRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, String.format(MessageUtil.NOT_FOUND_BY_ID, id)));
        return new ApiResponse<>(true, MessageUtil.FETCHED_SUCCESS, actMapper.toResponse(act));
    }

    @Transactional
    @Override
    public ApiResponse<?> updateAct(long id, ActRequest reqAct, User currentUser) {
        return updateActService.updateAct(id, reqAct, currentUser);
    }

    @Override
    public ApiResponse<?> deleteById(Long id) {
        try {
            actRepository.deleteById(id);
        } catch (Exception e) {
            throw new ApiException(INTERNAL_SERVER_ERROR, "Internal Server Error", e);
        }
        return new ApiResponse<>(true, "Successfully deleted.", null);
    }
}

