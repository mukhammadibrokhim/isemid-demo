package uz.uzinfocom.isemid.models.payload.responses.act.act155;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Resp155Detail{
    private String productName;
    private String purposeOfTesting;
    private String purposeOfTestingLoinc;//loinc
    private String laboratoryAddress;
    private String sampleTakenLocation;
    private Long sampleQuantity;
    private Long organizationId;
    private Long productBatchQuantity;
    private String appliedPesticides;
    private String manufacturer;
    private String sampleDocumentJustifying;
}
