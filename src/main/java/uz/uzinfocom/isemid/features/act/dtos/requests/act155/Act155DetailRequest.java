package uz.uzinfocom.isemid.features.act.dtos.requests.act155;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act155DetailRequest {
    private Long id;
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

