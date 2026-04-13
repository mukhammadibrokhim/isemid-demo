package uz.uzinfocom.isemid.features.act.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record LisActPushRequest(
    Integer actTemplateId, // @NotNull(message = "act template id must not be null")
    Priority priority, //   @NotNull(message = "priority must not be null")
    String tin,
    String organizationName,
    String organizationAddress,
    String organizationLegalAddress,
    Purpose purpose,  // @NotNull(message = "purpose must not be null")
    LocalDateTime sampleTakenDate,
    String deliveryDateToLaboratory,
    Conditions conditions, // @NotNull(message = "conditions must not be null")
    String additionalInformation,
    String involvedPersonName,
    Integer involvedProfessionId,
    PackageType packageType,
    String document,
    String goal,
    NoteConditions noteConditions,
    String fullNameOfDoctor,
    Integer collectorProfessionId,
    String manufacturer,
    LocalDate manufactureDate,
    String docNumber,
    PreservationMethod preservationMethod,
    String redirectUrl,
    Boolean paid,
    Boolean qualityControl,
    List<SelectionActItem> selectionActItems, // @NotEmpty(message = "selection act items must not be null")

    String locationOwnership
    ) {


  public record SelectionActItem(
      Integer itemTypeId,
      String groupSize,
      String sampleWeight,
      String samplingAddress,
      String samplingDepth,
      LengthUnit depthUnit,
      String distanceFromShore,
      LengthUnit distanceFromShoreUnit,
      String weatherConditions,
      String waterTemperature,
      String samplingPoint,
      SampleType sampleType,
      SampleQtUnit sampleQtUnit,
      List<String> coordinates,
      String sampleQt
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
