package uz.uzinfocom.isemid.features.integrations.legal.service;

import uz.uzinfocom.isemid.commons.dtos.ApiResponse;

public interface LegalEntityService {
    ApiResponse<?> fetchLegalEntityByTin(String tin, String token);
}
