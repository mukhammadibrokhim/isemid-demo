package uz.uzinfocom.app.modules.act.application.query.dto.detail.act155;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Отдельная проба, отобранная в рамках акта 155.")
public record Act155SampleResponse(
        Long id,
        String productName,
        String purposeOfTesting,
        String purposeOfTestingLoinc,
        Long lisOrganizationId,
        String laboratoryAddress,
        String sampleTakenLocation,
        Long sampleQuantity,
        Long productBatchQuantity,
        String appliedPesticides,
        String manufacturer,
        String sampleDocumentJustifying
) {
}
