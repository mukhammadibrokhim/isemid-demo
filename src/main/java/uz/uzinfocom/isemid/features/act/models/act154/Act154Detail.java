package uz.uzinfocom.isemid.features.act.models.act154;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

import java.time.LocalDateTime;
import java.util.Date;
import uz.uzinfocom.isemid.features.act.dtos.SampleQtUnit;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "act154_detail")
public class Act154Detail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "act154_id")  // Foreign key to Act154
    @JsonBackReference
    private Act154 act154;


    @Column(name = "item_type_id")
    private Integer itemTypeId;

    private Long lisOrganizationId;
    private String laboratoryAddress;

    private String shiftCode;
    private String sampleName;
    private String sampleType;
    private Integer samplingPurposeId;
    private String purposeOfSampling;
    private String samplingPurposeLoinc;
    private LocalDateTime sampleTakenDate;
    private Integer deliveryConditionId;
    private String deliveryTerm;
    private Integer storageConditionId;
    private String storageCondition;
    private String manufacturingCompany;
    private Date manufactureDate;
    private Long groupSize;
    private Long serialNumberOfGroup;
    private Double sampleWeight;
    @Enumerated(EnumType.STRING)
    private SampleQtUnit sampleWeightUnit;
    private Double sampleVolume;
    private Long docNumberOfTakenObject;
    private Integer packageTypeId;
    private String packageType;
    private String documentConfirmSampling;
    private String purposeOfChecking;
    private String note;
}
