package uz.uzinfocom.isemid.services.impls.act.strategy;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.act.Act;
import uz.uzinfocom.isemid.models.entities.act.ActStatus;
import uz.uzinfocom.isemid.models.entities.act.act154.Act154;
import uz.uzinfocom.isemid.models.entities.act.act154.Act154SampleDetails;
import uz.uzinfocom.isemid.models.entities.enums.ActType;
import uz.uzinfocom.isemid.models.entities.enums.CatalogType;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;
import uz.uzinfocom.isemid.models.payload.requests.act.ReqAct;
import uz.uzinfocom.isemid.models.payload.requests.act.act154.ReqAct154;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.repositories.act.ActRepository;
import uz.uzinfocom.isemid.services.act.strategy.UpdateActStrategy;
import uz.uzinfocom.isemid.services.value_set.CatalogService;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateAct154Strategy implements UpdateActStrategy {
    final ActRepository actRepository;
    final CatalogService catalogService;

    private Catalog getCatalogByCode(CatalogType type, String code) throws ApiException {
        return catalogService.findByTypeAndCode(type, code).orElse(null);
    }


    @Override
    public ApiResponse<?> updateAct(long id, ReqAct reqAct, User currentUser) {

        Act act =  actRepository.findById(id) .orElseThrow(() -> new ApiException(NOT_FOUND, "Act154 not found."));

        if (reqAct.getActType() == null || !reqAct.getActType().equals(act.getActType())) {
            throw new ApiException(BAD_REQUEST, "Invalid act type: expected " + act.getActType() + " but received " + reqAct.getActType());
        }

        Act154 existingAct = (Act154) act;

        ReqAct154 reqAct154 = (ReqAct154) reqAct;
        Optional.ofNullable(reqAct154.getTitle()).ifPresent(existingAct::setTitle);
        Optional.ofNullable(reqAct154.getSerialNumber()).ifPresent(existingAct::setSerialNumber);
        Optional.ofNullable(reqAct154.getDocumentSendDate()).ifPresent(existingAct::setDocumentSendDate);
        Optional.ofNullable(reqAct154.getTin()).ifPresent(existingAct::setTin);
        Optional.ofNullable(reqAct154.getNameOfObject()).ifPresent(existingAct::setNameOfObject);
        Optional.ofNullable(reqAct154.getObjectAddress()).ifPresent(existingAct::setObjectAddress);
        Optional.ofNullable(reqAct154.getHospitalizationDate()).ifPresent(existingAct::setHospitalizationDate);
        Optional.ofNullable(reqAct154.getSamplerFullName()).ifPresent(existingAct::setSamplerFullName);
        Optional.ofNullable(reqAct154.getPosition()).ifPresent(existingAct::setPosition);
        Optional.ofNullable(reqAct154.getAdditionalInfo()).ifPresent(existingAct::setAdditionalInfo);
        Optional.ofNullable(reqAct154.getActClassificatorCode()).ifPresent(code -> existingAct.setActClassificator(getCatalogByCode(CatalogType.actObject, code)));


        Optional.ofNullable(reqAct154.getAct154SampleDetailsList()).ifPresent(reqDetailsList -> {
            List<Act154SampleDetails> updatedDetailsList = reqDetailsList.stream()
                    .map(detail -> Act154SampleDetails.builder()
                            .act154(existingAct)
                            .sampleName(detail.getSampleName())
                            .sampleType(detail.getSampleType())
                            .purposeOfSampling(detail.getPurposeOfSampling())
                            .sampleTakenDate(detail.getSampleTakenDate())
                            .shift(getCatalogByCode(CatalogType.shift,detail.getShiftCode()))
                            .laboratoryAddress(detail.getLaboratoryAddress())
                            .deliveryTerm(detail.getDeliveryTerm())
                            .storageCondition(detail.getStorageCondition())
                            .manufacturingCompany(detail.getManufacturingCompany())
                            .manufactureDate(detail.getManufactureDate())
                            .groupSize(detail.getGroupSize())
                            .serialNumberOfGroup(detail.getSerialNumberOfGroup())
                            .sampleWeight(detail.getSampleWeight())
                            .sampleVolume(detail.getSampleVolume())
                            .docNumberOfTakenObject(detail.getDocNumberOfTakenObject())
                            .packageType(detail.getPackageType())
                            .documentConfirmSampling(detail.getDocumentConfirmSampling())
                            .purposeOfChecking(detail.getPurposeOfChecking())
                            .note(detail.getNote())
                            .build()
                    ).toList();

            existingAct.getAct154SampleDetailsList().clear();
            existingAct.getAct154SampleDetailsList().addAll(updatedDetailsList);
        });

        Act154 updateAct154 = actRepository.save(existingAct);
        existingAct.setActStatus(ActStatus.IN_PROGRESS);
        return new ApiResponse<>(true, "Act154 updated successfully.", updateAct154);
    }
}
