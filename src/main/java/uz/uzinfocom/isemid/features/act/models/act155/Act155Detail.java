package uz.uzinfocom.isemid.features.act.models.act155;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.commons.models.BaseEntity;


@Setter
@Getter
@Entity
@SuperBuilder
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "act155_detail")
public class Act155Detail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "act155_id")  // Foreign key to Act154
    private Act155 act155;

    private String productName;
    private String purposeOfTesting;
    private String purposeOfTestingLoinc;//loinc

    private Long lisOrganizationId;
    private String laboratoryAddress;

    private String sampleTakenLocation;
    private Long sampleQuantity;
    private Long productBatchQuantity;
    private String appliedPesticides;
    private String manufacturer;
    private String sampleDocumentJustifying;
}
