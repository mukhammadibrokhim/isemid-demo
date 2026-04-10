package uz.uzinfocom.isemid.features.act.dtos;

import java.util.List;
import lombok.Builder;

@Builder
public record LisActPushRequest(
    String locationOwnership,
    String tin,
    String organizationName,
    String organizationAddress,
    String organizationLegalAddress,
    String sampleTakenDate,
    String deliveryDateToLaboratory,
    Conditions conditions, // @NotNull(message = "conditions must not be null")
    PreservationMethod preservationMethod,
    Purpose purpose,  // @NotNull(message = "purpose must not be null")
    String additionalInformation,
    String fullNameOfDoctor,
    Integer collectorProfessionId,
    String involvedPersonName,
    Integer involvedProfessionId,
    Boolean qualityControl,
    String manufacturer,
    String manufactureDate,
    String docNumber,
    PackageType packageType,
    String document,
    String goal,
    Boolean paid,
    Priority priority, //   @NotNull(message = "priority must not be null")
    NoteConditions noteConditions,
    Integer actTemplateId, // @NotNull(message = "act template id must not be null")
    Integer applicationId,
    AirSelectionAct airSelectionAct,
    List<SelectionActItem> selectionActItems, // @NotEmpty(message = "selection act items must not be null")
    String isemidData,
    String redirectUrl
) {

  public record AirSelectionAct(
      List<Integer> equipmentIds,
      String preservationMethod,
      String airSamplingMethod,
      Boolean researchFiltersRequired,
      Integer roomArea,
      Integer roomVolume,
      Integer workersCount,
      AirMoisture airMoisture,
      HeatEmission heatEmission,
      String airSampleType,
      Integer technologicalDescription,
      String samplingConditions,
      String workMode,
      String manualOperations,
      String pollutionSourceDescription,
      Integer sketchFileId
  ) {}


  public record SelectionActItem(
      Integer itemTypeId,
      SampleType sampleType,
      String samplingPoint,
      String groupSize,
      String sampleWeight,
      String sampleQt,
      String sampleQtUnit,
      String weatherConditions,
      String waterTemperature,
      String samplingAddress,
      List<String> coordinates,
      String samplingDepth,
      String depthUnit,
      String distanceFromShore,
      String distanceFromShoreUnit,
      String description
  ) {}

  public record Conditions(Integer id, String name) {}

  public record PreservationMethod(Integer id, String name) {}

  public record Purpose(Integer id, String name) {}

  public record PackageType(Integer id, String name) {}

  public record NoteConditions(Integer id, String name) {}

  public record AirMoisture(Integer id, String name) {}

  public record HeatEmission(Integer id, String name) {}

  public record SampleType(Integer id, String name) {}

  public enum Priority {
    IMMEDIATE,
    URGENT,
    SCHEDULED
  }

}
