package uz.uzinfocom.isemid.models.entities.act.act153;

import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.Organization;
import uz.uzinfocom.isemid.models.entities.template.BaseEntity;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;

import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "act153_detail")
public class Act153Detail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "object_code", nullable = false)
    private Catalog object;

    @Column(name = "object_code", insertable = false, updatable = false)
    private String objectCode;

    @Column(columnDefinition = "TEXT")
    private String samplingPurpose;

    @Column(name = "sampling_purpose_loinc")
    private String samplingPurposeLoinc; // loinc

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "sampling_depth")
    private Double samplingDepth; // number

    @Column(name = "distance_from_shore")
    private Double distanceFromShore; // number

    @Column(name = "sample_volume")
    private Double sampleVolume; // number

    @Column(name = "sample_location", columnDefinition = "TEXT")
    private String sampleLocation;

    @Column(name = "weather_at_sampling")
    private Double weatherAtSampling; // number (Celsius)

    @Column(name = "water_temperature")
    private Double waterTemperature; // number (Celsius)

    @Column(name = "sample_type", columnDefinition = "TEXT")
    private String sampleType;

    @Column(name = "sampling_date")
    private LocalDateTime samplingDate; // datetime

    @Column(name = "special_sampling_conditions", columnDefinition = "TEXT")
    private String specialSamplingConditions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "organization_id", insertable = false, updatable = false)
    private Long organizationId;

    @Column(name = "laboratory_address", columnDefinition = "TEXT")
    private String laboratoryAddress;

    @Column(name = "delivery_conditions", columnDefinition = "TEXT")
    private String deliveryConditions;

    @Column(name = "storage_conditions", columnDefinition = "TEXT")
    private String storageConditions;

    @Column(name = "conservation_methods", columnDefinition = "TEXT")
    private String conservationMethods;

    @Column(name = "additional_info", columnDefinition = "TEXT")
    private String additionalInfo;
}
