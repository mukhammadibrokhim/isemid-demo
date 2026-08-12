package uz.uzinfocom.isemid.features.act.mappers;

import static java.time.ZoneOffset.UTC;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.act.dtos.ActLisDto;
import uz.uzinfocom.isemid.features.act.dtos.ActLisDto.SelectionActItemDto;
import uz.uzinfocom.isemid.features.act.dtos.LisActPushRequest;
import uz.uzinfocom.isemid.features.act.dtos.LisActPushRequest.LisDataDictionary;
import uz.uzinfocom.isemid.features.act.dtos.LisActPushRequest.SelectionActItem;
import uz.uzinfocom.isemid.features.act.dtos.LisRequest;

@Component
public class EMisToLisRequestMapper {

    /**
     * Merges the request-level fields ({@code priority}, {@code paid}) with the
     * act data captured in {@link ActLisDto} to produce the payload sent to LIS.
     *
     * <p>{@code fullNameOfDoctor} and {@code redirectUrl} are intentionally left
     * null here; callers must set them via the {@code @With} methods on the result.
     */
    public LisActPushRequest map(LisRequest lisRequest, ActLisDto actLisDto) {
        String manufactureDate = formatManufactureDate(actLisDto);

        return LisActPushRequest.builder()
            .actTemplateId(actLisDto.actTemplateId())
            .priority(lisRequest.priority())
            .tin(actLisDto.tin())
            .organizationName(actLisDto.organizationName())
            .organizationAddress(actLisDto.organizationAddress())
            .organizationLegalAddress(actLisDto.organizationLegalAddress())
            .purpose(actLisDto.purpose())
            .sampleTakenDate(actLisDto.sampleTakenDateTime()
                .atZone(ZoneId.of("Asia/Tashkent"))
                .withZoneSameInstant(UTC).format(DateTimeFormatter.ISO_INSTANT))
            .deliveryDateToLaboratory(actLisDto.deliveryDateTime()
                .atZone(ZoneId.of("Asia/Tashkent"))
                .withZoneSameInstant(UTC).format(DateTimeFormatter.ISO_INSTANT))
            .conditions(actLisDto.conditions())
            .additionalInformation(actLisDto.additionalInformation())
            .involvedPersonName(actLisDto.involvedPersonName())
            .involvedProfessionId(actLisDto.involvedProfessionId())
            .document(actLisDto.document())
            .goal(actLisDto.goal())
            .noteConditions(actLisDto.noteConditions())
            .collectorProfessionId(actLisDto.collectorProfessionId())
            .docNumber(actLisDto.docNumber())
            .preservationMethod(actLisDto.preservationMethod())
            .paid(lisRequest.paid())
            .selectionActItems(toSelectionActItems(
                actLisDto.selectionActItems(),
                actLisDto.packageType(),
                actLisDto.manufacturer(),
                manufactureDate
            ))
            .build();
    }

    private String formatManufactureDate(ActLisDto actLisDto) {
        if (actLisDto.manufactureDate() == null) {
            return null;
        }

        return actLisDto.manufactureDate().atStartOfDay()
            .atZone(ZoneId.of("Asia/Tashkent"))
            .withZoneSameInstant(UTC).format(DateTimeFormatter.ISO_INSTANT);
    }

    private List<SelectionActItem> toSelectionActItems(
        List<SelectionActItemDto> items,
        LisDataDictionary packageType,
        String manufacturer,
        String manufactureDate
    ) {
        if (items == null) return List.of();
        return IntStream.range(0, items.size())
            .mapToObj(index -> toSelectionActItem(items.get(index), index, packageType, manufacturer, manufactureDate))
            .toList();
    }

    private SelectionActItem toSelectionActItem(
        SelectionActItemDto dto,
        int order,
        LisDataDictionary packageType,
        String manufacturer,
        String manufactureDate
    ) {
        return new SelectionActItem(
            dto.itemTypeId(),
            order,
            null,
            dto.sampleType(),
            dto.samplingPoint(),
            manufacturer,
            manufactureDate,
            packageType,
            dto.groupSize(),
            dto.sampleWeight(),
            dto.sampleQt(),
            dto.sampleQtUnit(),
            dto.weatherConditions(),
            dto.waterTemperature(),
            dto.samplingAddress(),
            ActLisDto.mapToLocation(dto.location()),
            dto.samplingDepth(),
            dto.depthUnit(),
            dto.distanceFromShore(),
            dto.distanceFromShoreUnit(),
            null,
            null
        );
    }
}
