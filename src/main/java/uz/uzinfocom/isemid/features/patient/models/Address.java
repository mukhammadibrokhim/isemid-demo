package uz.uzinfocom.isemid.features.patient.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.commons.models.BaseEntity;
import uz.uzinfocom.isemid.features.form058.enums.AddressType;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "pt_address",
        indexes = {
                @Index(name = "idx_address_patient", columnList = "patient_id"),
                @Index(name = "idx_address_location", columnList = "stateCode, cityCode, districtCode")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class Address extends BaseEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Enumerated(EnumType.STRING)
    private AddressType type;

    private String stateCode;
    private String cityCode;
    private String neighborhoodCode;
    private String streetAddress;
    private String houseNumber;
    private String apartmentNumber;
}
