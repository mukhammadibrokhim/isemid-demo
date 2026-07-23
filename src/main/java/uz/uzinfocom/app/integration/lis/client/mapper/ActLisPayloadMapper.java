package uz.uzinfocom.app.integration.lis.client.mapper;

import org.springframework.stereotype.Component;
import uz.uzinfocom.app.integration.lis.client.dto.LisActPushRequest;
import uz.uzinfocom.app.integration.lis.client.dto.LisActPushRequest.LisDataDictionary;
import uz.uzinfocom.app.integration.lis.client.dto.LisActPushRequest.SelectionActItem;
import uz.uzinfocom.app.integration.lis.client.dto.LisPriority;
import uz.uzinfocom.app.integration.lis.client.dto.LisResearchCode;
import uz.uzinfocom.app.integration.lis.common.exception.LisUnsupportedActTypeException;
import uz.uzinfocom.app.modules.act.domain.model.Act;
import uz.uzinfocom.app.modules.act.domain.model.act153.Act153;
import uz.uzinfocom.app.modules.act.domain.model.act153.Act153Detail;
import uz.uzinfocom.app.modules.act.domain.model.act154.Act154;
import uz.uzinfocom.app.modules.act.domain.model.act154.Act154Detail;
import uz.uzinfocom.app.modules.act.domain.model.act223.Act223;
import uz.uzinfocom.app.modules.act.domain.model.act223.Act223Detail;
import uz.uzinfocom.app.modules.act.domain.model.embedded.ConditionInfo;
import uz.uzinfocom.app.modules.act.domain.model.embedded.ConservationTypeInfo;
import uz.uzinfocom.app.modules.act.domain.model.embedded.EmployeeInfo;
import uz.uzinfocom.app.modules.act.domain.model.embedded.Institution;
import uz.uzinfocom.app.modules.act.domain.model.embedded.PackageTypeInfo;
import uz.uzinfocom.app.modules.act.domain.model.embedded.Purpose;
import uz.uzinfocom.app.modules.act.domain.model.embedded.ResearchItemTypeInfo;
import uz.uzinfocom.app.modules.act.domain.model.embedded.SampleTypeInfo;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Builds the LIS push payload straight from an {@code Act} entity, in one
 * step — the legacy code did this in three (entity → {@code ActLisDto} →
 * {@code EMisToLisRequestMapper} → {@code LisActPushRequest}) purely to
 * decouple the act-template-id HTTP lookup from the local mapping; here that
 * lookup is a separate {@code LisActClient} call made by the orchestrator
 * before this mapper runs, so the middle stage has no reason to exist.
 *
 * <p>Only {@link uz.uzinfocom.app.modules.act.domain.enums.ActType#ACT153},
 * {@code ACT154} and {@code ACT223} have a LIS mapping — see
 * {@link LisResearchCode}.
 */
@Component
public class ActLisPayloadMapper {

    private static final ZoneId TASHKENT = ZoneId.of("Asia/Tashkent");

    /**
     * @param actTemplateId   resolved separately via {@code LisActClient},
     *                        since it comes from its own LIS lookup call
     * @param priority        chosen by the employee in the send dialog
     * @param paid            chosen by the employee in the send dialog
     * @param fullNameOfDoctor the sending employee's full name
     * @param redirectUrl     where LIS should post the result back to us
     */
    public LisActPushRequest toPushRequest(
            Act act,
            Integer actTemplateId,
            LisPriority priority,
            Boolean paid,
            String fullNameOfDoctor,
            String redirectUrl
    ) {
        return switch (act) {
            case Act153 a -> fromAct153(a, actTemplateId, priority, paid, fullNameOfDoctor, redirectUrl);
            case Act154 a -> fromAct154(a, actTemplateId, priority, paid, fullNameOfDoctor, redirectUrl);
            case Act223 a -> fromAct223(a, actTemplateId, priority, paid, fullNameOfDoctor, redirectUrl);
            default -> throw new LisUnsupportedActTypeException(act.getActType());
        };
    }

    private LisActPushRequest fromAct153(
            Act153 act, Integer actTemplateId, LisPriority priority, Boolean paid,
            String fullNameOfDoctor, String redirectUrl
    ) {
        return LisActPushRequest.builder()
                .actTemplateId(actTemplateId)
                .priority(priority)
                .tin(tinOf(act.getInstitution()))
                .organizationName(nameOf(act.getInstitution()))
                .organizationAddress(addressOf(act.getInstitution()))
                .organizationLegalAddress(legalAddressOf(act.getInstitution()))
                .purpose(dictionaryOf(act.getPurpose()))
                .sampleTakenDate(toUtcInstant(act.getSampleTakenDateTime()))
                .deliveryDateToLaboratory(toUtcInstant(act.getDeliveredDateTime()))
                .conditions(dictionaryOf(act.getStorageAndDeliveryCondition()))
                .additionalInformation(act.getAdditionalInfo())
                .involvedPersonName(fullNameOf(act.getParticipant()))
                .involvedProfessionId(positionOf(act.getParticipant()))
                .packageType(dictionaryOf(act.getPackageTypeInfo()))
                .document(act.getSamplingDocuments())
                .goal(act.getGoal())
                .noteConditions(dictionaryOf(act.getSpecialCondition()))
                .fullNameOfDoctor(fullNameOfDoctor)
                .collectorProfessionId(positionOf(act.getSampler()))
                .preservationMethod(dictionaryOf(act.getConservationTypeInfo()))
                .redirectUrl(redirectUrl)
                .paid(paid)
                .selectionActItems(act.getAct153Details().stream().map(this::toSelectionItem).toList())
                .build();
    }

    private SelectionActItem toSelectionItem(Act153Detail detail) {
        return SelectionActItem.builder()
                .itemTypeId(itemTypeIdOf(detail.getResearchItemTypeInfo()))
                .samplingAddress(detail.getAddress())
                .samplingDepth(toStringOrNull(detail.getSamplingDepth()))
                .depthUnit(detail.getDepthUnit())
                .distanceFromShore(toStringOrNull(detail.getDistanceFromShore()))
                .distanceFromShoreUnit(detail.getDistanceFromShoreUnit())
                .weatherConditions(toStringOrNull(detail.getWeatherAtSampling()))
                .waterTemperature(toStringOrNull(detail.getWaterTemperature()))
                .sampleType(hasSampleType(detail.getSampleTypeInfo()) ? dictionaryOf(detail.getSampleTypeInfo()) : null)
                .coordinates(toCoordinates(detail.getSampleLocation()))
                .build();
    }

    private LisActPushRequest fromAct154(
            Act154 act, Integer actTemplateId, LisPriority priority, Boolean paid,
            String fullNameOfDoctor, String redirectUrl
    ) {
        return LisActPushRequest.builder()
                .actTemplateId(actTemplateId)
                .priority(priority)
                .tin(tinOf(act.getInstitution()))
                .organizationName(nameOf(act.getInstitution()))
                .organizationAddress(addressOf(act.getInstitution()))
                .organizationLegalAddress(legalAddressOf(act.getInstitution()))
                .purpose(dictionaryOf(act.getPurpose()))
                .sampleTakenDate(toUtcInstant(act.getSampleTakenDateTime()))
                .deliveryDateToLaboratory(toUtcInstant(act.getDeliveredDateTime()))
                .conditions(dictionaryOf(act.getStorageAndDeliveryCondition()))
                .additionalInformation(act.getAdditionalInfo())
                .involvedPersonName(fullNameOf(act.getParticipant()))
                .involvedProfessionId(positionOf(act.getParticipant()))
                .packageType(dictionaryOf(act.getPackageTypeInfo()))
                .document(act.getDocumentConfirmSampling())
                .goal(act.getGoal())
                .noteConditions(dictionaryOf(act.getSpecialCondition()))
                .fullNameOfDoctor(fullNameOfDoctor)
                .collectorProfessionId(positionOf(act.getSampler()))
                .manufacturer(act.getManufacturingCompany())
                .manufactureDate(act.getManufactureDate() == null
                        ? null
                        : toUtcInstant(act.getManufactureDate().atStartOfDay()))
                .docNumber(act.getDocNumberOfTakenObject())
                .redirectUrl(redirectUrl)
                .paid(paid)
                .selectionActItems(act.getAct154Details().stream().map(this::toSelectionItem).toList())
                .build();
    }

    private SelectionActItem toSelectionItem(Act154Detail detail) {
        return SelectionActItem.builder()
                .itemTypeId(itemTypeIdOf(detail.getResearchItemTypeInfo()))
                .groupSize(toStringOrNull(detail.getGroupSize()))
                .sampleWeight(toStringOrNull(detail.getSampleWeight()))
                .build();
    }

    private LisActPushRequest fromAct223(
            Act223 act, Integer actTemplateId, LisPriority priority, Boolean paid,
            String fullNameOfDoctor, String redirectUrl
    ) {
        return LisActPushRequest.builder()
                .actTemplateId(actTemplateId)
                .priority(priority)
                .tin(tinOf(act.getInstitution()))
                .organizationName(nameOf(act.getInstitution()))
                .organizationAddress(addressOf(act.getInstitution()))
                .organizationLegalAddress(legalAddressOf(act.getInstitution()))
                .purpose(dictionaryOf(act.getPurpose()))
                .sampleTakenDate(toUtcInstant(act.getSampleTakenDateTime()))
                .deliveryDateToLaboratory(toUtcInstant(act.getDeliveredDateTime()))
                .conditions(dictionaryOf(act.getStorageAndDeliveryCondition()))
                .additionalInformation(act.getAdditionalInfo())
                .involvedPersonName(fullNameOf(act.getParticipant()))
                .involvedProfessionId(positionOf(act.getParticipant()))
                .packageType(dictionaryOf(act.getPackageTypeInfo()))
                .document(act.getSupportingDocumentsForSampling())
                .goal(act.getGoal())
                .noteConditions(dictionaryOf(act.getSpecialCondition()))
                .fullNameOfDoctor(fullNameOfDoctor)
                .collectorProfessionId(positionOf(act.getSampler()))
                .redirectUrl(redirectUrl)
                .paid(paid)
                .selectionActItems(act.getAct223Details().stream().map(this::toSelectionItem).toList())
                .build();
    }

    private SelectionActItem toSelectionItem(Act223Detail detail) {
        return SelectionActItem.builder()
                .itemTypeId(itemTypeIdOf(detail.getResearchItemTypeInfo()))
                .samplingDepth(toStringOrNull(detail.getDepthOfObtainedArea()))
                .depthUnit(detail.getDepthUnit())
                .sampleQt(toStringOrNull(detail.getAmount()))
                .coordinates(toCoordinates(detail.getExactLocationPointSampling()))
                .build();
    }

    private String tinOf(Institution institution) {
        return institution == null || institution.getTin() == null ? null : institution.getTin().toString();
    }

    private String nameOf(Institution institution) {
        return institution == null ? null : institution.getInstitutionName();
    }

    private String addressOf(Institution institution) {
        return institution == null ? null : institution.getInstitutionAddress();
    }

    private String legalAddressOf(Institution institution) {
        return institution == null ? null : institution.getInstitutionLegalAddress();
    }

    private String fullNameOf(EmployeeInfo info) {
        return info == null ? null : info.getFullName();
    }

    private Integer positionOf(EmployeeInfo info) {
        return info == null ? null : info.getPositionId();
    }

    private LisDataDictionary dictionaryOf(Purpose purpose) {
        return purpose == null ? null : new LisDataDictionary(purpose.getPurposeId(), purpose.getSamplingPurposeUz());
    }

    private LisDataDictionary dictionaryOf(ConditionInfo info) {
        if (info == null) {
            return null;
        }
        String name = info.getDescription() == null ? null : info.getDescription().getUz();
        return new LisDataDictionary(info.getConditionId(), name);
    }

    private LisDataDictionary dictionaryOf(PackageTypeInfo info) {
        return info == null ? null : new LisDataDictionary(info.getPackageTypeId(), info.getPackageTypeUz());
    }

    private LisDataDictionary dictionaryOf(ConservationTypeInfo info) {
        return info == null
                ? null
                : new LisDataDictionary(info.getConservationMethodId(), info.getConservationMethodsUz());
    }

    private LisDataDictionary dictionaryOf(SampleTypeInfo info) {
        return info == null ? null : new LisDataDictionary(info.getSampleTypeId(), info.getSampleTypeUz());
    }

    private boolean hasSampleType(SampleTypeInfo info) {
        return info != null && (info.getSampleTypeId() != null || info.getSampleTypeUz() != null || info.getSampleTypeRu() != null);
    }

    private Integer itemTypeIdOf(ResearchItemTypeInfo info) {
        return info == null ? null : info.getItemTypeId();
    }

    private String toStringOrNull(Object value) {
        return value == null ? null : value.toString();
    }

    /**
     * LIS wants UTC ISO-8601 instants; our act data is captured in local
     * ("Asia/Tashkent") time with no zone attached.
     */
    private String toUtcInstant(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(TASHKENT).withZoneSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
    }

    /**
     * Splits a free-text "location" string into whitespace-separated tokens —
     * matches the legacy {@code ActLisDto.mapToLocation} behavior exactly,
     * coordinate-parsing quirks included, since the LIS contract for this
     * field predates this rewrite.
     */
    private List<String> toCoordinates(String location) {
        if (location == null || location.isBlank()) {
            return List.of();
        }
        return Arrays.asList(location.trim().split("\\s+"));
    }
}
