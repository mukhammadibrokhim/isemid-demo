package uz.uzinfocom.isemid.services;

import uz.uzinfocom.isemid.models.entities.Form058;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.enums.FilterType;
import uz.uzinfocom.isemid.models.payload.requests.form.ReqForm058;
import uz.uzinfocom.isemid.models.payload.requests.form.ReqForm058AssignCard;
import uz.uzinfocom.isemid.models.payload.requests.form.ReqForm058OrgUpdate;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;

public interface Form058Service {
    ApiResponse<Form058> createForm058(ReqForm058 reqForm058, User user);

    ApiResponse<Form058> updateForm058(ReqForm058 reqForm058, Long id, User user);

    ApiResponsePage getFilteredForm058(int page, int size, User user, String url, boolean isAllApi);

    ApiResponse<Form058> assignCard(ReqForm058AssignCard assignCard, User user);

    ApiResponse<Form058> getById(long id);

    ApiResponse<?> changeReceivedOrd(Long formId, ReqForm058OrgUpdate reqForm058OrgUpdate);

    ApiResponse<?> cancel(Long formId);

    ApiResponsePage getReceivedAll(int page, int size, User user, FilterType received, String fullURL);

    ApiResponse<?> deleteById(Long formId);
}
