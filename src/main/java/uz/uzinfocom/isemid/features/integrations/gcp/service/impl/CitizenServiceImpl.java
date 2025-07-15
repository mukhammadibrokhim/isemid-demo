package uz.uzinfocom.isemid.features.integrations.gcp.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.integrations.config.OAuthTokenService;
import uz.uzinfocom.isemid.features.integrations.gcp.dto.ChildResponse;
import uz.uzinfocom.isemid.features.integrations.gcp.dto.CitizenResponse;
import uz.uzinfocom.isemid.features.integrations.gcp.mappers.CitizenMapper;
import uz.uzinfocom.isemid.features.integrations.gcp.service.CitizenService;

import java.time.LocalDate;

import static uz.uzinfocom.isemid.commons.constants.ApiPaths.ExternalApi.GCP_CHILD;
import static uz.uzinfocom.isemid.commons.constants.ApiPaths.ExternalApi.GCP_CITIZEN;
import static uz.uzinfocom.isemid.commons.utils.RestClientUtil.makeRequest;
import static uz.uzinfocom.isemid.features.integrations.gcp.util.ValidationUtil.validateByNnuzb;
import static uz.uzinfocom.isemid.features.integrations.gcp.util.ValidationUtil.validateChildCertificate;

@Service
@RequiredArgsConstructor
public class CitizenServiceImpl implements CitizenService {
    private final CatalogService catalogService;
    private final OAuthTokenService oAuthTokenService;
    private final CitizenMapper citizenMapper;

    @Override
    public Object getCitizenByNnuzb(String nnuzb, LocalDate birthDate, String photo, String token) {
        validateByNnuzb(nnuzb);
        CitizenResponse response = makeRequest(String.format(GCP_CITIZEN, nnuzb, birthDate, photo), HttpMethod.GET, token, null, CitizenResponse.class);

        CitizenResponse mappedResponse = citizenMapper.mapToResponse(response);
        mappedResponse.getData().forEach(data -> citizenMapper.transformData(data, catalogService));

        return mappedResponse;
    }

    @Override
    public Object getCitizenByChild(String series, String number, String token) {
        validateChildCertificate(series, number);
        ChildResponse respChild = makeRequest(String.format(GCP_CHILD, series, number), HttpMethod.GET, token, null, ChildResponse.class);
        return citizenMapper.mapToCitizenResponse(respChild);
    }

    @Override
    public ResponseEntity<?> getCitizenByNnuzbByToken(String nnuzb, LocalDate birthDate) {
        return ResponseEntity.ok(getCitizenByNnuzb(nnuzb, birthDate, "N", oAuthTokenService.getAccessToken()));
    }
}
