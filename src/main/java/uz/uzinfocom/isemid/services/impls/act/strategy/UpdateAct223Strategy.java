package uz.uzinfocom.isemid.services.impls.act.strategy;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.act.Act;
import uz.uzinfocom.isemid.models.entities.act.act223.Act223;
import uz.uzinfocom.isemid.models.entities.act.act223.Act223Detail;
import uz.uzinfocom.isemid.models.payload.requests.act.ReqAct;
import uz.uzinfocom.isemid.models.payload.requests.act.act223.ReqAct223;
import uz.uzinfocom.isemid.models.payload.requests.act.act223.ReqAct223Detail;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.repositories.act.ActRepository;
import uz.uzinfocom.isemid.services.act.strategy.UpdateActStrategy;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateAct223Strategy implements UpdateActStrategy {

    private final ActRepository actRepository;

    @Override
    public ApiResponse<?> updateAct(long id, ReqAct reqAct, User currentUser) {
        Act act = actRepository.findById(id)
                .orElseThrow(() -> new ApiException(NOT_FOUND, "Act223 not found."));

        if (reqAct.getActType() == null || !reqAct.getActType().equals(act.getActType())) {
            throw new ApiException(BAD_REQUEST, "Invalid act type: expected " + act.getActType() + " but received " + reqAct.getActType());
        }
        Act223 existingAct = (Act223) act;
        ReqAct223 reqAct223 = (ReqAct223) reqAct;

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
        return new ApiResponse<>(true, "Success", null);
    }

    private Act223Detail createNewAct223Detail(ReqAct223Detail reqDetail, Act223 existingAct) {
        return Act223Detail.builder()
                .exactLocationPointSampling(reqDetail.getExactLocationPointSampling())
                .amount(reqDetail.getAmount())
                .supportingDocumentsForSampling(reqDetail.getSupportingDocumentsForSampling())
                .depthOfObtainedArea(reqDetail.getDepthOfObtainedArea())
                .build();

    }

    private Act223Detail updateAct223Detail(Act223Detail existingDetail, ReqAct223Detail reqDetail) {
        Optional.ofNullable(reqDetail.getExactLocationPointSampling()).ifPresent(existingDetail::setExactLocationPointSampling);
        Optional.ofNullable(reqDetail.getAmount()).ifPresent(existingDetail::setAmount);
        Optional.ofNullable(reqDetail.getSupportingDocumentsForSampling()).ifPresent(existingDetail::setSupportingDocumentsForSampling);
        Optional.ofNullable(reqDetail.getDepthOfObtainedArea()).ifPresent(existingDetail::setDepthOfObtainedArea);
        return existingDetail;
    }
}
