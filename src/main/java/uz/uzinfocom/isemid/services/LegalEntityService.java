package uz.uzinfocom.isemid.services;

import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;

public interface LegalEntityService {
    ApiResponse<?> fetchLegalEntityByTin(String tin, String token);
}
