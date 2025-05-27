package uz.uzinfocom.isemid.features.act.services;


import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.user.models.User;

public interface ActService {

   ApiResponse<?> getById(long id);

    ApiResponse<?> updateAct(long id, ActRequest reqAct, User currentUser);

    ApiResponse<?> deleteById(Long id);

    ApiResponse<?> getAllByCardId(Long cardId);
}
