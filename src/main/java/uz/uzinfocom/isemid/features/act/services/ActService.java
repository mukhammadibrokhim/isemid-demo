package uz.uzinfocom.isemid.features.act.services;


import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;

public interface ActService {

   ApiResponse<?> getById(long id);

    ApiResponse<?> updateAct(long id, ActRequest reqAct);

    ApiResponse<?> deleteById(Long id);

    ApiResponse<?> getAllByCardId(Long cardId);
}
