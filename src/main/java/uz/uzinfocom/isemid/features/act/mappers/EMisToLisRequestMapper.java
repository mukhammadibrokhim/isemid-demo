package uz.uzinfocom.isemid.features.act.mappers;

import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.act.dtos.ActLisDto;
import uz.uzinfocom.isemid.features.act.dtos.ActLisDto.SelectionActItemDto;
import uz.uzinfocom.isemid.features.act.dtos.LisActPushRequest;
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
        return LisActPushRequest.builder()
            .actTemplateId(actLisDto.actTemplateId())
            .priority(lisRequest.priority())
            .tin(actLisDto.tin())
            .organizationName(actLisDto.organizationName())
            .organizationAddress(actLisDto.organizationAddress())
            .organizationLegalAddress(actLisDto.organizationLegalAddress())
            .purpose(actLisDto.purpose())
            .sampleTakenDate(actLisDto.sampleTakenDateTime().atZone(ZoneId.of("Asia/Tashkent")))
            .deliveryDateToLaboratory(actLisDto.deliveryDateTime().atZone(ZoneId.of("Asia/Tashkent")))
            .conditions(actLisDto.conditions())
            .additionalInformation(actLisDto.additionalInformation())
            .involvedPersonName(actLisDto.involvedPersonName())
            .involvedProfessionId(actLisDto.involvedProfessionId())
            .packageType(actLisDto.packageType())
            .document(actLisDto.document())
            .goal(actLisDto.goal())
            .noteConditions(actLisDto.noteConditions())
            .collectorProfessionId(actLisDto.collectorProfessionId())
            .manufacturer(actLisDto.manufacturer())
            .manufactureDate(actLisDto.manufactureDate())
            .docNumber(actLisDto.docNumber())
            .preservationMethod(actLisDto.preservationMethod())
            .paid(lisRequest.paid())
            .selectionActItems(toSelectionActItems(actLisDto.selectionActItems()))
            .build();
    }

    private List<SelectionActItem> toSelectionActItems(List<SelectionActItemDto> items) {
        if (items == null) return List.of();
        return items.stream().map(this::toSelectionActItem).toList();
    }

    private SelectionActItem toSelectionActItem(SelectionActItemDto dto) {
        return new SelectionActItem(
            dto.itemTypeId(),
            dto.groupSize(),
            dto.sampleWeight(),
            dto.samplingAddress(),
            dto.samplingDepth(),
            dto.depthUnit(),
            dto.distanceFromShore(),
            dto.distanceFromShoreUnit(),
            dto.weatherConditions(),
            dto.waterTemperature(),
            dto.samplingPoint(),
            dto.sampleType(),
            dto.sampleQtUnit(),
            dto.sampleQt(),
            List.of(dto.location())
        );
    }
}
