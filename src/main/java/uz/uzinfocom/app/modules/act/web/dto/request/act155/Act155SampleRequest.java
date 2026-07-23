package uz.uzinfocom.app.modules.act.web.dto.request.act155;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

@Schema(description = "Отдельная проба, отбираемая в рамках акта 155.")
public record Act155SampleRequest(
        Long id,
        @Size(max = 255) String productName,
        @Size(max = 500) String purposeOfTesting,
        @Size(max = 100) String purposeOfTestingLoinc,
        Long lisOrganizationId,
        @Size(max = 500) String laboratoryAddress,
        @Size(max = 500) String sampleTakenLocation,
        Long sampleQuantity,
        Long productBatchQuantity,
        @Size(max = 500) String appliedPesticides,
        @Size(max = 255) String manufacturer,
        @Size(max = 500) String sampleDocumentJustifying
) implements ChildRequest {
}
