package uz.uzinfocom.isemid.features.act.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    Integer actTemplateId, // @NotNull(message = "act template id must not be null")
    Priority priority, //   @NotNull(message = "priority must not be null")
    String tin, //front
    String organizationName, //front
    String organizationAddress, //front
    String organizationLegalAddress, //front
    Purpose purpose,  // @NotNull(message = "purpose must not be null") //front
    LocalDateTime sampleTakenDate, // front
    LocalDate deliveryDateToLaboratory,  // front
    Conditions conditions, // @NotNull(message = "conditions must not be null")  //front
    String additionalInformation,  // front
    String involvedPersonName, // front
    Integer involvedProfessionId, // front
    PackageType packageType, // front
    String document, // front
    String goal, //  front
    NoteConditions noteConditions, //front
    String fullNameOfDoctor, // isemisda current userdan ///////////////////////////////
    Integer collectorProfessionId, // front
    String manufacturer, // front
    LocalDate manufactureDate, // front
    String docNumber, // front
    PreservationMethod preservationMethod, //front
    String redirectUrl, // isemis berishi kk
    Boolean paid, // front (dropdown)
    List<SelectionActItem> selectionActItems, // @NotEmpty(message = "selection act items must not be null")
    String locationOwnership // front
    ) {


  public record SelectionActItem(
      Integer itemTypeId, // front
      String groupSize, // front
      String sampleWeight, // front
      String samplingAddress, // front
      String samplingDepth, // front
      LengthUnit depthUnit, // front
      String distanceFromShore, // front
      LengthUnit distanceFromShoreUnit, //front
      String weatherConditions,  // front
      String waterTemperature, // front
      String samplingPoint, // front
      SampleType sampleType, // front
      SampleQtUnit sampleQtUnit, // front
      String sampleQt, // front
      List<String> coordinates // front
  ) {}

  public record Conditions(Integer id, String name) {}

  public record PreservationMethod(Integer id, String name) {}

  public record Purpose(Integer id, String name) {}

  public record PackageType(Integer id, String name) {}

  public record NoteConditions(Integer id, String name) {}

  public record SampleType(Integer id, String name) {}

  public enum Priority {
    IMMEDIATE,
    URGENT,
    SCHEDULED
  }

  public enum LengthUnit {
    CENTIMETER,
    METER,
    KILOMETER
  }

  public enum SampleQtUnit {
    MILLIGRAM, GRAM, KILOGRAM
  }

}
