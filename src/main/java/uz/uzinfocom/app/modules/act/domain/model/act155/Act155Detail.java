package uz.uzinfocom.app.modules.act.domain.model.act155;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.app.platform.persistence.entity.UuidAuditableEntity;

@Getter
@Setter
@Entity
@Table(
        name = "act155_detail",
        indexes = @Index(name = "idx_act155_detail_act155_id", columnList = "act155_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class Act155Detail extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "act155_id", nullable = false, foreignKey = @ForeignKey(name = "fk_act155_detail_act155"))
    private Act155 act155;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "purpose_of_testing")
    private String purposeOfTesting;

    @Column(name = "purpose_of_testing_loinc")
    private String purposeOfTestingLoinc;

    @Column(name = "lis_organization_id")
    private Long lisOrganizationId;

    @Column(name = "laboratory_address")
    private String laboratoryAddress;

    @Column(name = "sample_taken_location")
    private String sampleTakenLocation;

    @Column(name = "sample_quantity")
    private Long sampleQuantity;

    @Column(name = "product_batch_quantity")
    private Long productBatchQuantity;

    @Column(name = "applied_pesticides")
    private String appliedPesticides;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "sample_document_justifying")
    private String sampleDocumentJustifying;
}
