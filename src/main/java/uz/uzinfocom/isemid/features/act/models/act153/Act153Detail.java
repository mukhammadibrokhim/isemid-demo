package uz.uzinfocom.isemid.features.act.models.act153;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

import java.time.LocalDateTime;
import uz.uzinfocom.isemid.features.act.dtos.LengthUnit;
import uz.uzinfocom.isemid.features.act.dtos.SampleQtUnit;


@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "act153_detail")
public class Act153Detail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "act153_id")
    @JsonBackReference
    private Act153 act153;

    private Integer researchTypeId;
    private Integer categoryId;

    @Column(name = "item_type_id")
    private Integer itemTypeId;
    private String itemTypeNameUz;
    private String itemTypeNameRu;

    @Column(name = "object_type_id")
    private Integer objectTypeId;

    @Column(name = "object_code")
    private String objectCode;

    @Column(name = "purpose_id")
    private Integer purposeId;

    @Column(name = "sampling_purpose_uz", columnDefinition = "TEXT")
    private String samplingPurposeUz;

    @Column(name = "sampling_purpose_ru", columnDefinition = "TEXT")
    private String samplingPurposeRu;

    @Column(name = "sampling_purpose_loinc")
    private String samplingPurposeLoinc; // loinc

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "sampling_depth")
    private Double samplingDepth; // number

    @Enumerated(EnumType.STRING)
    private LengthUnit depthUnit;

    @Column(name = "distance_from_shore")
    private Double distanceFromShore; // number

    @Enumerated(EnumType.STRING)
    private LengthUnit distanceFromShoreUnit;

    @Column(name = "sample_volume")
    private Double sampleVolume; // number

    @Enumerated(EnumType.STRING)
    private SampleQtUnit sampleQtUnit;

    @Column(name = "sample_location", columnDefinition = "TEXT")
    private String sampleLocation;

    @Column(name = "weather_at_sampling")
    private Double weatherAtSampling; // number (Celsius)

    @Column(name = "water_temperature")
    private Double waterTemperature; // number (Celsius)

    @Column(name = "sample_type_id")
    private Integer sampleTypeId;

    @Column(name = "sample_type_uz", columnDefinition = "TEXT")
    private String sampleTypeUz;

    @Column(name = "sample_type_ru", columnDefinition = "TEXT")
    private String sampleTypeRu;

    @Column(name = "sampling_date_time")
    private LocalDateTime samplingDateTime; // datetime

    @Column(name = "special_condition_id")
    private Integer specialConditionId;

    @Column(name = "special_sampling_conditions_uz", columnDefinition = "TEXT")
    private String specialSamplingConditionsUz;

    @Column(name = "special_sampling_conditions_ru", columnDefinition = "TEXT")
    private String specialSamplingConditionsRu;

    private Long lisOrganizationId;

    @Column(name = "laboratory_address", columnDefinition = "TEXT")
    private String laboratoryAddress;

    @Column(name = "delivery_condition_id")
    private Integer deliveryConditionId;

    @Column(name = "delivery_conditions_uz", columnDefinition = "TEXT")
    private String deliveryConditionsUz;

    @Column(name = "delivery_conditions_ru", columnDefinition = "TEXT")
    private String deliveryConditionsRu;

    @Column(name = "storage_condition_id")
    private Integer storageConditionId;

    @Column(name = "storage_conditions_uz", columnDefinition = "TEXT")
    private String storageConditionsUz;

    @Column(name = "storage_conditions_ru", columnDefinition = "TEXT")
    private String storageConditionsRu;

    @Column(name = "conservation_method_id")
    private Integer conservationMethodId;

    @Column(name = "conservation_methods_uz", columnDefinition = "TEXT")
    private String conservationMethodsUz;

    @Column(name = "conservation_methods_ru", columnDefinition = "TEXT")
    private String conservationMethodsRu;

    @Column(name = "additional_info", columnDefinition = "TEXT")
    private String additionalInfo;
}
