package uz.uzinfocom.isemid.services;

import uz.uzinfocom.isemid.models.entities.Form089;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.payload.requests.ReqForm089;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;

public interface Form089Service {
    ApiResponse<Form089> createForm089(ReqForm089 reqForm089, User user);

    ApiResponse<Form089> updateForm089(int id, ReqForm089 reqForm089, User user);
}
