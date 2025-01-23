package uz.uzinfocom.isemid.models.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.models.entities.enums.AddressType;
import uz.uzinfocom.isemid.models.entities.template.BaseEntity;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Address extends BaseEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;


    @Enumerated(EnumType.STRING)
    private AddressType type;
    @NotBlank(message = "State code is required and not blank.")
    private String stateCode;
    @NotBlank(message = "Citi code is required and not blank.")
    private String cityCode;
    @NotBlank(message = "Neighborhood  is required and not blank.")
    private String neighborhood;
    @NotBlank(message = "Street address is required and not blank.")
    private String streetAddress;
    @NotBlank(message = "House number is required and not blank.")
    private String houseNumber;
    @NotBlank(message = "Apartment number is required and not blank.")
    private String apartmentNumber;
}
