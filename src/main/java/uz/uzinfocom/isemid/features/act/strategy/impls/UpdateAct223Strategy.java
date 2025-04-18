package uz.uzinfocom.isemid.features.act.strategy.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.dtos.requests.act223.Act223DetailRequest;
import uz.uzinfocom.isemid.features.act.dtos.requests.act223.Act223Request;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.models.act223.Act223;
import uz.uzinfocom.isemid.features.act.models.act223.Act223Detail;
import uz.uzinfocom.isemid.features.act.repositores.ActRepository;
import uz.uzinfocom.isemid.features.act.strategy.UpdateActStrategy;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static uz.uzinfocom.isemid.features.act.utils.ActUtil.validateAct;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateAct223Strategy implements UpdateActStrategy {

    private final ActRepository actRepository;

    @Override
    public ApiResponse<?> updateAct(long id, ActRequest reqAct, User currentUser) {
        Act act = validateAct(id, reqAct, actRepository);

        Act223 existingAct = (Act223) act;
        Act223Request reqAct223 = (Act223Request) reqAct;

        Optional.ofNullable(reqAct223.getTitle()).ifPresent(existingAct::setTitle);
        Optional.ofNullable(reqAct223.getDocumentSendDate()).ifPresent(existingAct::setDocumentSendDate);
        Optional.ofNullable(reqAct223.getTin()).ifPresent(existingAct::setTin);
        Optional.ofNullable(reqAct223.getInstitutionName()).ifPresent(existingAct::setInstitutionName);
        Optional.ofNullable(reqAct223.getInstitutionAddress()).ifPresent(existingAct::setInstitutionAddress);
        Optional.ofNullable(reqAct223.getFullNameofSampler()).ifPresent(existingAct::setFullNameofSampler);
        Optional.ofNullable(reqAct223.getPositionOfSampler()).ifPresent(existingAct::setPositionOfSampler);
        Optional.ofNullable(reqAct223.getFullNameOfParticipant()).ifPresent(existingAct::setFullNameOfParticipant);
        Optional.ofNullable(reqAct223.getPositionOfParticipant()).ifPresent(existingAct::setPositionOfParticipant);
        Optional.ofNullable(reqAct223.getSampleTakenDate()).ifPresent(existingAct::setSampleTakenDate);
        Optional.ofNullable(reqAct223.getReasonOfInspectoring()).ifPresent(existingAct::setReasonOfInspectoring);
        Optional.ofNullable(reqAct223.getReasonInspectoringLoinc()).ifPresent(existingAct::setReasonInspectoringLoinc);
        Optional.ofNullable(reqAct223.getAct223Details()).ifPresent(reqAct223Details -> {
            Set<Act223Detail> existingDetails = existingAct.getAct223Details();

            Set<Act223Detail> updatedDetails = reqAct223Details.stream()
                    .map(reqDetail -> {
                        if (reqDetail.getId() != null) {
                            return existingDetails.stream()
                                    .filter(existingDetail -> existingDetail.getId().equals(reqDetail.getId()))
                                    .findFirst()
                                    .map(existingDetail -> updateAct223Detail(existingDetail, reqDetail))
                                    .orElseGet(() -> createNewAct223Detail(reqDetail, existingAct));
                        } else {
                            return createNewAct223Detail(reqDetail, existingAct);
                        }
                    })
                    .collect(Collectors.toSet());

            existingAct.setAct223Details(updatedDetails);
        });
        return new ApiResponse<>(true, MessageUtil.UPDATED_SUCCESS, null);
    }

    private Act223Detail createNewAct223Detail(Act223DetailRequest reqDetail, Act223 existingAct) {
        return Act223Detail.builder()
                .exactLocationPointSampling(reqDetail.getExactLocationPointSampling())
                .amount(reqDetail.getAmount())
                .supportingDocumentsForSampling(reqDetail.getSupportingDocumentsForSampling())
                .depthOfObtainedArea(reqDetail.getDepthOfObtainedArea())
                .build();

    }

    private Act223Detail updateAct223Detail(Act223Detail existingDetail, Act223DetailRequest reqDetail) {
        Optional.ofNullable(reqDetail.getExactLocationPointSampling()).ifPresent(existingDetail::setExactLocationPointSampling);
        Optional.ofNullable(reqDetail.getAmount()).ifPresent(existingDetail::setAmount);
        Optional.ofNullable(reqDetail.getSupportingDocumentsForSampling()).ifPresent(existingDetail::setSupportingDocumentsForSampling);
        Optional.ofNullable(reqDetail.getDepthOfObtainedArea()).ifPresent(existingDetail::setDepthOfObtainedArea);
        return existingDetail;
    }
}
