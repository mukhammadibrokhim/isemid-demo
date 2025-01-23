package uz.uzinfocom.isemid.services;

import uz.uzinfocom.isemid.models.entities.Form058;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.payload.requests.ReqForm058;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;

public interface Form058Service {
    ApiResponse<Form058> createForm058(ReqForm058 reqForm058, User user);

    ApiResponse<Form058> updateForm058(ReqForm058 reqForm058, Long id, User user);

    ApiResponsePage getFilteredForm058(int page, int size, User user, String url);
}
