package uz.uzinfocom.app.modules.patient.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.modules.patient.domain.enums.AddressType;
import uz.uzinfocom.app.platform.persistence.entity.AbsEntity;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "pt_address",
        indexes = {
                @Index(
                        name = "idx_address_patient",
                        columnList = "patient_id"
                ),
                @Index(
                        name = "idx_address_location",
                        columnList = "region_code, district_code, neighborhood_code"
                )
        }
)
public class PatientAddress extends AbsEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private AddressType type;

    @Column(name = "region_code")
    private String regionCode;

    @Column(name = "district_code")
    private String districtCode;

    @Column(name = "neighborhood_code")
    private String neighborhoodCode;

    @Column(name = "street_address")
    private String streetAddress;

    @Column(name = "house_number")
    private String houseNumber;

    @Column(name = "apartment_number")
    private String apartmentNumber;
}