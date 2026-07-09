package uz.uzinfocom.app.modules.card.domain.model.card161;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.app.platform.persistence.entity.UuidAuditableEntity;

@Getter
@Setter
@Entity
@Table(
        name = "card161_contact_person",
        indexes = @Index(name = "idx_card161_contact_person_card161_id", columnList = "card161_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class ContactPerson extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card161_id", nullable = false, foreignKey = @ForeignKey(name = "fk_card161_contact_person_card161"))
    private Card161 card161;

    @Column(name = "full_name", length = 500)
    private String fullName;

    @Column(name = "age", length = 32)
    private String age;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "job_type_and_location", length = 500)
    private String jobTypeAndLocation;

    @Column(name = "immunization_status", length = 255)
    private String immunizationStatus;

    @Column(name = "restriction_measures", length = 500)
    private String restrictionMeasures;
}
