package uz.uzinfocom.isemid.features.act.services;


import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import uz.uzinfocom.isemid.features.act.dtos.LisRequest;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.dtos.requests.ProtocolConclusionRequest;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.models.common.DocumentType;

public interface ActService {
    Page<Act> filter(int page, int size, Long cardId);

    Act getById(long id);

    Act update(long id, ActRequest reqAct);

    void deleteById(Long id);

    void sendToLis(LisRequest lisRequest, UUID organizationUuid, String token);

    void attachLisDocument(Long id, DocumentType documentType, Map<String,Object> document);

    Map<String,Object> getReviewedDocument(Long actId);
    Map<String,Object> getProtocol(Long actId);
    void concludeProtocol(Long actId, ProtocolConclusionRequest request);
}
