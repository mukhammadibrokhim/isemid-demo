package uz.uzinfocom.isemid.features.act.strategy.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.dtos.requests.act154.Act154DetailRequest;
import uz.uzinfocom.isemid.features.act.dtos.requests.act154.Act154Request;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.models.act154.Act154;
import uz.uzinfocom.isemid.features.act.models.act154.Act154SampleDetails;
import uz.uzinfocom.isemid.features.act.repositores.ActRepository;
import uz.uzinfocom.isemid.features.act.strategy.UpdateActStrategy;
import uz.uzinfocom.isemid.features.organization.services.OrganizationService;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.List;
import java.util.Optional;

import static uz.uzinfocom.isemid.features.act.utils.ActUtil.validateAct;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateAct154Strategy implements UpdateActStrategy {
    private final ActRepository actRepository;
    private final OrganizationService organizationService;


    @Override
    public ApiResponse<?> updateAct(long id, ActRequest reqAct, User currentUser) {

        Act act = validateAct(id, reqAct, actRepository);

        Act154 existingAct = (Act154) act;

        Act154Request reqAct154 = (Act154Request) reqAct;

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
            List<Act154SampleDetails> existingDetails = existingAct.getAct154SampleDetails();

            List<Act154SampleDetails> updatedDetails = reqAct154Details.stream()
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
                    }).toList();

            existingAct.setAct154SampleDetails(updatedDetails);
        });

        return new ApiResponse<>(true, MessageUtil.UPDATED_SUCCESS, actRepository.save(act));
    }

    private Act154SampleDetails createNewAct154Detail(Act154DetailRequest reqAct154Detail, Act154 existingAct) {
        return Act154SampleDetails.builder()
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

    private Act154SampleDetails updateExistingAct154Detail(Act154SampleDetails existingDetail, Act154DetailRequest reqDetail) {
        Optional.ofNullable(reqDetail.getShiftCode()).ifPresent(existingDetail::setShiftCode);
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
