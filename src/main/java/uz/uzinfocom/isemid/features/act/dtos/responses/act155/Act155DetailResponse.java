package uz.uzinfocom.isemid.features.act.dtos.responses.act155;

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
public class Act155DetailResponse {
    private Long id;
    private String productName;
    private String purposeOfTesting;
    private String purposeOfTestingLoinc;//loinc
    private String sampleTakenLocation;
    private Long sampleQuantity;
    private Long lisOrganizationId;
    private String laboratoryAddress;
    private Long productBatchQuantity;
    private String appliedPesticides;
    private String manufacturer;
    private String sampleDocumentJustifying;
}

