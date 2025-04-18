package uz.uzinfocom.isemid.features.integrations.gcp.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.integrations.config.OAuthTokenService;
import uz.uzinfocom.isemid.features.integrations.gcp.mappers.CitizenMapper;
import uz.uzinfocom.isemid.features.integrations.gcp.dto.ChildResponse;
import uz.uzinfocom.isemid.features.integrations.gcp.dto.CitizenResponse;
import uz.uzinfocom.isemid.features.integrations.gcp.service.CitizenService;
import uz.uzinfocom.isemid.features.patient.enums.IdentifierType;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uz.uzinfocom.isemid.commons.ApiPaths.ExternalApi.GCP_CHILD;
import static uz.uzinfocom.isemid.commons.ApiPaths.ExternalApi.GCP_CITIZEN;
import static uz.uzinfocom.isemid.commons.utils.CommonUtil.checkNnuzb;
import static uz.uzinfocom.isemid.commons.utils.RestClientUtil.makeRequest;

@Service
@RequiredArgsConstructor
public class CitizenServiceImpl implements CitizenService {
    private final CatalogService catalogService;
    private final OAuthTokenService oAuthTokenService;
    private final CitizenMapper citizenMapper;

    public static void validateByNnuzb(String nnuzb) {
        if (!checkNnuzb(nnuzb)) {
            throw new ApiException(BAD_REQUEST, "Invalid NNUZB or photo query param invalid");
        }
    }

    public static void validateChildCertificate(String serial, String number) {
        if (!isValidCertificateSeries(serial)) {
            throw new ApiException(BAD_REQUEST, "Invalid certificate series.");
        }
        if (!isValidNumber(number)) {
            throw new ApiException(BAD_REQUEST, "Invalid certificate number.");
        }
    }

    public static boolean isValidCertificateSeries(String series) {
        String validSeriesPattern = "[A-Z-]+";
        return series != null && series.matches(validSeriesPattern);
    }

    public static boolean isValidNumber(String number) {
        return number != null && number.matches("\\d+");
    }


    @Override
    public Object getCitizenByNnuzb(String nnuzb, String photo, String token) {
        validateByNnuzb(nnuzb);
//        photo = "N";
        CitizenResponse response = makeRequest(String.format(GCP_CITIZEN, nnuzb, photo), HttpMethod.GET, token, null, CitizenResponse.class);

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
    public ResponseEntity<?> getCitizenByNnuzbByToken(String nnuzb) {
        return ResponseEntity.ok(getCitizenByNnuzb(nnuzb, "N", oAuthTokenService.getAccessToken()));
    }


    private Object mappingCitizen(CitizenResponse respCitizen) {
        if (respCitizen == null) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Couldn't fetch citizen");
        }

        if (respCitizen.getData() == null || respCitizen.getData().isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "No citizen data found");
        }

        CitizenResponse.CitizenData data = respCitizen.getData().getFirst();

        if (!data.getDocuments().isEmpty()) {
            data.getDocuments()
                    .forEach(doc -> {
                        switch (doc.getType()) {
                            case "IDMS_RECV_CITIZ_DOCUMENTS":
                                doc.setType("PPN");
                                break;
                            case "IDMS_RECV_IP_DOCUMENTS":
                                doc.setType("TPPN");
                                break;
                            case "IDMS_RECV_LBG_DOCUMENTS":
                                doc.setType("FID");
                                break;
                            case "IDMS_RECV_MVD_IDCARD_CITIZEN", "IDMS_RECV_MVD_IDCARD_NEWBORN":
                                doc.setType("CZ");
                                break;
                            case "IDMS_RECV_MVD_IDCARD_FOREIGN", "IDMS_RECV_MVD_IDCARD_LBG":
                                doc.setType("CZF");
                                break;
                            case "IDMS_RECV_MJ_BIRTH_CERTS":
                                doc.setType("BCT");
                                break;
                        }
                    });
        }
        if (data.getNationalityid() != 0) {
            Optional<Catalog> nationality = catalogService.findByTypeAndParentCode(CatalogType.nationality, String.valueOf(data.getNationalityid()));
            nationality.ifPresent(value -> data.setNationalityid(Integer.parseInt(value.getCode())));
        }

        if (!data.getCitizenshipid().isEmpty()) {
            Optional<Catalog> country = catalogService.findByTypeAndParentCode(CatalogType.country, data.getCitizenshipid());
            country.ifPresent(value -> data.setCitizenshipid(value.getCode()));
        }

        if (!data.getBirthcountryid().isEmpty()) {
            Optional<Catalog> country = catalogService.findByTypeAndParentCode(CatalogType.country, data.getBirthcountryid());
            country.ifPresent(value -> data.setBirthcountryid(value.getCode()));
        }

        if (!data.getLivestatus().isEmpty()) {
            if (data.getLivestatus().equals("1")) {
                data.setLivestatus("false");
            } else
                data.setLivestatus("true");
        }

        if (!data.getSex().isEmpty()) {
            if (data.getSex().equals("1")) {
                data.setSex("male");
            } else data.setSex("female");
        }

        return respCitizen;
    }

    private Object mappingChild(ChildResponse respChild) {
        if (respChild == null) throw new ApiException(BAD_REQUEST, "No child data found");

        List<CitizenResponse.CitizenData> outputDataList = respChild.getItems().stream()
                .map(input -> CitizenResponse.CitizenData.builder()
                        .transaction_id(1) //
                        .current_pinpp(input.getPnfl())
                        .pinpps(List.of(input.getPnfl()))
                        .current_document(input.getCert_series() + input.getCert_number())
                        .documents(List.of(
                                CitizenResponse.Document.builder()
                                        .document(input.getCert_series() + input.getCert_number())
                                        .type(IdentifierType.BCT.name())
                                        .docgiveplaceid(input.getBranch())
                                        .datebegin(input.getDoc_date())
                                        .build()
                        ))
                        .surnamelat(input.getSurname())
                        .namelat(input.getName())
                        .patronymlat(input.getPatronym())
                        .surnamecyr(input.getSurname())
                        .namecyr(input.getName())
                        .patronymcyr(input.getPatronym())
                        .birth_date(input.getBirth_date())
                        .sex(input.getGender_code() == 1 ? "male" : "female")
                        .build()).toList();

        return CitizenResponse.builder()
                .result("1")
                .data(outputDataList)
                .comments("")
                .build();
    }

}
