package uz.uzinfocom.isemid.services;

import uz.uzinfocom.isemid.models.payload.requests.ReqJournal060;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;

public interface Journal060Service {
    ApiResponsePage getAllJournal060Filter(int page, int size, String request);

    ApiResponse<?> getById(long id);

    ApiResponse<?> update(long id, ReqJournal060 reqJournal060);
}
