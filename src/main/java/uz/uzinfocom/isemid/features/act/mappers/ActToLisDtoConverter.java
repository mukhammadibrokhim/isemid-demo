package uz.uzinfocom.isemid.features.act.mappers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.act.dtos.ActLisDto;
import uz.uzinfocom.isemid.features.act.dtos.ActLisDto.SelectionActItemDto;
import uz.uzinfocom.isemid.features.integrations.lis.LisActClient;
import uz.uzinfocom.isemid.features.integrations.lis.dto.LisResponse;
import uz.uzinfocom.isemid.features.integrations.lis.dto.CreateLisActRequest.LisDataDictionary;
import uz.uzinfocom.isemid.features.act.enums.ActType;
import uz.uzinfocom.isemid.features.act.enums.LisResearchCode;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.models.act153.Act153;
import uz.uzinfocom.isemid.features.act.models.act153.Act153Detail;
import uz.uzinfocom.isemid.features.act.models.act154.Act154;
import uz.uzinfocom.isemid.features.act.models.act154.Act154Detail;
import uz.uzinfocom.isemid.features.act.models.act223.Act223;
import uz.uzinfocom.isemid.features.act.models.act223.Act223Detail;
import uz.uzinfocom.isemid.features.act.models.common.ConditionInfo;
import uz.uzinfocom.isemid.features.act.models.common.EmployeeInfo;
import uz.uzinfocom.isemid.features.act.models.common.Institution;
import uz.uzinfocom.isemid.features.act.models.common.PackageTypeInfo;
import uz.uzinfocom.isemid.features.act.models.common.Purpose;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Component
public class ActToLisDtoConverter {

    private final LisActClient lisActClient;

    public ActToLisDtoConverter(LisActClient lisActClient) {
        this.lisActClient = lisActClient;
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

        return buildCommonFields(
                act.getType(), act.getInstitution(), act.getPurpose(), act.getSampleTakenDateTime(),
                act.getDeliveredDateTime(), act.getStorageAndDeliveryCondition(), act.getAdditionalInfo(),
                act.getParticipant(), act.getPackageTypeInfo(), act.getGoal(), act.getSpecialCondition(), act.getSampler()
        )
                .document(act.getSamplingDocuments())
                .preservationMethod(new LisDataDictionary(
                        act.getConservationTypeInfo().getConservationMethodId(),
                        act.getConservationTypeInfo().getConservationMethodsUz()
                ))
                .selectionActItems(details.stream().map(this::toSelectionItemFromAct153).toList())
                .build();
    }

    private SelectionActItemDto toSelectionItemFromAct153(Act153Detail detail) {
        SelectionActItemDto.SelectionActItemDtoBuilder item = SelectionActItemDto.builder()
                .itemTypeId(detail.getResearchItemTypeInfo().getItemTypeId())
                .location(detail.getSampleLocation())
                .samplingAddress(detail.getAddress())
                .samplingDepth(Objects.toString(detail.getSamplingDepth(), null))
                .depthUnit(detail.getDepthUnit())
                .distanceFromShore(Objects.toString(detail.getDistanceFromShore(), null))
                .distanceFromShoreUnit(detail.getDistanceFromShoreUnit())
                .weatherConditions(Objects.toString(detail.getWeatherAtSampling(), null))
                .waterTemperature(Objects.toString(detail.getWaterTemperature(), null));

        if (hasSampleTypeInfo(detail)) {
            item.sampleType(new LisDataDictionary(
                    detail.getSampleTypeInfo().getSampleTypeId(),
                    detail.getSampleTypeInfo().getSampleTypeUz()
            ));
        }

        return item.build();
    }

    private ActLisDto fromAct154(Act154 act) {
        Set<Act154Detail> details = act.getAct154Details();

        return buildCommonFields(
                act.getType(), act.getInstitution(), act.getPurpose(), act.getSampleTakenDateTime(),
                act.getDeliveredDateTime(), act.getStorageAndDeliveryCondition(), act.getAdditionalInfo(),
                act.getParticipant(), act.getPackageTypeInfo(), act.getGoal(), act.getSpecialCondition(), act.getSampler()
        )
                .document(act.getDocumentConfirmSampling())
                .manufacturer(act.getManufacturingCompany())
                .manufactureDate(act.getManufactureDate())
                .docNumber(act.getDocNumberOfTakenObject())
                .selectionActItems(details.stream().map(this::toSelectionItemFromAct154).toList())
                .build();
    }

    private SelectionActItemDto toSelectionItemFromAct154(Act154Detail d) {
        return SelectionActItemDto.builder()
                .itemTypeId(d.getResearchItemTypeInfo().getItemTypeId())
                .groupSize(Objects.toString(d.getGroupSize(), null))
                .sampleWeight(Objects.toString(d.getSampleWeight(), null))
                .sampleQtUnit(d.getSampleQtUnit())
                .build();
    }

    private ActLisDto fromAct223(Act223 act) {
        List<Act223Detail> details = new ArrayList<>(act.getAct223Details());

        return buildCommonFields(
                act.getType(), act.getInstitution(), act.getPurpose(), act.getSampleTakenDateTime(),
                act.getDeliveredDateTime(), act.getStorageAndDeliveryCondition(), act.getAdditionalInfo(),
                act.getParticipant(), act.getPackageTypeInfo(), act.getGoal(), act.getSpecialCondition(), act.getSampler()
        )
                .document(act.getSupportingDocumentsForSampling())
                .selectionActItems(details.stream().map(this::toSelectionItemFromAct223).toList())
                .build();
    }

    private SelectionActItemDto toSelectionItemFromAct223(Act223Detail d) {
        return SelectionActItemDto.builder()
                .itemTypeId(d.getResearchItemTypeInfo().getItemTypeId())
                .location(d.getExactLocationPointSampling())
                .samplingDepth(Objects.toString(d.getDepthOfObtainedArea(), null))
                .depthUnit(d.getDepthUnit())
                .sampleQt(Objects.toString(d.getAmount(), null))
                .build();
    }

    /**
     * Builds the fields shared by every act type (institution, purpose, dates,
     * conditions, participant, package type, goal) as a pre-populated builder,
     * leaving the act-type-specific fields (document, manufacturer, manufactureDate,
     * docNumber, preservationMethod, selectionActItems) for the caller to set.
     */
    private ActLisDto.ActLisDtoBuilder buildCommonFields(
            ActType actType,
            Institution institution,
            Purpose purpose,
            LocalDateTime sampleTakenDateTime,
            LocalDateTime deliveredDateTime,
            ConditionInfo storageAndDeliveryCondition,
            String additionalInfo,
            EmployeeInfo participant,
            PackageTypeInfo packageTypeInfo,
            String goal,
            ConditionInfo specialCondition,
            EmployeeInfo sampler
    ) {
        return ActLisDto.builder()
                .actTemplateId(resolveLisActTemplateId(actType))
                .tin(Objects.toString(institution.getTin(), null))
                .organizationName(institution.getInstitutionName())
                .organizationAddress(institution.getInstitutionAddress())
                .organizationLegalAddress(institution.getInstitutionLegalAddress())
                .purpose(new LisDataDictionary(purpose.getPurposeId(), purpose.getSamplingPurposeUz()))
                .sampleTakenDateTime(sampleTakenDateTime)
                .deliveryDateTime(deliveredDateTime)
                .conditions(new LisDataDictionary(
                        storageAndDeliveryCondition.getConditionId(),
                        storageAndDeliveryCondition.getDescription().getUz()
                ))
                .additionalInformation(additionalInfo)
                .involvedPersonName(participant.getFullName())
                .involvedProfessionId(participant.getPositionId())
                .packageType(new LisDataDictionary(packageTypeInfo.getPackageTypeId(), packageTypeInfo.getPackageTypeUz()))
                .goal(goal)
                .noteConditions(new LisDataDictionary(
                        specialCondition.getConditionId(),
                        specialCondition.getDescription().getUz()
                ))
                .collectorProfessionId(sampler.getPositionId());
    }

    private Long resolveLisActTemplateId(ActType actType) {
        LisResearchCode lisResearchCode = ActType.mapToLisResearchCode(actType);
        Long lisActTemplateResponse = lisActClient.getLisActTemplate(lisResearchCode.name());

        if (Objects.isNull(lisActTemplateResponse)) {
            throw new IllegalStateException(
                    "LIS act template ID is empty. actType=" + actType + ", lisResearchCode=" + lisResearchCode
            );
        }
        return lisActTemplateResponse;
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