package uz.uzinfocom.isemid.services.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.enums.CatalogType;
import uz.uzinfocom.isemid.models.entities.enums.IdentifierType;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;
import uz.uzinfocom.isemid.models.payload.responses.RespChild;
import uz.uzinfocom.isemid.models.payload.responses.RespCitizen;
import uz.uzinfocom.isemid.services.CitizenService;
import uz.uzinfocom.isemid.services.OAuthTokenService;
import uz.uzinfocom.isemid.services.value_set.CatalogService;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uz.uzinfocom.isemid.utils.ApiUrlConstant.GCP_CHILD;
import static uz.uzinfocom.isemid.utils.ApiUrlConstant.GCP_CITIZEN;
import static uz.uzinfocom.isemid.utils.CommonUtils.validateByNnuzb;
import static uz.uzinfocom.isemid.utils.CommonUtils.validateChildCertificate;
import static uz.uzinfocom.isemid.utils.RestTemplateUtil.makeRequest;

@Service
@RequiredArgsConstructor
public class CitizenServiceImpl implements CitizenService {
    final CatalogService catalogService;
    final OAuthTokenService oAuthTokenService;

    @Override
    public Object getCitizenByNnuzb(String nnuzb, String photo, String token) {
        validateByNnuzb(nnuzb);
        RespCitizen respCitizen = makeRequest(String.format(GCP_CITIZEN, nnuzb, photo), HttpMethod.GET, token, null, RespCitizen.class);
        return mappingCitizen(respCitizen);
    }

    @Override
    public Object getCitizenByChild(String series, String number, String token) {
        validateChildCertificate(series, number);
        RespChild respChild = makeRequest(String.format(GCP_CHILD, series, number), HttpMethod.GET, token, null, RespChild.class);
        return mappingChild(respChild);
    }

    @Override
    public ResponseEntity<?> getCitizenByNnuzbByToken(String nnuzb) {
        return ResponseEntity.ok(getCitizenByNnuzb(nnuzb, "N", oAuthTokenService.getAccessToken()));
    }


    private Object mappingCitizen(RespCitizen respCitizen) {
        if (respCitizen == null) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Couldn't fetch citizen");
        }

        if (respCitizen.getData() == null || respCitizen.getData().isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "No citizen data found");
        }

        RespCitizen.CitizenData data = respCitizen.getData().getFirst();

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

    private Object mappingChild(RespChild respChild) {
        if (respChild == null) throw new ApiException(BAD_REQUEST, "No child data found");

        List<RespCitizen.CitizenData> outputDataList = respChild.getItems().stream()
                .map(input -> RespCitizen.CitizenData.builder()
                        .transaction_id(1) //
                        .current_pinpp(input.getPnfl())
                        .pinpps(List.of(input.getPnfl()))
                        .current_document(input.getCert_series() + input.getCert_number())
                        .documents(List.of(
                                RespCitizen.Document.builder()
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

        return RespCitizen.builder()
                .result("1")
                .data(outputDataList)
                .comments("")
                .build();
    }
}
