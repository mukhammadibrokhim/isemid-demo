package uz.uzinfocom.isemid.features.act.services;


import org.springframework.data.domain.Page;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.models.Act;

import java.util.UUID;

public interface ActService {
    Page<Act> filter(int page, int size, Long cardId);

    Act getById(long id);

    Act update(long id, ActRequest reqAct);

    void deleteById(Long id);

    void sendToLis(Long id, UUID lisLabId, UUID organizationUuid, String token);

    void handleLisResponse(Long id, String body);
}
