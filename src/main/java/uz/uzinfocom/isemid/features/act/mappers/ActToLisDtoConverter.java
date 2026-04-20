package uz.uzinfocom.isemid.features.act.mappers;

import static uz.uzinfocom.isemid.commons.constants.ApiPaths.ExternalApi.LIS_RESEARCH_TYPE_API;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.commons.utils.RestClientUtil;
import uz.uzinfocom.isemid.features.act.dtos.ActLisDto;
import uz.uzinfocom.isemid.features.act.dtos.ActLisDto.SelectionActItemDto;
import uz.uzinfocom.isemid.features.act.dtos.LisActPushRequest.LisDataDictionary;
import uz.uzinfocom.isemid.features.act.enums.ActType;
import uz.uzinfocom.isemid.features.act.enums.LisResearchCode;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.models.act153.Act153;
import uz.uzinfocom.isemid.features.act.models.act153.Act153Detail;
import uz.uzinfocom.isemid.features.act.models.act154.Act154;
import uz.uzinfocom.isemid.features.act.models.act154.Act154Detail;
import uz.uzinfocom.isemid.features.act.models.act223.Act223;
import uz.uzinfocom.isemid.features.act.models.act223.Act223Detail;

@Component
@RequiredArgsConstructor
public class ActToLisDtoConverter {

    private final MessageUtil messageUtil;

    public ActLisDto convert(Act act) {
        return switch (act.getType()) {
            case ACT153 -> fromAct153((Act153) act);
            case ACT154 -> fromAct154((Act154) act);
            case ACT223 -> fromAct223((Act223) act);
            default -> throw new IllegalArgumentException(
                "LIS mapping is not supported for act type: " + act.getType());
        };
    }

    // -------------------------------------------------------------------------
    // Act153
    // -------------------------------------------------------------------------

    private ActLisDto fromAct153(Act153 act) {
        Set<Act153Detail> details = act.getAct153Details();
        Act153Detail first = details.stream().findFirst().orElse(null);

        LisResearchCode lisResearchCode = ActType.mapToLisResearchCode(act.getType());
        String url = LIS_RESEARCH_TYPE_API.formatted(lisResearchCode);

        Integer actTemplateId = RestClientUtil.makeRequest(url, HttpMethod.GET, "", null, null, Integer.class, messageUtil);

        return new ActLisDto(
            actTemplateId,
            act.getTin() != null ? act.getTin().toString() : null,
            act.getInstitutionName(),
            act.getInstitutionAddress(),
            first != null ? new LisDataDictionary(first.getPurposeId(), first.getSamplingPurposeUz(),first.getSamplingPurposeRu()) : null,
            first != null ? first.getSamplingDateTime() : null,
            act.getDeliveredDateTime(),
            first != null ? new LisDataDictionary(first.getSpecialConditionId(), first.getSpecialSamplingConditionsUz(),first.getSpecialSamplingConditionsRu()) : null,
            act.getAdditionalInfo(),
            act.getParticipantFullName(),
            act.getParticipantPositionId(),
            null,
            act.getSamplingDocuments(),
            null,
            first != null ? new LisDataDictionary(first.getStorageConditionId(), first.getStorageConditionsUz(),first.getSpecialSamplingConditionsRu()) : null,
            act.getSamplerPositionId(),
            null,
            null,
            null,
            first != null ? new LisDataDictionary(first.getConservationMethodId(), first.getConservationMethodsUz(),first.getConservationMethodsRu()) : null,
            details.stream().map(this::toSelectionItemFromAct153).toList()
        );
    }

    private SelectionActItemDto toSelectionItemFromAct153(Act153Detail d) {
        return new SelectionActItemDto(
            d.getItemTypeId(),
            null,
            null,
            null,
            d.getAddress(),
            d.getSamplingDepth() != null ? d.getSamplingDepth().toString() : null,
            d.getDepthUnit(),
            d.getDistanceFromShore() != null ? d.getDistanceFromShore().toString() : null,
            d.getDistanceFromShoreUnit(),
            d.getWeatherAtSampling() != null ? d.getWeatherAtSampling().toString() : null,
            d.getWaterTemperature() != null ? d.getWaterTemperature().toString() : null,
            null,
            d.getSampleTypeId() != null || d.getSampleTypeUz() != null || d.getSampleTypeRu() != null
                ? new LisDataDictionary(d.getSampleTypeId(), d.getSampleTypeUz(),
                d.getSampleTypeRu()) : null,
            null,
            null
        );
    }

    // -------------------------------------------------------------------------
    // Act154
    // -------------------------------------------------------------------------

    private ActLisDto fromAct154(Act154 act) {
        Set<Act154Detail> details = act.getAct154Details();
        Act154Detail first = details.stream().findFirst().orElse(null);

        LisResearchCode lisResearchCode = ActType.mapToLisResearchCode(act.getType());
        String url = LIS_RESEARCH_TYPE_API.formatted(lisResearchCode);

        Integer actTemplateId = RestClientUtil.makeRequest(url, HttpMethod.GET, "", null, null, Integer.class, messageUtil);

        return new ActLisDto(
            actTemplateId,
            act.getTin() != null ? act.getTin().toString() : null,
            act.getInstitutionName(),
            act.getInstitutionAddress(),
            first != null ? new LisDataDictionary(first.getSamplingPurposeId(), first.getPurposeOfSamplingUz(),first.getPurposeOfSamplingRu()) : null,
            first != null ? first.getSampleTakenDateTime() : null,
            act.getDeliveredDateTime(),
            first != null ? new LisDataDictionary(first.getDeliveryConditionId(), first.getDeliveryConditionUz(),first.getDeliveryConditionRu()) : null,
            act.getAdditionalInfo(),
            act.getParticipantFullName(),
            act.getParticipantPositionId(),
            first != null ? new LisDataDictionary(first.getPackageTypeId(), first.getPackageTypeUz(),first.getPackageTypeRu()) : null,
            null,
            null,
            first != null ? new LisDataDictionary(first.getStorageConditionId(), first.getStorageConditionUz(),first.getStorageConditionRu()) : null,
            act.getSamplerPositionId(),
            first != null ? first.getManufacturingCompany() : null,
            first != null && first.getManufactureDate() != null
                ? first.getManufactureDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null,
            first != null && first.getDocNumberOfTakenObject() != null
                ? first.getDocNumberOfTakenObject().toString() : null,
            null,
            details.stream().map(this::toSelectionItemFromAct154).toList()
        );
    }

    private SelectionActItemDto toSelectionItemFromAct154(Act154Detail d) {
        return new SelectionActItemDto(
            d.getItemTypeId(),
            null,
            d.getGroupSize() != null ? d.getGroupSize().toString() : null,
            d.getSampleWeight() != null ? d.getSampleWeight().toString() : null,
            d.getLaboratoryAddress(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null

        );
    }

    // -------------------------------------------------------------------------
    // Act223
    // -------------------------------------------------------------------------

    private ActLisDto fromAct223(Act223 act) {
        List<Act223Detail> details = new ArrayList<>(act.getAct223Details());
        Act223Detail first = details.isEmpty() ? null : details.getFirst();

        LisResearchCode lisResearchCode = ActType.mapToLisResearchCode(act.getType());
        String url = LIS_RESEARCH_TYPE_API.formatted(lisResearchCode);

        Integer actTemplateId = RestClientUtil.makeRequest(url, HttpMethod.GET, "", null, null, Integer.class, messageUtil);

        return new ActLisDto(
            actTemplateId,
            act.getTin() != null ? act.getTin().toString() : null,
            act.getInstitutionName(),
            act.getInstitutionAddress(),
            null,
            act.getSampleTakenDateTime(),
            act.getDeliveredDateTime(),
            first != null ? new LisDataDictionary(first.getDeliveryConditionId(), first.getDeliveryConditionsUz(),first.getDeliveryConditionsRu()) : null,
            act.getAdditionalInfo(),
            act.getParticipantFullName(),
            act.getParticipantPositionId(),
            null,
            null,
            null,
            first != null ? new LisDataDictionary(first.getStorageConditionId(), first.getStorageConditionsUz(),first.getStorageConditionsRu()) : null,
            act.getSamplerPositionId(),
            null,
            null,
            null,
            null,
            details.stream().map(this::toSelectionItemFromAct223).toList()
        );
    }

    private SelectionActItemDto toSelectionItemFromAct223(Act223Detail d) {
        return new SelectionActItemDto(
            d.getItemTypeId(),
            d.getExactLocationPointSampling(),
            null,
            null,
            null,
            d.getDepthOfObtainedArea() != null ? d.getDepthOfObtainedArea().toString() : null,
            d.getDepthUnit(),
            null,
            null,
            null,
            null,
            null,
            new LisDataDictionary(d.getSampleTypeId(), d.getSampleTypeUz(),d.getSampleTypeRu()),
            d.getAmount() != null ? d.getAmount().toString() : null,
            null
        );
    }
}
