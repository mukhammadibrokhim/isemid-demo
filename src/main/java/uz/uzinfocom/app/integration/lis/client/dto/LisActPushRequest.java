package uz.uzinfocom.app.integration.lis.client.dto;

import lombok.Builder;
import uz.uzinfocom.app.modules.act.domain.enums.LengthUnit;
import uz.uzinfocom.app.modules.act.domain.enums.SampleQtUnit;

import java.util.List;

/**
 * The body LIS expects when we submit an act for laboratory processing.
 * This is an external contract — field names and shapes are LIS's, not ours,
 * so they are mapped from our domain rather than mirroring it.
 *
 * <p>Most fields come from the act itself (see {@code ActLisPayloadMapper});
 * {@code priority} and {@code paid} come from the employee's send dialog,
 * {@code fullNameOfDoctor} from the authenticated user, and
 * {@code redirectUrl} is where LIS should post the result back to us.
 */
@Builder
public record LisActPushRequest(

        /** LIS's own template id for this research family, looked up per act type. */
        Integer actTemplateId,

        LisPriority priority,

        String tin,
        String organizationName,
        String organizationAddress,
        String organizationLegalAddress,

        LisDataDictionary purpose,

        /** ISO-8601 instant — LIS takes UTC strings, not local date-times. */
        String sampleTakenDate,
        String deliveryDateToLaboratory,

        LisDataDictionary conditions,
        String additionalInformation,

        String involvedPersonName,
        Integer involvedProfessionId,

        LisDataDictionary packageType,
        String document,
        String goal,
        LisDataDictionary noteConditions,

        String fullNameOfDoctor,
        Integer collectorProfessionId,

        String manufacturer,
        String manufactureDate,
        String docNumber,

        LisDataDictionary preservationMethod,

        /** Where LIS posts the result once the laboratory work is done. */
        String redirectUrl,

        Boolean paid,

        List<SelectionActItem> selectionActItems
) {

    /**
     * One collected sample within the act.
     */
    @Builder
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
            LisDataDictionary sampleType,
            SampleQtUnit sampleQtUnit,
            String sampleQt,
            List<String> coordinates
    ) {
    }

    /**
     * LIS's generic "reference entry" shape: an id plus its display name.
     * Our embeddables already carry both, so the mapping is direct.
     */
    public record LisDataDictionary(Integer id, String name) {
    }
}
