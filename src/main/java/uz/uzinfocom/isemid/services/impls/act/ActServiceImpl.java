package uz.uzinfocom.isemid.services.impls.act;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.payload.requests.act.ReqAct;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.repositories.act.ActRepository;
import uz.uzinfocom.isemid.services.act.ActService;
import uz.uzinfocom.isemid.services.impls.act.strategy.UpdateActService;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ActServiceImpl implements ActService {

    private final ActRepository actRepository;
    private final UpdateActService updateActService;

    @Override
    public ApiResponse<?> getById(long id) {
        return new ApiResponse<>(true, "successfully retrieved.",
                actRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "Act not found.")));
    }

    @Transactional
    @Override
    public ApiResponse<?> updateAct(long id, ReqAct reqAct, User currentUser) {
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

