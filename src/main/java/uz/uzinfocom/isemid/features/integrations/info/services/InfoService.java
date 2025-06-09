package uz.uzinfocom.isemid.features.integrations.info.services;

import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.features.integrations.info.dtos.responses.InfoIcd10Response;

public interface InfoService {
    ApiResponse<?> getPatientMHByNnuzb(String nnuzb);

    ApiResponse<InfoIcd10Response> getConfirmedMkb10ByNnuzb(String nnuzb);
}
