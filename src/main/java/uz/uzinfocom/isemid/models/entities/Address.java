package uz.uzinfocom.isemid.models.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.models.entities.enums.AddressType;
import uz.uzinfocom.isemid.models.entities.template.BaseEntity;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;

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

    @ManyToOne
    @JoinColumn(name = "state", referencedColumnName = "code")
    @JsonIgnoreProperties({"type","id", "createdAt", "updatedAt","createdBy","updatedBy"})
    private Catalog state;

    @ManyToOne
    @JoinColumn(name = "city", referencedColumnName = "code")
    @JsonIgnoreProperties({"type","id", "createdAt", "updatedAt","createdBy","updatedBy"})
    private Catalog city;

    private String neighborhood;
    private String streetAddress;
    private String houseNumber;
    private String apartmentNumber;
}
