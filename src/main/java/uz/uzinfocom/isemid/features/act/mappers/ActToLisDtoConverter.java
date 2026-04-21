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
            act.getInstitution().getTin() != null ? act.getInstitution().getTin().toString() : null,
            act.getInstitution().getInstitutionName(),
            act.getInstitution().getInstitutionAddress(),
            act.getInstitution().getInstitutionLegalAddress(),
            new LisDataDictionary(act.getPurpose().getPurposeId(),act.getPurpose().getSamplingPurposeUz(),act.getPurpose().getSamplingPurposeRu()),
            act.getSampleTakenDateTime(),
            act.getDeliveredDateTime(),
            new LisDataDictionary(act.getSpecialCondition().getConditionId(), act.getSpecialCondition().getDescription().getUz(),act.getSpecialCondition().getDescription().getUz()),
            act.getAdditionalInfo(),
            act.getParticipant().getFullName(),
            act.getParticipant().getPositionId(),
            new LisDataDictionary(act.getPackageTypeInfo().getPackageTypeId(),act.getPackageTypeInfo().getPackageTypeUz(),act.getPackageTypeInfo().getPackageTypeRu()),
            act.getSamplingDocuments(),
            null,
            new LisDataDictionary(act.getSpecialCondition().getConditionId(),act.getSpecialCondition().getDescription().getUz(),act.getSpecialCondition().getDescription().getRu()),
            act.getSampler().getPositionId(),
            null,
            null,
            null,
            first != null ? new LisDataDictionary(first.getConservationTypeInfo().getConservationMethodId(), first.getConservationTypeInfo().getConservationMethodsUz(),first.getConservationTypeInfo().getConservationMethodsRu()) : null,
            details.stream().map(this::toSelectionItemFromAct153).toList()
        );
    }

    private SelectionActItemDto toSelectionItemFromAct153(Act153Detail d) {
        return new SelectionActItemDto(
            d.getResearchItemTypeInfo().getItemTypeId(),
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
            d.getSampleTypeInfo().getSampleTypeId() != null || d.getSampleTypeInfo().getSampleTypeUz() != null || d.getSampleTypeInfo().getSampleTypeRu() != null
                ? new LisDataDictionary(d.getSampleTypeInfo().getSampleTypeId(), d.getSampleTypeInfo().getSampleTypeUz(),
                d.getSampleTypeInfo().getSampleTypeRu()) : null,
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
            act.getInstitution().getTin() != null ? act.getInstitution().getTin().toString() : null,
            act.getInstitution().getInstitutionName(),
            act.getInstitution().getInstitutionAddress(),
            act.getInstitution().getInstitutionLegalAddress(),
            new LisDataDictionary(act.getPurpose().getPurposeId(),act.getPurpose().getSamplingPurposeUz(),act.getPurpose().getSamplingPurposeRu()),
            act.getSampleTakenDateTime(),
            act.getDeliveredDateTime(),
            new LisDataDictionary(act.getSpecialCondition().getConditionId(), act.getSpecialCondition().getDescription().getUz(),act.getSpecialCondition().getDescription().getUz()),
            act.getAdditionalInfo(),
            act.getParticipant().getFullName(),
            act.getParticipant().getPositionId(),
            new LisDataDictionary(act.getPackageTypeInfo().getPackageTypeId(),act.getPackageTypeInfo().getPackageTypeUz(),act.getPackageTypeInfo().getPackageTypeRu()),
            null,
            null,
            new LisDataDictionary(act.getSpecialCondition().getConditionId(),act.getSpecialCondition().getDescription().getUz(),act.getSpecialCondition().getDescription().getRu()),
            act.getSampler().getPositionId(),
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
            d.getResearchItemTypeInfo().getItemTypeId(),
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
            d.getSampleTypeInfo().getSampleTypeId() != null || d.getSampleTypeInfo().getSampleTypeUz() != null || d.getSampleTypeInfo().getSampleTypeRu() != null
                ? new LisDataDictionary(d.getSampleTypeInfo().getSampleTypeId(), d.getSampleTypeInfo().getSampleTypeUz(),
                d.getSampleTypeInfo().getSampleTypeRu()) : null,
            null,
            null

        );
    }

    // -------------------------------------------------------------------------
    // Act223
    // -------------------------------------------------------------------------

    private ActLisDto fromAct223(Act223 act) {
        List<Act223Detail> details = new ArrayList<>(act.getAct223Details());

        LisResearchCode lisResearchCode = ActType.mapToLisResearchCode(act.getType());
        String url = LIS_RESEARCH_TYPE_API.formatted(lisResearchCode);

        Integer actTemplateId = RestClientUtil.makeRequest(url, HttpMethod.GET, "", null, null, Integer.class, messageUtil);

        return new ActLisDto(
            actTemplateId,
            act.getInstitution().getTin() != null ? act.getInstitution().getTin().toString() : null,
            act.getInstitution().getInstitutionName(),
            act.getInstitution().getInstitutionAddress(),
            act.getInstitution().getInstitutionLegalAddress(),
            new LisDataDictionary(act.getPurpose().getPurposeId(),act.getPurpose().getSamplingPurposeUz(),act.getPurpose().getSamplingPurposeRu()),
            act.getSampleTakenDateTime(),
            act.getDeliveredDateTime(),
            new LisDataDictionary(act.getSpecialCondition().getConditionId(), act.getSpecialCondition().getDescription().getUz(),act.getSpecialCondition().getDescription().getUz()),
            act.getAdditionalInfo(),
            act.getParticipant().getFullName(),
            act.getParticipant().getPositionId(),
            new LisDataDictionary(act.getPackageTypeInfo().getPackageTypeId(),act.getPackageTypeInfo().getPackageTypeUz(),act.getPackageTypeInfo().getPackageTypeRu()),
            null,
            null,
            new LisDataDictionary(act.getSpecialCondition().getConditionId(),act.getSpecialCondition().getDescription().getUz(),act.getSpecialCondition().getDescription().getRu()),
            act.getSampler().getPositionId(),
            null,
            null,
            null,
            null,
            details.stream().map(this::toSelectionItemFromAct223).toList()
        );
    }

    private SelectionActItemDto toSelectionItemFromAct223(Act223Detail d) {
        return new SelectionActItemDto(
            d.getResearchItemTypeInfo().getItemTypeId(),
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
            d.getSampleTypeInfo().getSampleTypeId() != null || d.getSampleTypeInfo().getSampleTypeUz() != null || d.getSampleTypeInfo().getSampleTypeRu() != null
                ? new LisDataDictionary(d.getSampleTypeInfo().getSampleTypeId(), d.getSampleTypeInfo().getSampleTypeUz(),
                d.getSampleTypeInfo().getSampleTypeRu()) : null,
            d.getAmount() != null ? d.getAmount().toString() : null,
            null
        );
    }
}
