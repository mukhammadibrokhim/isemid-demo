package uz.uzinfocom.isemid.features.integrations.legal.service.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.integrations.config.OAuthTokenService;
import uz.uzinfocom.isemid.features.integrations.legal.service.LegalEntityService;

import static uz.uzinfocom.isemid.commons.ApiPaths.ExternalApi.LEGAL_ENTITY_API;
import static uz.uzinfocom.isemid.commons.utils.RestClientUtil.makeRequest;

@Service
@RequiredArgsConstructor
public class LegalEntityServiceImpl implements LegalEntityService {

    private final OAuthTokenService oAuthTokenService;

    @Override
    public ApiResponse<?> fetchLegalEntityByTin(String tin) {
        Object resp = makeRequest(String.format(LEGAL_ENTITY_API, tin), HttpMethod.GET, oAuthTokenService.getAccessToken(), null, Object.class);
        return new ApiResponse<>(true, MessageUtil.OPERATION_SUCCESS, resp);
    }
}
