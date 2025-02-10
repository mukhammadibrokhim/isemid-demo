package uz.uzinfocom.isemid.services;

import uz.uzinfocom.isemid.models.entities.Form058;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.card.Card175;
import uz.uzinfocom.isemid.models.payload.requests.ReqCard175;
import uz.uzinfocom.isemid.models.payload.requests.form.ReqForm058;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;

public interface Card175Service {
    ApiResponse<Card175> createCard175(ReqCard175 reqCard175, User user);
}
