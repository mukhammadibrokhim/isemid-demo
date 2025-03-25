package uz.uzinfocom.isemid.services.impls.act.strategy;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.act.Act;
import uz.uzinfocom.isemid.models.entities.act.act224.Act224;
import uz.uzinfocom.isemid.models.entities.enums.CatalogType;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;
import uz.uzinfocom.isemid.models.payload.requests.act.ReqAct;
import uz.uzinfocom.isemid.models.payload.requests.act.act224.ReqAct224;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.repositories.act.ActRepository;
import uz.uzinfocom.isemid.services.act.strategy.UpdateActStrategy;
import uz.uzinfocom.isemid.services.value_set.CatalogService;

import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uz.uzinfocom.isemid.models.entities.enums.CatalogType.country;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateAct224Strategy implements UpdateActStrategy {

    private final CatalogService catalogService;
    private final ActRepository actRepository;

    private Catalog getCatalogByCode(CatalogType type, String code) throws ApiException {
        return catalogService.findByTypeAndCode(type, code).orElse(null);
    }

    @Override
    public ApiResponse<?> updateAct(long id, ReqAct reqAct, User currentUser) {
        Act act = actRepository.findById(id)
                .orElseThrow(() -> new ApiException(NOT_FOUND, "Act224 not found."));

        if (reqAct.getActType() == null || !reqAct.getActType().equals(act.getActType())) {
            throw new ApiException(BAD_REQUEST, "Invalid act type: expected " + act.getActType() + " but received " + reqAct.getActType());
        }
        Act224 existingAct = (Act224) act;
        ReqAct224 reqAct224 = (ReqAct224) reqAct;

        Optional.ofNullable(reqAct224.getTitle()).ifPresent(existingAct::setTitle);
        Optional.ofNullable(reqAct224.getDocumentSendDate()).ifPresent(existingAct::setDocumentSendDate);
        Optional.ofNullable(reqAct224.getRegionCode()).ifPresent(regionCode -> {
            existingAct.setRegionCode(regionCode);
            getCatalogByCode(country, regionCode);
        });
        Optional.ofNullable(reqAct224.getFullNameOfEpidStaff()).ifPresent(existingAct::setFullNameOfEpidStaff);
        Optional.ofNullable(reqAct224.getNameOfInstitution()).ifPresent(existingAct::setNameOfInstitution);
        Optional.ofNullable(reqAct224.getAddressOfEpidStaff()).ifPresent(existingAct::setAddressOfEpidStaff);
        Optional.ofNullable(reqAct224.getNameOfRegulatoryActs()).ifPresent(existingAct::setNameOfRegulatoryActs);
        Optional.ofNullable(reqAct224.getCheckingFulfillmentOfRequirements()).ifPresent(existingAct::setCheckingFulfillmentOfRequirements);
        Optional.ofNullable(reqAct224.getFullNameOfParticipant()).ifPresent(existingAct::setFullNameOfParticipant);
        Optional.ofNullable(reqAct224.getRecommendedActivities()).ifPresent(existingAct::setRecommendedActivities);
        Optional.ofNullable(reqAct224.getExecutionPeriod()).ifPresent(existingAct::setExecutionPeriod);
        return new ApiResponse<>(true, "Success", null);
    }
}
