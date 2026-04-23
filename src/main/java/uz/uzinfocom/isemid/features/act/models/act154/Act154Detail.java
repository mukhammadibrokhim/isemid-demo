package uz.uzinfocom.isemid.features.act.models.act154;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Embedded;
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
import uz.uzinfocom.isemid.features.act.dtos.SampleQtUnit;
import uz.uzinfocom.isemid.features.act.models.common.ResearchItemTypeInfo;

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

    @Embedded
    private ResearchItemTypeInfo researchItemTypeInfo;

    private String shiftCode;
    private String sampleName;

    private Long groupSize;
    private Long serialNumberOfGroup;
    private Double sampleWeight;
    @Enumerated(EnumType.STRING)
    private SampleQtUnit sampleWeightUnit;
    private Double sampleVolume;
    private String note;
}
