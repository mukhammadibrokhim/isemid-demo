package uz.uzinfocom.isemid.features.act.strategy.impls;

import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.enums.ActStatus;
import uz.uzinfocom.isemid.features.act.enums.ActType;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.models.LisDocumentAttachable;
import uz.uzinfocom.isemid.features.act.models.common.DocumentType;
import uz.uzinfocom.isemid.features.act.strategy.UpdateActStrategy;

import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateActService {
    private final Map<ActType, UpdateActStrategy> updateCardStrategies;
    private final MessageUtil messageUtil;

    public Act updateAct(long id, ActRequest reqAct) {
        ActType actType = reqAct.getType();

        UpdateActStrategy strategy = updateCardStrategies.get(actType);
        if (strategy == null) {
            throw new ApiException(BAD_REQUEST, messageUtil.get("error.unsupported_act_type"));
        }
        return strategy.updateAct(id, reqAct);
    }

    public void attachLisDocument(Act act, DocumentType documentType, Map<String, Object> document) {
        if (!(act instanceof LisDocumentAttachable attachable)) {
            throw new ApiException(INTERNAL_SERVER_ERROR, messageUtil.get("error.internal_server"));
        }

        if (documentType == DocumentType.ACT) {
            Long lisActId = Optional.ofNullable(document.get("id"))
                .map(Number.class::cast)
                .map(Number::longValue)
                .orElse(null);

            ActStatus actStatus = Optional.ofNullable(document.get("status"))
                .map(String.class::cast)
                .map(ActStatus::valueOf)
                .orElse(null);

            act.setLisActId(lisActId);       // on base Act
            attachable.setLisActResponse(document);
            act.setStatus(actStatus);
        } else {
            attachable.setLisProtocolResponse(document);
            act.setStatus(ActStatus.COMPLETED);
        }
    }
}
