package uz.uzinfocom.isemid.features.card.models.card161;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity(name = "card161_contact_person")
public class ContactPerson extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card161_id", nullable = false)
    @JsonIgnore
    private Card161 card161;

    private String fullName;
    private String age;
    private String address;
    private String jobTypeAndLocation;
    private String immunizationStatus;
    private String restrictionMeasures;
}
