package uz.uzinfocom.app.modules.act.application.query.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.Act153DetailResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.Act154DetailResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.Act155DetailResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.Act156DetailResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.Act223DetailResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.Act224DetailResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.ActDetailResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.act153.Act153SampleResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.act154.Act154SampleResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.act155.Act155SampleResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.act156.Act156GroupDetailResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.act156.Act156KitchenUtensilResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.act223.Act223SampleResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.act224.Act224RecommendationResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.ActInstitutionResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.ConditionInfoResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.ConservationTypeInfoResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.EmployeeInfoResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.PackageTypeInfoResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.PurposeResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.ResearchItemTypeInfoResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.SampleTypeInfoResponse;
import uz.uzinfocom.app.modules.act.domain.model.Act;
import uz.uzinfocom.app.modules.act.domain.model.act153.Act153;
import uz.uzinfocom.app.modules.act.domain.model.act153.Act153Detail;
import uz.uzinfocom.app.modules.act.domain.model.act154.Act154;
import uz.uzinfocom.app.modules.act.domain.model.act154.Act154Detail;
import uz.uzinfocom.app.modules.act.domain.model.act155.Act155;
import uz.uzinfocom.app.modules.act.domain.model.act155.Act155Detail;
import uz.uzinfocom.app.modules.act.domain.model.act156.Act156;
import uz.uzinfocom.app.modules.act.domain.model.act156.Act156GroupDetail;
import uz.uzinfocom.app.modules.act.domain.model.act156.Act156KitchenUtensil;
import uz.uzinfocom.app.modules.act.domain.model.act223.Act223;
import uz.uzinfocom.app.modules.act.domain.model.act223.Act223Detail;
import uz.uzinfocom.app.modules.act.domain.model.act224.Act224;
import uz.uzinfocom.app.modules.act.domain.model.act224.Act224Detail;
import uz.uzinfocom.app.modules.act.domain.model.embedded.ConditionInfo;
import uz.uzinfocom.app.modules.act.domain.model.embedded.ConservationTypeInfo;
import uz.uzinfocom.app.modules.act.domain.model.embedded.EmployeeInfo;
import uz.uzinfocom.app.modules.act.domain.model.embedded.Institution;
import uz.uzinfocom.app.modules.act.domain.model.embedded.PackageTypeInfo;
import uz.uzinfocom.app.modules.act.domain.model.embedded.Purpose;
import uz.uzinfocom.app.modules.act.domain.model.embedded.ResearchItemTypeInfo;
import uz.uzinfocom.app.modules.act.domain.model.embedded.SampleTypeInfo;
import uz.uzinfocom.app.modules.card.application.query.dto.CardMiniResponse;
import uz.uzinfocom.app.modules.card.application.query.mapper.CardTableMapperHelper;
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;

import java.util.List;

/**
 * Hand-written rather than MapStruct — the source is a JOINED-inheritance
 * hierarchy and the target a sealed interface, so building the right
 * concrete response record for the runtime {@link Act} subtype is a plain
 * pattern-matching switch (Java 21), the same shape {@code Form058PdfMapper}
 * uses for its own print-oriented composition. Used for both
 * {@code GET /v1/acts/{id}} (with {@code audit} resolved) and
 * {@code GET /v1/acts/{id}/pdf} (passing {@code null} for {@code audit},
 * which that view has no use for) — see {@link ActDetailResponse}'s javadoc
 * for why one shape covers both.
 */
@Component
@RequiredArgsConstructor
public class ActDetailMapper {

    private final CardTableMapperHelper cardTableMapperHelper;

    public ActDetailResponse toDetailResponse(Act act, AuditResponse audit) {
        return switch (act) {
            case Act153 a -> toAct153(a, audit);
            case Act154 a -> toAct154(a, audit);
            case Act155 a -> toAct155(a, audit);
            case Act156 a -> toAct156(a, audit);
            case Act223 a -> toAct223(a, audit);
            case Act224 a -> toAct224(a, audit);
            default -> throw new IllegalStateException("Unsupported Act subtype: " + act.getClass());
        };
    }

    private Act153DetailResponse toAct153(Act153 act, AuditResponse audit) {
        return new Act153DetailResponse(
                act.getId(), act.getActType(), act.getActStatus(), cardMini(act.getCard()), act.getAssignedById(),
                act.getResultComment(), institution(act.getInstitution()),
                act.getActNumber(), act.getActivityTypeCode(), act.getSamplingDocuments(), act.getGoal(),
                act.getSampleTakenDateTime(), act.getDeliveredDateTime(), purpose(act.getPurpose()),
                employee(act.getSampler()), employee(act.getParticipant()), condition(act.getSpecialCondition()),
                condition(act.getStorageAndDeliveryCondition()), act.getLisOrganizationId(), act.getLaboratoryAddress(),
                packageType(act.getPackageTypeInfo()), conservationType(act.getConservationTypeInfo()),
                act.getAdditionalInfo(), act153Samples(act.getAct153Details()), audit
        );
    }

    private Act154DetailResponse toAct154(Act154 act, AuditResponse audit) {
        return new Act154DetailResponse(
                act.getId(), act.getActType(), act.getActStatus(), cardMini(act.getCard()), act.getAssignedById(),
                act.getResultComment(), institution(act.getInstitution()),
                act.getTitle(), act.getActNumber(), act.getActivityTypeCode(), act.getSampleTakenDateTime(),
                act.getDeliveredDateTime(), act.getDocumentConfirmSampling(), act.getGoal(), purpose(act.getPurpose()),
                employee(act.getSampler()), employee(act.getParticipant()), act.getManufacturingCompany(),
                act.getManufactureDate(), act.getDocNumberOfTakenObject(), condition(act.getSpecialCondition()),
                condition(act.getStorageAndDeliveryCondition()), act.getLisOrganizationId(), act.getLaboratoryAddress(),
                packageType(act.getPackageTypeInfo()), act.getAdditionalInfo(), act154Samples(act.getAct154Details()),
                audit
        );
    }

    private Act155DetailResponse toAct155(Act155 act, AuditResponse audit) {
        return new Act155DetailResponse(
                act.getId(), act.getActType(), act.getActStatus(), cardMini(act.getCard()), act.getAssignedById(),
                act.getResultComment(), institution(act.getInstitution()),
                act.getTitle(), act.getTin(), act.getInstitutionName(), act.getInstitutionAddress(),
                act.getActivityTypeCode(), act.getSelectedDate(), act.getSamplerFullName(), act.getSamplerPosition(),
                act.getObjectRepresentativeFullName(), act.getObjectRepresentativePosition(), act.getAdditionalInfo(),
                act155Samples(act.getAct155Details()), audit
        );
    }

    private Act156DetailResponse toAct156(Act156 act, AuditResponse audit) {
        return new Act156DetailResponse(
                act.getId(), act.getActType(), act.getActStatus(), cardMini(act.getCard()), act.getAssignedById(),
                act.getResultComment(), institution(act.getInstitution()),
                act.getTitle(), act.getTin(), act.getInstitutionName(), act.getInstitutionAddress(),
                act.getActivityTypeCode(), act.getSampleTakenTime(), act.getLisOrganizationId(), act.getLaboratoryAddress(),
                act.getSampleDeliveryTime(), act.getFullNameOfSampler(), act.getPositionOfSampler(),
                act.getFullNameOfObjectRepresentative(), act.getPositionOfObjectRepresentative(),
                act156KitchenUtensils(act.getAct156KitchenUtensils()), act156GroupDetails(act.getAct156GroupDetails()),
                audit
        );
    }

    private Act223DetailResponse toAct223(Act223 act, AuditResponse audit) {
        return new Act223DetailResponse(
                act.getId(), act.getActType(), act.getActStatus(), cardMini(act.getCard()), act.getAssignedById(),
                act.getResultComment(), institution(act.getInstitution()),
                act.getActNumber(), act.getSupportingDocumentsForSampling(), act.getGoal(), act.getActivityTypeCode(),
                employee(act.getSampler()), employee(act.getParticipant()), purpose(act.getPurpose()),
                act.getSampleTakenDateTime(), act.getDeliveredDateTime(), condition(act.getSpecialCondition()),
                condition(act.getStorageAndDeliveryCondition()), act.getLisOrganizationId(), act.getLaboratoryAddress(),
                packageType(act.getPackageTypeInfo()), act.getAdditionalInfo(), act223Samples(act.getAct223Details()),
                audit
        );
    }

    private Act224DetailResponse toAct224(Act224 act, AuditResponse audit) {
        return new Act224DetailResponse(
                act.getId(), act.getActType(), act.getActStatus(), cardMini(act.getCard()), act.getAssignedById(),
                act.getResultComment(), institution(act.getInstitution()),
                act.getTin(), act.getInstitutionName(), act.getInstitutionAddress(), act.getActivityTypeCode(),
                act.getFullNameOfEpidStaff(), act.getPositionOfEpidStaff(), act.getFullNameOfParticipantEpid(),
                act.getPositionOfParticipantEpid(), act.getNameOfInstitution(), act.getAddressOfInstitution(),
                act.getNameOfRegulatoryActs(), act.getCheckingFulfillmentOfRequirements(), act.getFullNameOfParticipant(),
                act.getAdditionalInfo(), act224Recommendations(act.getAct224Details()), audit
        );
    }

    private List<Act153SampleResponse> act153Samples(List<Act153Detail> details) {
        return details.stream()
                .map(detail -> new Act153SampleResponse(
                        detail.getId(), researchItemType(detail.getResearchItemTypeInfo()), detail.getObjectTypeId(),
                        detail.getObjectCode(), detail.getAddress(), detail.getSamplingDepth(), detail.getDepthUnit(),
                        detail.getDistanceFromShore(), detail.getDistanceFromShoreUnit(), detail.getSampleVolume(),
                        detail.getSampleVolumeUnit(), detail.getSampleQtUnit(), detail.getSampleLocation(),
                        detail.getWeatherAtSampling(), detail.getWaterTemperature(), sampleType(detail.getSampleTypeInfo())
                ))
                .toList();
    }

    private List<Act154SampleResponse> act154Samples(List<Act154Detail> details) {
        return details.stream()
                .map(detail -> new Act154SampleResponse(
                        detail.getId(), researchItemType(detail.getResearchItemTypeInfo()), detail.getShiftCode(),
                        detail.getSampleName(), detail.getGroupSize(), detail.getSerialNumberOfGroup(),
                        detail.getSampleWeight(), detail.getSampleQtUnit(), detail.getSampleVolume(),
                        detail.getSampleVolumeUnit(), detail.getNote()
                ))
                .toList();
    }

    private List<Act155SampleResponse> act155Samples(List<Act155Detail> details) {
        return details.stream()
                .map(detail -> new Act155SampleResponse(
                        detail.getId(), detail.getProductName(), detail.getPurposeOfTesting(),
                        detail.getPurposeOfTestingLoinc(), detail.getLisOrganizationId(), detail.getLaboratoryAddress(),
                        detail.getSampleTakenLocation(), detail.getSampleQuantity(), detail.getProductBatchQuantity(),
                        detail.getAppliedPesticides(), detail.getManufacturer(), detail.getSampleDocumentJustifying()
                ))
                .toList();
    }

    private List<Act156KitchenUtensilResponse> act156KitchenUtensils(List<Act156KitchenUtensil> items) {
        return items.stream()
                .map(item -> new Act156KitchenUtensilResponse(
                        item.getId(), item.getKnifeForBread(), item.getFruitCuttingBoard(), item.getDistributionTable(),
                        item.getContainerForFinishedProducts(), item.getFullNameOfChef(), item.getHandsOfChef(),
                        item.getClothesOfChef()
                ))
                .toList();
    }

    private List<Act156GroupDetailResponse> act156GroupDetails(List<Act156GroupDetail> items) {
        return items.stream()
                .map(item -> new Act156GroupDetailResponse(
                        item.getId(), item.getFullNameOfEducator(), item.getHandsOfEducator(), item.getFirstFoodBowl(),
                        item.getSecondFoodBowl(), item.getTables(), item.getChairs(), item.getWindowSill(),
                        item.getDoorHandles(), item.getToys(), item.getToyShelf(), item.getCarpets(),
                        item.getClothesRack(), item.getFullNameOfPlaceOwner(), item.getBedClothes(),
                        item.getBathroomWall(), item.getTowels(), item.getTowelRack(), item.getWaterTapFaucet(),
                        item.getWcSeats()
                ))
                .toList();
    }

    private List<Act223SampleResponse> act223Samples(List<Act223Detail> details) {
        return details.stream()
                .map(detail -> new Act223SampleResponse(
                        detail.getId(), researchItemType(detail.getResearchItemTypeInfo()),
                        detail.getExactLocationPointSampling(), detail.getAmount(), detail.getDepthOfObtainedArea(),
                        detail.getDepthUnit()
                ))
                .toList();
    }

    private List<Act224RecommendationResponse> act224Recommendations(List<Act224Detail> details) {
        return details.stream()
                .map(detail -> new Act224RecommendationResponse(
                        detail.getId(), detail.getRecommendedActivities(), detail.getExecutionPeriod()
                ))
                .toList();
    }

    private CardMiniResponse cardMini(Card card) {
        return new CardMiniResponse(
                card.getId(), card.getCardType(), cardTableMapperHelper.cardTypeName(card.getCardType()),
                card.getStatus(), cardTableMapperHelper.cardStatusName(card.getStatus()), card.getCreatedAt()
        );
    }

    private ActInstitutionResponse institution(Institution institution) {
        if (institution == null) {
            return null;
        }
        return new ActInstitutionResponse(
                institution.getSubjectType(), institution.getTin(), institution.getInstitutionName(),
                institution.getInstitutionAddress(), institution.getInstitutionLegalAddress()
        );
    }

    private EmployeeInfoResponse employee(EmployeeInfo info) {
        if (info == null) {
            return null;
        }
        return new EmployeeInfoResponse(info.getFullName(), info.getPositionId(), info.getPositionUz(), info.getPositionRu());
    }

    private ConditionInfoResponse condition(ConditionInfo info) {
        if (info == null) {
            return null;
        }
        return new ConditionInfoResponse(
                info.getConditionId(),
                info.getDescription() == null ? null : info.getDescription().getUz(),
                info.getDescription() == null ? null : info.getDescription().getRu()
        );
    }

    private PackageTypeInfoResponse packageType(PackageTypeInfo info) {
        if (info == null) {
            return null;
        }
        return new PackageTypeInfoResponse(info.getPackageTypeId(), info.getPackageTypeUz(), info.getPackageTypeRu());
    }

    private ConservationTypeInfoResponse conservationType(ConservationTypeInfo info) {
        if (info == null) {
            return null;
        }
        return new ConservationTypeInfoResponse(
                info.getConservationMethodId(), info.getConservationMethodsUz(), info.getConservationMethodsRu()
        );
    }

    private PurposeResponse purpose(Purpose purpose) {
        if (purpose == null) {
            return null;
        }
        return new PurposeResponse(
                purpose.getPurposeId(), purpose.getSamplingPurposeUz(), purpose.getSamplingPurposeRu(),
                purpose.getSamplingPurposeLoinc()
        );
    }

    private ResearchItemTypeInfoResponse researchItemType(ResearchItemTypeInfo info) {
        if (info == null) {
            return null;
        }
        return new ResearchItemTypeInfoResponse(
                info.getResearchTypeId(), info.getResearchTypeNameUz(), info.getResearchTypeNameRu(),
                info.getCategoryId(), info.getCategoryNameUz(), info.getCategoryNameRu(),
                info.getItemTypeId(), info.getItemTypeNameUz(), info.getItemTypeNameRu()
        );
    }

    private SampleTypeInfoResponse sampleType(SampleTypeInfo info) {
        if (info == null) {
            return null;
        }
        return new SampleTypeInfoResponse(info.getSampleTypeId(), info.getSampleTypeUz(), info.getSampleTypeRu());
    }
}
