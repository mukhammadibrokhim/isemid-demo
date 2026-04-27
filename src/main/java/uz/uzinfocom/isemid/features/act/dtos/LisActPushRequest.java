package uz.uzinfocom.isemid.features.act.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.Builder;
import lombok.With;


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
 * @param packageType
 * @param document
 * @param goal
 * @param noteConditions
 * @param fullNameOfDoctor
 * @param collectorProfessionId
 * @param manufacturer
 * @param manufactureDate
 * @param docNumber
 * @param preservationMethod
 * @param redirectUrl
 * @param paid
 * @param selectionActItems
 * @param locationOwnership
 *
 * only @param fullNameOfDoctor and
 * redirectUrl is taken from isemis
 */
@Builder
@With
public record LisActPushRequest(
    Integer actTemplateId,// @NotNull(message = "act template id must not be null") // get from lis and map to local enum
    Priority priority, //   @NotNull(message = "priority must not be null") // from send pop up
    String tin, // from backend act.tin
    String organizationName, //from backend act.institutionName
    String organizationAddress, //from backend act.institutionAddress
    String organizationLegalAddress, //from backend act.institutionAddress
    LisDataDictionary purpose,// @NotNull(message = "purpose must not be null") // act.details.purposeId ; act.details.samplingPurpose
    @JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss"
    )
    ZonedDateTime sampleTakenDate, // act.details.samplingDate
    @JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss"
    )
    ZonedDateTime deliveryDateToLaboratory,  // act.deliveredDate
    LisDataDictionary conditions,// @NotNull(message = "conditions must not be null")  // act.details.deliveryConditions+act.details.storageConditions
    String additionalInformation,  // act.additionalInfo
    String involvedPersonName,// act153.participantFullName || act154.participantFullName || act223.fullNameOfParticipant
    Integer involvedProfessionId,// act153.participantPosition || act154.participantPositionId || act223.participantPositionId
    LisDataDictionary packageType, // act154.packageTypeId; act154.packageType
    String document, // act153.samplingDocuments
    String goal, //  act154.details.purposeOfSampling
    LisDataDictionary noteConditions, //act153.details
    String fullNameOfDoctor, // isemisda current userdan ///////////////////////////////
    Integer collectorProfessionId, // act.positionId
    String manufacturer, // act154.detail.manufacturingCompany
    LocalDate manufactureDate, // act154.detail.manufactureDate
    String docNumber, // act.docNumberOfTakenObject
    LisDataDictionary preservationMethod,// act153.details.conservationMethodId; act153.details.conservationMethods
    String redirectUrl, // isemis berishi kk
    Boolean paid, // from send pop up
    List<SelectionActItem> selectionActItems,// @NotEmpty(message = "selection act items must not be null")
    String locationOwnership
    ) {


  public record SelectionActItem(
      Integer itemTypeId, // act153.details.objectTypeId
      String groupSize, // act154.details.groupSize
      String sampleWeight, // act154.details.sampleWeight ||
      String samplingAddress, // act153.details.address
      String samplingDepth, // act153.details.depth
      LengthUnit depthUnit,
      String distanceFromShore, // act153.details.distanceFromShore
      LengthUnit distanceFromShoreUnit, //enum front
      String weatherConditions,  // act153.details.weatherAtSampling
      String waterTemperature, // act153.details.waterTemperature
      String samplingPoint, // ??????????????????????????
      LisDataDictionary sampleType, // act153.details.sampleTypeId and act153.details.sampleType
      SampleQtUnit sampleQtUnit,
      String sampleQt, // act223.details.amount
      List<String> coordinates
  ) {}

  public record LisDataDictionary(Integer id, String name){}

}
