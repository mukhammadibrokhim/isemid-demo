package uz.uzinfocom.isemid.services.impls.act;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.act.Act;
import uz.uzinfocom.isemid.models.entities.act.ActStatus;
import uz.uzinfocom.isemid.models.entities.card.Card;
import uz.uzinfocom.isemid.models.payload.requests.act.ReqAct;
import uz.uzinfocom.isemid.models.payload.requests.act.ReqCardAssignAct;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.repositories.act.ActRepository;
import uz.uzinfocom.isemid.repositories.card.CardRepository;
import uz.uzinfocom.isemid.services.act.ActService;
import uz.uzinfocom.isemid.services.impls.act.update.UpdateActsService;

import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uz.uzinfocom.isemid.utils.CommonUtils.*;

@Service
@RequiredArgsConstructor
@Primary
public class ActServiceImpl implements ActService {
    private final CardRepository cardRepository;
    private final ActRepository actRepository;
    private final UpdateActsService updateActsService;


//

    @Override
    public ApiResponse<?> getById(long id) {
        return new ApiResponse<>(true, "successfully retrieved.",
                actRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "Act not found.")));
    }
    @Transactional
    @Override
    public ApiResponse<?> updateAct(long id, ReqAct reqAct, User currentUser) {
        return updateActsService.updateAct(id, reqAct, currentUser);
    }

    @Override
    public ApiResponse<Card> assignAct(ReqCardAssignAct reqCardAssignAct, User user) {
        Card card = cardRepository.findById(reqCardAssignAct.getCardId())
                .orElseThrow(() -> new ApiException(NOT_FOUND, "Card not found by id: " + reqCardAssignAct.getCardId()));

        List<Act> acts = reqCardAssignAct.getActTypes().stream()
                .map(actType -> {
                    Act act = createAct(actType);
                    act.setActType(actType);
                    act.setCard(card);
                    act.setActStatus(ActStatus.NEW);
                    return act;
                })
                .toList();

        card.getActs().addAll(acts);

        cardRepository.save(card);
        actRepository.saveAll(acts);

        return new ApiResponse<>(true, "Acts successfully assigned.", card);
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

