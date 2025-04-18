package uz.uzinfocom.isemid.features.integrations.vacciantion.services;

import uz.uzinfocom.isemid.commons.dtos.ApiResponse;

public interface VaccinationService {
    ApiResponse<?> getVaccinationByNnuzbLast(String nnuzb);
}
