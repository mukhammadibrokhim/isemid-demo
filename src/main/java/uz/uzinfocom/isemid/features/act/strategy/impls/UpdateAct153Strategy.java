package uz.uzinfocom.isemid.features.act.strategy.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.dtos.requests.act153.Act153DetailRequest;
import uz.uzinfocom.isemid.features.act.dtos.requests.act153.Act153Request;
import uz.uzinfocom.isemid.features.act.enums.ActStatus;
import uz.uzinfocom.isemid.features.act.mappers.ActMapper;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.models.act153.Act153;
import uz.uzinfocom.isemid.features.act.models.act153.Act153Detail;
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
public class UpdateAct153Strategy implements UpdateActStrategy {

    private final ActRepository actRepository;
    private final OrganizationService organizationService;
    private final ActMapper actMapper;


    @Override
    public ApiResponse<?> updateAct(long id, ActRequest reqAct, User currentUser) {
        Act act = validateAct(id, reqAct, actRepository);

        Act153 existingAct = (Act153) act;
        Act153Request reqAct153 = (Act153Request) reqAct;
        existingAct.setActStatus(ActStatus.IN_PROGRESS);

        Optional.ofNullable(reqAct153.getTin()).ifPresent(existingAct::setTin);
        Optional.ofNullable(reqAct153.getSampleObjectName()).ifPresent(existingAct::setSampleObjectName);
        Optional.ofNullable(reqAct153.getSamplingDocuments()).ifPresent(existingAct::setSamplingDocuments);
        Optional.ofNullable(reqAct153.getSamplerFullName()).ifPresent(existingAct::setSamplerFullName);
        Optional.ofNullable(reqAct153.getPosition()).ifPresent(existingAct::setPosition);
        Optional.ofNullable(reqAct153.getParticipantFullName()).ifPresent(existingAct::setParticipantFullName);
        Optional.ofNullable(reqAct153.getParticipantPosition()).ifPresent(existingAct::setParticipantPosition);
        Optional.ofNullable(reqAct153.getAdditionalInfo()).ifPresent(existingAct::setAdditionalInfo);
        Optional.ofNullable(reqAct153.getAct153Details()).ifPresent(reqAct153Details -> {
            Set<Act153Detail> existingDetails = existingAct.getAct153Details();

            Set<Act153Detail> updatedDetails = reqAct153Details.stream()
                    .map(reqDetail -> {
                        if (reqDetail.getId() != null) {
                            return existingDetails.stream()
                                    .filter(existingDetail -> existingDetail.getId().equals(reqDetail.getId()))
                                    .findFirst()
                                    .map(existingDetail -> updateExistingAct153Detail(existingDetail, reqDetail))
                                    .orElseGet(() -> createNewAct153Detail(reqDetail, existingAct));
                        } else {
                            return createNewAct153Detail(reqDetail, existingAct);
                        }
                    })
                    .collect(Collectors.toSet());

            existingAct.getAct153Details().addAll(updatedDetails);
        });

        return new ApiResponse<>(true, MessageUtil.UPDATED_SUCCESS, actMapper.toResponse(actRepository.save(act)));
    }

    private Act153Detail updateExistingAct153Detail(Act153Detail existingDetail, Act153DetailRequest reqDetail) {
        Optional.ofNullable(reqDetail.getObjectCode()).ifPresent(existingDetail::setObjectCode);
        Optional.ofNullable(reqDetail.getObjectCode()).ifPresent(existingDetail::setObjectCode);
        Optional.ofNullable(reqDetail.getSamplingPurpose()).ifPresent(existingDetail::setSamplingPurpose);
        Optional.ofNullable(reqDetail.getSamplingPurposeLoinc()).ifPresent(existingDetail::setSamplingPurposeLoinc);
        Optional.ofNullable(reqDetail.getAddress()).ifPresent(existingDetail::setAddress);
        Optional.ofNullable(reqDetail.getSamplingDepth()).ifPresent(existingDetail::setSamplingDepth);
        Optional.ofNullable(reqDetail.getDistanceFromShore()).ifPresent(existingDetail::setDistanceFromShore);
        Optional.ofNullable(reqDetail.getSampleVolume()).ifPresent(existingDetail::setSampleVolume);
        Optional.ofNullable(reqDetail.getSampleLocation()).ifPresent(existingDetail::setSampleLocation);
        Optional.ofNullable(reqDetail.getWeatherAtSampling()).ifPresent(existingDetail::setWeatherAtSampling);
        Optional.ofNullable(reqDetail.getWaterTemperature()).ifPresent(existingDetail::setWaterTemperature);
        Optional.ofNullable(reqDetail.getSampleType()).ifPresent(existingDetail::setSampleType);
        Optional.ofNullable(reqDetail.getSamplingDate()).ifPresent(existingDetail::setSamplingDate);
        Optional.ofNullable(reqDetail.getSpecialSamplingConditions()).ifPresent(existingDetail::setSpecialSamplingConditions);
        Optional.ofNullable(reqDetail.getOrganizationId()).ifPresent(orgId -> {
            existingDetail.setOrganizationId(orgId);
            organizationService.findById(orgId).ifPresent(existingDetail::setOrganization);
        });
        Optional.ofNullable(reqDetail.getLaboratoryAddress()).ifPresent(existingDetail::setLaboratoryAddress);
        Optional.ofNullable(reqDetail.getDeliveryConditions()).ifPresent(existingDetail::setDeliveryConditions);
        Optional.ofNullable(reqDetail.getStorageConditions()).ifPresent(existingDetail::setStorageConditions);
        Optional.ofNullable(reqDetail.getConservationMethods()).ifPresent(existingDetail::setConservationMethods);
        Optional.ofNullable(reqDetail.getAdditionalInfo()).ifPresent(existingDetail::setAdditionalInfo);
        return existingDetail;
    }

    private Act153Detail createNewAct153Detail(Act153DetailRequest reqDetail, Act153 existingAct) {
        return Act153Detail.builder()
                .act153(existingAct)
                .objectCode(reqDetail.getObjectCode())
                .samplingPurpose(reqDetail.getSamplingPurpose())
                .samplingPurposeLoinc(reqDetail.getSamplingPurposeLoinc())
                .address(reqDetail.getAddress())
                .samplingDepth(reqDetail.getSamplingDepth())
                .distanceFromShore(reqDetail.getDistanceFromShore())
                .sampleVolume(reqDetail.getSampleVolume())
                .sampleLocation(reqDetail.getSampleLocation())
                .weatherAtSampling(reqDetail.getWeatherAtSampling())
                .waterTemperature(reqDetail.getWaterTemperature())
                .sampleType(reqDetail.getSampleType())
                .samplingDate(reqDetail.getSamplingDate())
                .specialSamplingConditions(reqDetail.getSpecialSamplingConditions())
                .organization(organizationService.findById(reqDetail.getOrganizationId()).orElse(null))
                .organizationId(reqDetail.getOrganizationId())
                .laboratoryAddress(reqDetail.getLaboratoryAddress())
                .deliveryConditions(reqDetail.getDeliveryConditions())
                .storageConditions(reqDetail.getStorageConditions())
                .conservationMethods(reqDetail.getConservationMethods())
                .additionalInfo(reqDetail.getAdditionalInfo())
                .build();
    }
}
