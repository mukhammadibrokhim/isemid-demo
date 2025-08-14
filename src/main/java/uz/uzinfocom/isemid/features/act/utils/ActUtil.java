package uz.uzinfocom.isemid.features.act.utils;

import lombok.experimental.UtilityClass;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.enums.ActStatus;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.repositores.ActRepository;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@UtilityClass
public class ActUtil {
    public static Act validateAct(long id, ActRequest request, ActRepository actRepository, MessageUtil messageUtil) {
        Act act = actRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        NOT_FOUND,
                        messageUtil.get("error.not_found_by_id", id)
                ));

        if (request.getType() == null || !request.getType().equals(act.getType())) {
            throw new ApiException(
                    BAD_REQUEST,
                    messageUtil.get("error.unsupported_act_type", request.getType())
            );
        }
        act.setStatus(ActStatus.IN_PROGRESS);

        return act;
    }
}
