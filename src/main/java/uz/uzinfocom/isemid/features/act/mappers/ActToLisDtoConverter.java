package uz.uzinfocom.isemid.features.act.mappers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import uz.uzinfocom.isemid.commons.integration.IntegrationUrlFactory;
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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class ActToLisDtoConverter {

    private final RestClient integrationRestClient;
    private final IntegrationUrlFactory integrationUrlFactory;

    public ActToLisDtoConverter(
            @Qualifier("integrationRestClient") RestClient integrationRestClient,
            IntegrationUrlFactory integrationUrlFactory
    ) {
        this.integrationRestClient = integrationRestClient;
        this.integrationUrlFactory = integrationUrlFactory;
    }

    public ActLisDto convert(Act act) {
        return switch (act.getType()) {
            case ACT153 -> fromAct153((Act153) act);
            case ACT154 -> fromAct154((Act154) act);
            case ACT223 -> fromAct223((Act223) act);
            default -> throw new IllegalArgumentException(
                    "LIS mapping is not supported for act type: " + act.getType()
            );
        };
    }

    private ActLisDto fromAct153(Act153 act) {
        Set<Act153Detail> details = act.getAct153Details();
        Integer actTemplateId = resolveLisActTemplateId(act.getType());

        return new ActLisDto(
                actTemplateId,
                act.getInstitution().getTin() != null ? act.getInstitution().getTin().toString() : null,
                act.getInstitution().getInstitutionName(),
                act.getInstitution().getInstitutionAddress(),
                act.getInstitution().getInstitutionLegalAddress(),
                new LisDataDictionary(act.getPurpose().getPurposeId(), act.getPurpose().getSamplingPurposeUz()),
                act.getSampleTakenDateTime(),
                act.getDeliveredDateTime(),
                new LisDataDictionary(
                        act.getStorageAndDeliveryCondition().getConditionId(),
                        act.getStorageAndDeliveryCondition().getDescription().getUz()
                ),
                act.getAdditionalInfo(),
                act.getParticipant().getFullName(),
                act.getParticipant().getPositionId(),
                new LisDataDictionary(
                        act.getPackageTypeInfo().getPackageTypeId(),
                        act.getPackageTypeInfo().getPackageTypeUz()
                ),
                act.getSamplingDocuments(),
                act.getGoal(),
                new LisDataDictionary(
                        act.getSpecialCondition().getConditionId(),
                        act.getSpecialCondition().getDescription().getUz()
                ),
                act.getSampler().getPositionId(),
                null,
                null,
                null,
                new LisDataDictionary(
                        act.getConservationTypeInfo().getConservationMethodId(),
                        act.getConservationTypeInfo().getConservationMethodsUz()
                ),
                details.stream().map(this::toSelectionItemFromAct153).toList()
        );
    }

    private SelectionActItemDto toSelectionItemFromAct153(Act153Detail d) {
        return new SelectionActItemDto(
                d.getResearchItemTypeInfo().getItemTypeId(),
                d.getSampleLocation(),
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
                hasSampleTypeInfo(d)
                        ? new LisDataDictionary(
                        d.getSampleTypeInfo().getSampleTypeId(),
                        d.getSampleTypeInfo().getSampleTypeUz()
                )
                        : null,
                null,
                null
        );
    }

    private ActLisDto fromAct154(Act154 act) {
        Set<Act154Detail> details = act.getAct154Details();
        Integer actTemplateId = resolveLisActTemplateId(act.getType());

        return new ActLisDto(
                actTemplateId,
                act.getInstitution().getTin() != null ? act.getInstitution().getTin().toString() : null,
                act.getInstitution().getInstitutionName(),
                act.getInstitution().getInstitutionAddress(),
                act.getInstitution().getInstitutionLegalAddress(),
                new LisDataDictionary(act.getPurpose().getPurposeId(), act.getPurpose().getSamplingPurposeUz()),
                act.getSampleTakenDateTime(),
                act.getDeliveredDateTime(),
                new LisDataDictionary(
                        act.getStorageAndDeliveryCondition().getConditionId(),
                        act.getStorageAndDeliveryCondition().getDescription().getUz()
                ),
                act.getAdditionalInfo(),
                act.getParticipant().getFullName(),
                act.getParticipant().getPositionId(),
                new LisDataDictionary(
                        act.getPackageTypeInfo().getPackageTypeId(),
                        act.getPackageTypeInfo().getPackageTypeUz()
                ),
                act.getDocumentConfirmSampling(),
                act.getGoal(),
                new LisDataDictionary(
                        act.getSpecialCondition().getConditionId(),
                        act.getSpecialCondition().getDescription().getUz()
                ),
                act.getSampler().getPositionId(),
                act.getManufacturingCompany(),
                act.getManufactureDate(),
                act.getDocNumberOfTakenObject(),
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
                null,
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

    private ActLisDto fromAct223(Act223 act) {
        List<Act223Detail> details = new ArrayList<>(act.getAct223Details());
        Integer actTemplateId = resolveLisActTemplateId(act.getType());

        return new ActLisDto(
                actTemplateId,
                act.getInstitution().getTin() != null ? act.getInstitution().getTin().toString() : null,
                act.getInstitution().getInstitutionName(),
                act.getInstitution().getInstitutionAddress(),
                act.getInstitution().getInstitutionLegalAddress(),
                new LisDataDictionary(act.getPurpose().getPurposeId(), act.getPurpose().getSamplingPurposeUz()),
                act.getSampleTakenDateTime(),
                act.getDeliveredDateTime(),
                new LisDataDictionary(
                        act.getStorageAndDeliveryCondition().getConditionId(),
                        act.getStorageAndDeliveryCondition().getDescription().getUz()
                ),
                act.getAdditionalInfo(),
                act.getParticipant().getFullName(),
                act.getParticipant().getPositionId(),
                new LisDataDictionary(
                        act.getPackageTypeInfo().getPackageTypeId(),
                        act.getPackageTypeInfo().getPackageTypeUz()
                ),
                act.getSupportingDocumentsForSampling(),
                act.getGoal(),
                new LisDataDictionary(
                        act.getSpecialCondition().getConditionId(),
                        act.getSpecialCondition().getDescription().getUz()
                ),
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
                null,
                d.getAmount() != null ? d.getAmount().toString() : null,
                null
        );
    }

    private Integer resolveLisActTemplateId(ActType actType) {
        LisResearchCode lisResearchCode = ActType.mapToLisResearchCode(actType);
        URI uri = integrationUrlFactory.lisResearchTypeUrl(lisResearchCode.name());

        log.info("Fetching LIS act template ID. actType={}, lisResearchCode={}, uri={}",
                actType,
                lisResearchCode,
                uri
        );

        Integer actTemplateId = integrationRestClient
                .get()
                .uri(uri)
                .retrieve()
                .body(Integer.class);

        if (actTemplateId == null) {
            throw new IllegalStateException(
                    "LIS act template ID is empty. actType=" + actType + ", lisResearchCode=" + lisResearchCode
            );
        }

        return actTemplateId;
    }

    private boolean hasSampleTypeInfo(Act153Detail detail) {
        if (detail.getSampleTypeInfo() == null) {
            return false;
        }

        return detail.getSampleTypeInfo().getSampleTypeId() != null
                || detail.getSampleTypeInfo().getSampleTypeUz() != null
                || detail.getSampleTypeInfo().getSampleTypeRu() != null;
    }
}