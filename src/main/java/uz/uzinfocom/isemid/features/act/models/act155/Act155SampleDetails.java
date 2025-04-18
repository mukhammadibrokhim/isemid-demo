package uz.uzinfocom.isemid.features.act.models.act155;

import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.commons.models.BaseEntity;
import uz.uzinfocom.isemid.features.organization.models.Organization;


@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "act155_detail")
public class Act155SampleDetails extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "act155_id")  // Foreign key to Act154
    private Act155 act155;

    private String productName;
    private String purposeOfTesting;
    private String purposeOfTestingLoinc;//loinc

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "organization_id", insertable = false, updatable = false)
    private Long organizationId;

    private String laboratoryAddress;
    private String sampleTakenLocation;
    private Long sampleQuantity;
    private Long productBatchQuantity;
    private String appliedPesticides;
    private String manufacturer;
    private String sampleDocumentJustifying;
}
