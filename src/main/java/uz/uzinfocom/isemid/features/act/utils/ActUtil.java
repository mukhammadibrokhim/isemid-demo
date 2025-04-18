package uz.uzinfocom.isemid.features.act.utils;

import lombok.experimental.UtilityClass;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.repositores.ActRepository;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@UtilityClass
public class ActUtil {

    public static Act validateAct(long id, ActRequest reqAct, ActRepository actRepository) {
        Act act = actRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, String.format(MessageUtil.NOT_FOUND_BY_ID, id)));

        if (reqAct.getActType() == null || !reqAct.getActType().equals(act.getActType())) {
            throw new ApiException(BAD_REQUEST, String.format(MessageUtil.INVALID_TYPE, act.getActType(), reqAct.getActType()));
        }
        return act;
    }
}
