package uz.uzinfocom.isemid.features.integrations.lis.dto;

import java.util.List;
import lombok.Builder;
import lombok.With;
import uz.uzinfocom.isemid.features.act.dtos.LengthUnit;
import uz.uzinfocom.isemid.features.act.dtos.Priority;
import uz.uzinfocom.isemid.features.act.dtos.SampleQtUnit;


/**
 *
 * @param actTemplateId
 * @param priority
 * @param tin
 * @param organizationName
 * @param organizationAddress
 * @param organizationLegalAddress
 * @param purpose
 * @param sampleTakenDate
 * @param deliveryDateToLaboratory
 * @param conditions
 * @param additionalInformation
 * @param involvedPersonName
 * @param involvedProfessionId
 * @param document
 * @param goal
 * @param noteConditions
 * @param fullNameOfDoctor
 * @param collectorProfessionId
 * @param docNumber
 * @param preservationMethod
 * @param redirectUrl
 * @param paid
 * @param selectionActItems
 * @param locationOwnership
 *
 * only @param fullNameOfDoctor and
 * redirectUrl is taken from isemis
 *
 * locationOwnership is intentionally left unset: LIS's full enum contract
 * for this field is not yet confirmed, and Institution.subjectType is not
 * a reliable 1:1 mapping.
 */
@Builder
@With
public record CreateLisActRequest(
    Long actTemplateId,// @NotNull(message = "act template id must not be null") // get from lis and map to local enum
    Priority priority, //   @NotNull(message = "priority must not be null") // from send pop up
    String tin, // from backend act.tin
    String organizationName, //from backend act.institutionName
    String organizationAddress, //from backend act.institutionAddress
    String organizationLegalAddress, //from backend act.institutionAddress
    LisDataDictionary purpose,// @NotNull(message = "purpose must not be null") // act.details.purposeId ; act.details.samplingPurpose
    String sampleTakenDate, // act.details.samplingDate
    String deliveryDateToLaboratory,  // act.deliveredDate
    LisDataDictionary conditions,// @NotNull(message = "conditions must not be null")  // act.details.deliveryConditions+act.details.storageConditions
    String additionalInformation,  // act.additionalInfo
    String involvedPersonName,// act153.participantFullName || act154.participantFullName || act223.fullNameOfParticipant
    Integer involvedProfessionId,// act153.participantPosition || act154.participantPositionId || act223.participantPositionId
    String document, // act153.samplingDocuments
    String goal, //  act154.details.purposeOfSampling
    LisDataDictionary noteConditions, //act153.details
    String fullNameOfDoctor, // isemisda current userdan ///////////////////////////////
    Integer collectorProfessionId, // act.positionId
    String docNumber, // act.docNumberOfTakenObject
    LisDataDictionary preservationMethod,// act153.details.conservationMethodId; act153.details.conservationMethods
    String redirectUrl, // isemis berishi kk
    Boolean paid, // from send pop up
    List<SelectionActItem> selectionActItems,// @NotEmpty(message = "selection act items must not be null")
    String locationOwnership
    ) {


  public record SelectionActItem(
      Integer itemTypeId, // act153.details.objectTypeId
      Integer itemOrder, // position within selectionActItems, assigned during mapping
      String brandName, // not tracked in EMIS
      LisDataDictionary sampleType, // act153.details.sampleTypeId and act153.details.sampleType
      String samplingPoint, // ??????????????????????????
      String manufacturer, // act154.detail.manufacturingCompany
      String manufactureDate, // act154.detail.manufactureDate
      LisDataDictionary packageType, // act154.packageTypeId; act154.packageType (per-item in LIS's current contract, broadcast from act-level value)
      String groupSize, // act154.details.groupSize
      String sampleWeight, // act154.details.sampleWeight ||
      String sampleQt, // act223.details.amount
      SampleQtUnit sampleQtUnit,
      String weatherConditions,  // act153.details.weatherAtSampling
      String waterTemperature, // act153.details.waterTemperature
      String samplingAddress, // act153.details.address
      List<String> coordinates,
      String samplingDepth, // act153.details.depth
      LengthUnit depthUnit,
      String distanceFromShore, // act153.details.distanceFromShore
      LengthUnit distanceFromShoreUnit, //enum front
      String description, // not tracked in EMIS
      String samplingAdditives // not tracked in EMIS
  ) {}

  public record LisDataDictionary(Integer id, String name){}

}
