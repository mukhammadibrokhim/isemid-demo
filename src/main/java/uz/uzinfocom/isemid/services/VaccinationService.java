package uz.uzinfocom.isemid.services;

import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;

public interface VaccinationService {
    ApiResponse<?> getVaccinationByNnuzbLast(String nnuzb);
}
