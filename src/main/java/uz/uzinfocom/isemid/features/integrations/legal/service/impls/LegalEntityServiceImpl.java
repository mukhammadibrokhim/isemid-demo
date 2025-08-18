package uz.uzinfocom.isemid.features.integrations.legal.service.impls;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.integrations.config.OAuthTokenService;
import uz.uzinfocom.isemid.features.integrations.legal.service.LegalEntityService;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static uz.uzinfocom.isemid.commons.ApiPaths.ExternalApi.LEGAL_ENTITY_API;
import static uz.uzinfocom.isemid.commons.utils.CommonUtil.maskTin;
import static uz.uzinfocom.isemid.commons.utils.RestClientUtil.makeRequest;
import static uz.uzinfocom.isemid.features.integrations.legal.utils.ValidationUtil.validationTin;

@Slf4j
@Service
@RequiredArgsConstructor
public class LegalEntityServiceImpl implements LegalEntityService {
    private final OAuthTokenService oAuthTokenService;
    private final MessageUtil messageUtil;

    @Override
    public Object fetchLegalEntityByTin(String tin) {
        log.info("Fetching legal entity info for TIN: {}", maskTin(tin));

        if (!validationTin(tin)) {
            log.warn("TIN validation failed for: {}", maskTin(tin));
            throw new ApiException(BAD_REQUEST, messageUtil.get("error.bad_request"));
        }

        Object resp = makeRequest(String.format(LEGAL_ENTITY_API, tin), HttpMethod.GET, oAuthTokenService.getAccessToken(), null, Object.class);

        log.info("Successfully fetched legal entity info for TIN: {}", maskTin(tin));
        return  resp;
    }
}