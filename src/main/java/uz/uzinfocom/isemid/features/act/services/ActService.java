package uz.uzinfocom.isemid.features.act.services;


import org.springframework.data.domain.Page;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.models.Act;

public interface ActService {
    Page<Act> filter(int page, int size, Long cardId);

    Act getById(long id);

    Act update(long id, ActRequest reqAct);

    void deleteById(Long id);
}
