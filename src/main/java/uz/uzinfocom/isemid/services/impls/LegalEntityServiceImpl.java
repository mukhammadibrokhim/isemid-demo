package uz.uzinfocom.isemid.services.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.services.LegalEntityService;
import uz.uzinfocom.isemid.services.OAuthTokenService;

import static uz.uzinfocom.isemid.utils.ApiUrlConstant.LEGAL_ENTITY_API;
import static uz.uzinfocom.isemid.utils.RestTemplateUtil.makeRequest;

@Service
@RequiredArgsConstructor
public class LegalEntityServiceImpl implements LegalEntityService {
    final OAuthTokenService oAuthTokenService;

    @Override
    public ApiResponse<?> fetchLegalEntityByTin(String tin) {
        Object resp = makeRequest(String.format(LEGAL_ENTITY_API, tin), HttpMethod.GET, oAuthTokenService.getAccessToken(), null, Object.class);
        return new ApiResponse<>(true,"Successfully fetched legal entity", resp);
    }
}
