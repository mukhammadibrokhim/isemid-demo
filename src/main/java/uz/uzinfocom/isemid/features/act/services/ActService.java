package uz.uzinfocom.isemid.features.act.services;


import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.models.Act;

import java.util.List;

public interface ActService {

    Act getById(long id);

    Act update(long id, ActRequest reqAct);

    void deleteById(Long id);

    List<Act> getAllByCardId(Long cardId);
}
