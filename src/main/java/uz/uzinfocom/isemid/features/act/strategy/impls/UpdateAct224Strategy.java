package uz.uzinfocom.isemid.features.act.strategy.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.dtos.requests.act224.Act224Request;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.models.act224.Act224;
import uz.uzinfocom.isemid.features.act.repositores.ActRepository;
import uz.uzinfocom.isemid.features.act.strategy.UpdateActStrategy;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.Optional;

import static uz.uzinfocom.isemid.features.act.utils.ActUtil.validateAct;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateAct224Strategy implements UpdateActStrategy {

    private final ActRepository actRepository;

    @Override
    public ApiResponse<?> updateAct(long id, ActRequest reqAct, User currentUser) {
        Act act = validateAct(id, reqAct, actRepository);
        Act224 existingAct = (Act224) act;
        Act224Request reqAct224 = (Act224Request) reqAct;

        Optional.ofNullable(reqAct224.getTitle()).ifPresent(existingAct::setTitle);
        Optional.ofNullable(reqAct224.getDocumentSendDate()).ifPresent(existingAct::setDocumentSendDate);
        Optional.ofNullable(reqAct224.getRegionCode()).ifPresent(existingAct::setRegionCode);
        Optional.ofNullable(reqAct224.getFullNameOfEpidStaff()).ifPresent(existingAct::setFullNameOfEpidStaff);
        Optional.ofNullable(reqAct224.getNameOfInstitution()).ifPresent(existingAct::setNameOfInstitution);
        Optional.ofNullable(reqAct224.getAddressOfEpidStaff()).ifPresent(existingAct::setAddressOfEpidStaff);
        Optional.ofNullable(reqAct224.getNameOfRegulatoryActs()).ifPresent(existingAct::setNameOfRegulatoryActs);
        Optional.ofNullable(reqAct224.getCheckingFulfillmentOfRequirements()).ifPresent(existingAct::setCheckingFulfillmentOfRequirements);
        Optional.ofNullable(reqAct224.getFullNameOfParticipant()).ifPresent(existingAct::setFullNameOfParticipant);
        Optional.ofNullable(reqAct224.getRecommendedActivities()).ifPresent(existingAct::setRecommendedActivities);
        Optional.ofNullable(reqAct224.getExecutionPeriod()).ifPresent(existingAct::setExecutionPeriod);
        return new ApiResponse<>(true, MessageUtil.UPDATED_SUCCESS, null);
    }
}
