package uz.uzinfocom.isemid.services.impls.act.strategy;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.act.Act;
import uz.uzinfocom.isemid.models.entities.act.act154.Act154;
import uz.uzinfocom.isemid.models.entities.act.act154.Act154SampleDetails;
import uz.uzinfocom.isemid.models.entities.enums.CatalogType;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;
import uz.uzinfocom.isemid.models.payload.requests.act.ReqAct;
import uz.uzinfocom.isemid.models.payload.requests.act.act154.ReqAct154;
import uz.uzinfocom.isemid.models.payload.requests.act.act154.ReqAct154Detail;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.repositories.act.ActRepository;
import uz.uzinfocom.isemid.services.OrganizationService;
import uz.uzinfocom.isemid.services.act.strategy.UpdateActStrategy;
import uz.uzinfocom.isemid.services.value_set.CatalogService;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uz.uzinfocom.isemid.models.entities.enums.CatalogType.shift;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateAct154Strategy implements UpdateActStrategy {
    final ActRepository actRepository;
    final CatalogService catalogService;
    final OrganizationService organizationService;

    private Catalog getCatalogByCode(CatalogType type, String code) throws ApiException {
        return catalogService.findByTypeAndCode(type, code).orElse(null);
    }


    @Override
    public ApiResponse<?> updateAct(long id, ReqAct reqAct, User currentUser) {

        Act act = actRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "Act154 not found."));

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

        Optional.ofNullable(reqAct154.getAct154SampleDetailsList()).ifPresent(reqAct154Details -> {
            Set<Act154SampleDetails> existingDetails = existingAct.getAct154Details();

            Set<Act154SampleDetails> updatedDetails = reqAct154Details.stream()
                    .map(reqDetail -> {
                        if (reqDetail.getId() != null) {
                            return existingDetails.stream()
                                    .filter(existingDetail -> existingDetail.getId().equals(reqDetail.getId()))
                                    .findFirst()
                                    .map(existingDetail -> updateExistingAct154Detail(existingDetail, reqDetail))
                                    .orElseGet(() -> createNewAct154Detail(reqDetail, existingAct));
                        } else {
                            return createNewAct154Detail(reqDetail, existingAct);
                        }
                    })
                    .collect(Collectors.toSet());

            existingAct.setAct154Details(updatedDetails);
        });

        return new ApiResponse<>(true, "Success", actRepository.save(act));
    }

    private Act154SampleDetails createNewAct154Detail(ReqAct154Detail reqAct154Detail, Act154 existingAct) {
        return Act154SampleDetails.builder()
                .shift(getCatalogByCode(shift, reqAct154Detail.getShiftCode()))
                .shiftCode(reqAct154Detail.getShiftCode())
                .sampleName(reqAct154Detail.getSampleName())
                .sampleType(reqAct154Detail.getSampleType())
                .purposeOfSampling(reqAct154Detail.getPurposeOfSampling())
                .samplingPurposeLoinc(reqAct154Detail.getSamplingPurposeLoinc())
                .sampleTakenDate(reqAct154Detail.getSampleTakenDate())
                .organization(organizationService.findById(reqAct154Detail.getOrganizationId()).orElse(null))
                .organizationId(reqAct154Detail.getOrganizationId())
                .laboratoryAddress(reqAct154Detail.getLaboratoryAddress())
                .deliveryTerm(reqAct154Detail.getDeliveryTerm())
                .storageCondition(reqAct154Detail.getStorageCondition())
                .manufacturingCompany(reqAct154Detail.getManufacturingCompany())
                .manufactureDate(reqAct154Detail.getManufactureDate())
                .groupSize(reqAct154Detail.getGroupSize())
                .serialNumberOfGroup(reqAct154Detail.getSerialNumberOfGroup())
                .sampleWeight(reqAct154Detail.getSampleWeight())
                .sampleVolume(reqAct154Detail.getSampleVolume())
                .docNumberOfTakenObject(reqAct154Detail.getDocNumberOfTakenObject())
                .packageType(reqAct154Detail.getPackageType())
                .documentConfirmSampling(reqAct154Detail.getDocumentConfirmSampling())
                .purposeOfChecking(reqAct154Detail.getPurposeOfChecking())
                .note(reqAct154Detail.getNote())
                .build();
    }

    private Act154SampleDetails updateExistingAct154Detail(Act154SampleDetails existingDetail, ReqAct154Detail reqDetail) {
        Optional.ofNullable(reqDetail.getShiftCode()).ifPresent(shiftCode -> {
            existingDetail.setShiftCode(shiftCode);
            getCatalogByCode(shift, shiftCode);
        });
        Optional.ofNullable(reqDetail.getOrganizationId()).ifPresent(orgId -> {
            existingDetail.setOrganizationId(orgId);
            organizationService.findById(orgId).ifPresent(existingDetail::setOrganization);
        });
        Optional.ofNullable(reqDetail.getSampleName()).ifPresent(existingDetail::setSampleName);
        Optional.ofNullable(reqDetail.getSampleType()).ifPresent(existingDetail::setSampleType);
        Optional.ofNullable(reqDetail.getPurposeOfSampling()).ifPresent(existingDetail::setPurposeOfSampling);
        Optional.ofNullable(reqDetail.getSamplingPurposeLoinc()).ifPresent(existingDetail::setSamplingPurposeLoinc);
        Optional.ofNullable(reqDetail.getSampleTakenDate()).ifPresent(existingDetail::setSampleTakenDate);
        Optional.ofNullable(reqDetail.getShiftCode()).ifPresent(existingDetail::setShiftCode);
        Optional.ofNullable(reqDetail.getOrganizationId()).ifPresent(existingDetail::setOrganizationId);
        Optional.ofNullable(reqDetail.getLaboratoryAddress()).ifPresent(existingDetail::setLaboratoryAddress);
        Optional.ofNullable(reqDetail.getDeliveryTerm()).ifPresent(existingDetail::setDeliveryTerm);
        Optional.ofNullable(reqDetail.getStorageCondition()).ifPresent(existingDetail::setStorageCondition);
        Optional.ofNullable(reqDetail.getManufacturingCompany()).ifPresent(existingDetail::setManufacturingCompany);
        Optional.ofNullable(reqDetail.getManufactureDate()).ifPresent(existingDetail::setManufactureDate);
        Optional.ofNullable(reqDetail.getGroupSize()).ifPresent(existingDetail::setGroupSize);
        Optional.ofNullable(reqDetail.getSerialNumberOfGroup()).ifPresent(existingDetail::setSerialNumberOfGroup);
        Optional.ofNullable(reqDetail.getSampleWeight()).ifPresent(existingDetail::setSampleWeight);
        Optional.ofNullable(reqDetail.getSampleVolume()).ifPresent(existingDetail::setSampleVolume);
        Optional.ofNullable(reqDetail.getDocNumberOfTakenObject()).ifPresent(existingDetail::setDocNumberOfTakenObject);
        Optional.ofNullable(reqDetail.getPackageType()).ifPresent(existingDetail::setPackageType);
        Optional.ofNullable(reqDetail.getDocumentConfirmSampling()).ifPresent(existingDetail::setDocumentConfirmSampling);
        Optional.ofNullable(reqDetail.getPurposeOfChecking()).ifPresent(existingDetail::setPurposeOfChecking);
        Optional.ofNullable(reqDetail.getNote()).ifPresent(existingDetail::setNote);
        return existingDetail;
    }
}
