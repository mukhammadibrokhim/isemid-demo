package uz.uzinfocom.isemid.features.act.strategy.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.dtos.requests.act155.Act155DetailRequest;
import uz.uzinfocom.isemid.features.act.dtos.requests.act155.Act155Request;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.models.act155.Act155;
import uz.uzinfocom.isemid.features.act.models.act155.Act155SampleDetails;
import uz.uzinfocom.isemid.features.act.repositores.ActRepository;
import uz.uzinfocom.isemid.features.act.strategy.UpdateActStrategy;
import uz.uzinfocom.isemid.features.organization.services.OrganizationService;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static uz.uzinfocom.isemid.features.act.utils.ActUtil.validateAct;


@Service
@RequiredArgsConstructor
@Transactional
public class UpdateAct155Strategy implements UpdateActStrategy {
    private final ActRepository actRepository;
    private final OrganizationService organizationService;


    @Override
    public ApiResponse<?> updateAct(long id, ActRequest reqAct, User currentUser) {
        Act act = validateAct(id, reqAct, actRepository);

        Act155 existingAct = (Act155) act;
        Act155Request reqAct155 = (Act155Request) reqAct;

        Optional.ofNullable(reqAct155.getTitle()).ifPresent(existingAct::setTitle);
        Optional.ofNullable(reqAct155.getDocumentSendDate()).ifPresent(existingAct::setDocumentSendDate);
        Optional.ofNullable(reqAct155.getTin()).ifPresent(existingAct::setTin);
        Optional.ofNullable(reqAct155.getInstitutionName()).ifPresent(existingAct::setInstitutionName);
        Optional.ofNullable(reqAct155.getInstitutionAddress()).ifPresent(existingAct::setInstitutionAddress);
        Optional.ofNullable(reqAct155.getSelectedDate()).ifPresent(existingAct::setSelectedDate);
        Optional.ofNullable(reqAct155.getSamplerFullName()).ifPresent(existingAct::setSamplerFullName);
        Optional.ofNullable(reqAct155.getSamplerPosition()).ifPresent(existingAct::setSamplerPosition);
        Optional.ofNullable(reqAct155.getObjectRepresentativeFullName()).ifPresent(existingAct::setObjectRepresentativeFullName);
        Optional.ofNullable(reqAct155.getObjectRepresentativePosition()).ifPresent(existingAct::setObjectRepresentativePosition);
        Optional.ofNullable(reqAct155.getAdditionalInfo()).ifPresent(existingAct::setAdditionalInfo);

        Optional.ofNullable(reqAct155.getAct155Details()).ifPresent(reqAct155Details -> {
            Set<Act155SampleDetails> existingDetails = existingAct.getAct155SampleDetails();

            Set<Act155SampleDetails> updatedDetails = reqAct155Details.stream()
                    .map(reqDetail -> {
                        if (reqDetail.getId() != null) {
                            return existingDetails.stream()
                                    .filter(existingDetail -> existingDetail.getId().equals(reqDetail.getId()))
                                    .findFirst()
                                    .map(existingDetail -> updateExistingAct155Detail(existingDetail, reqDetail))
                                    .orElseGet(() -> createNewAct155Detail(reqDetail, existingAct));
                        } else {
                            return createNewAct155Detail(reqDetail, existingAct);
                        }
                    })
                    .collect(Collectors.toSet());

            existingAct.setAct155SampleDetails(updatedDetails);
        });

        return new ApiResponse<>(true, MessageUtil.UPDATED_SUCCESS, actRepository.save(act));
    }

    private Act155SampleDetails createNewAct155Detail(Act155DetailRequest reqDetail, Act155 existingAct) {
        return Act155SampleDetails.builder()
                .productName(reqDetail.getProductName())
                .purposeOfTesting(reqDetail.getPurposeOfTesting())
                .purposeOfTestingLoinc(reqDetail.getPurposeOfTestingLoinc())
                .laboratoryAddress(reqDetail.getLaboratoryAddress())
                .sampleTakenLocation(reqDetail.getSampleTakenLocation())
                .sampleQuantity(reqDetail.getSampleQuantity())
                .organization(organizationService.findById(reqDetail.getOrganizationId()).orElse(null))
                .productBatchQuantity(reqDetail.getProductBatchQuantity())
                .appliedPesticides(reqDetail.getAppliedPesticides())
                .manufacturer(reqDetail.getManufacturer())
                .sampleDocumentJustifying(reqDetail.getSampleDocumentJustifying()).
                build();
    }

    private Act155SampleDetails updateExistingAct155Detail(Act155SampleDetails existingDetail, Act155DetailRequest reqDetail) {

        Optional.ofNullable(reqDetail.getProductName()).ifPresent(existingDetail::setProductName);
        Optional.ofNullable(reqDetail.getPurposeOfTesting()).ifPresent(existingDetail::setPurposeOfTesting);
        Optional.ofNullable(reqDetail.getPurposeOfTestingLoinc()).ifPresent(existingDetail::setPurposeOfTestingLoinc);
        Optional.ofNullable(reqDetail.getLaboratoryAddress()).ifPresent(existingDetail::setLaboratoryAddress);
        Optional.ofNullable(reqDetail.getSampleTakenLocation()).ifPresent(existingDetail::setSampleTakenLocation);
        Optional.ofNullable(reqDetail.getSampleQuantity()).ifPresent(existingDetail::setSampleQuantity);
        Optional.ofNullable(reqDetail.getOrganizationId()).ifPresent(orgId -> {
            existingDetail.setOrganizationId(orgId);
            organizationService.findById(orgId).ifPresent(existingDetail::setOrganization);
        });
        Optional.ofNullable(reqDetail.getProductBatchQuantity()).ifPresent(existingDetail::setProductBatchQuantity);
        Optional.ofNullable(reqDetail.getAppliedPesticides()).ifPresent(existingDetail::setAppliedPesticides);
        Optional.ofNullable(reqDetail.getManufacturer()).ifPresent(existingDetail::setManufacturer);
        Optional.ofNullable(reqDetail.getSampleDocumentJustifying()).ifPresent(existingDetail::setSampleDocumentJustifying);
        return existingDetail;
    }
}
