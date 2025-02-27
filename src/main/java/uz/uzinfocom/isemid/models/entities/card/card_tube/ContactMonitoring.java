package uz.uzinfocom.isemid.models.entities.card.card_tube;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.template.BaseEntity;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "card_tube_contact_monitoring")
public class ContactMonitoring extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "card_tube_id", nullable = false)
    @JsonIgnore
    private CardTube cardTube;

    private String fullName;
    private LocalDate birthDate;
    private Integer age;

    @ManyToOne
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog relation;
    private String workplaceOrStudyPlace;
    private String notificationReceiver;
    private LocalDate diagnosisDate;

    @ManyToOne
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog contactStatus;
}
