package uz.uzinfocom.isemid.features.act.models.act154;

import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.commons.models.BaseEntity;
import uz.uzinfocom.isemid.features.organization.models.Organization;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "act154_detail")
public class Act154SampleDetails extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "act154_id")  // Foreign key to Act154
    private Act154 act154;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "organization_id", insertable = false, updatable = false)
    private Long organizationId;

    private String shiftCode;
    private String sampleName;
    private String sampleType;
    private String purposeOfSampling;
    private String samplingPurposeLoinc;
    private LocalDateTime sampleTakenDate;
    private String laboratoryAddress;
    private String deliveryTerm;
    private String storageCondition;
    private String manufacturingCompany;
    private Date manufactureDate;
    private Long groupSize;
    private Long serialNumberOfGroup;
    private Double sampleWeight;
    private Double sampleVolume;
    private Long docNumberOfTakenObject;
    private String packageType;
    private String documentConfirmSampling;
    private String purposeOfChecking;
    private String note;
}
