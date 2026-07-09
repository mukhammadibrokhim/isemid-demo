package uz.uzinfocom.app.modules.card.domain.model.card_tube;

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
import uz.uzinfocom.app.modules.card.domain.annotation.CatalogCode;
import uz.uzinfocom.app.platform.persistence.entity.UuidAuditableEntity;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(
        name = "card_tube_contact_monitoring",
        indexes = @Index(name = "idx_card_tube_contact_monitoring_card_tube_id", columnList = "card_tube_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class ContactMonitoring extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_tube_id", nullable = false, foreignKey = @ForeignKey(name = "fk_card_tube_contact_monitoring_card_tube"))
    private CardTube cardTube;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "age")
    private Integer age;

    @CatalogCode("card-tube-relation")
    @Column(name = "relation_code", length = 64)
    private String relationCode;

    @Column(name = "workplace_or_study_place", length = 255)
    private String workplaceOrStudyPlace;

    @Column(name = "notification_receiver", length = 255)
    private String notificationReceiver;

    @Column(name = "diagnosis_date")
    private LocalDate diagnosisDate;

    @CatalogCode("card-tube-contact-status")
    @Column(name = "contact_status_code", length = 64)
    private String contactStatusCode;
}
