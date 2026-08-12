package uz.uzinfocom.isemid.features.act.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import uz.uzinfocom.isemid.features.integrations.lis.dto.CreateLisActRequest;
import uz.uzinfocom.isemid.features.integrations.lis.dto.CreateLisActRequest.LisDataDictionary;

/**
 * Intermediate DTO populated from the Act entity (and its loaded detail collection)
 * before final mapping to {@link CreateLisActRequest}.
 * <p>
 * Fields that come from the HTTP request itself (priority, paid, fullNameOfDoctor,
 * redirectUrl, locationOwnership) are intentionally absent here.
 */
@Builder
public record ActLisDto(
    Long actTemplateId,
    String tin,
    String organizationName,
    String organizationAddress,
    String organizationLegalAddress,
    LisDataDictionary purpose,
    LocalDateTime sampleTakenDateTime,
    LocalDateTime deliveryDateTime,
    LisDataDictionary conditions,
    String additionalInformation,
    String involvedPersonName,
    Integer involvedProfessionId,
    LisDataDictionary packageType,
    String document,
    String goal,
    LisDataDictionary noteConditions,
    Integer collectorProfessionId,
    String manufacturer,
    LocalDate manufactureDate,
    String docNumber,
    LisDataDictionary preservationMethod,
    List<SelectionActItemDto> selectionActItems
) {

    @Builder
    public record SelectionActItemDto(
        Integer itemTypeId,
        String location,
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
        LisDataDictionary sampleType,
        String sampleQt,
        SampleQtUnit sampleQtUnit
    ) {}

    public static List<String> mapToLocation(String location) {
        return Optional.ofNullable(location)
            .filter(s -> !s.isBlank())
            .map(s -> Arrays.asList(s.trim().split("\\s+")))
            .orElse(List.of());
    }
}
