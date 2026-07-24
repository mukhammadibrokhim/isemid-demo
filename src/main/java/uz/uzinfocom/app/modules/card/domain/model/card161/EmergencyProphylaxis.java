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

import java.time.LocalDateTime;

/**
 * A single row of the "Tezkor profilaktika yoki antirabik yordam haqida
 * ma'lumot" table on Card161's zoonotic-disease attachment sheet (Ilova
 * varag'i №178) — distinct from {@link Vaccination}, which is the routine
 * vaccination-history table on the same sheet.
 */
@Getter
@Setter
@Entity
@Table(
        name = "card161_emergency_prophylaxis",
        indexes = @Index(name = "idx_card161_emergency_prophylaxis_card161_id", columnList = "card161_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyProphylaxis extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card161_id", nullable = false, foreignKey = @ForeignKey(name = "fk_card161_emergency_prophylaxis_card161"))
    private Card161 card161;

    @Column(name = "treatment_date")
    private LocalDateTime treatmentDate;

    @Column(name = "drug_name", length = 255)
    private String drugName;

    @Column(name = "dose", length = 100)
    private String dose;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "administration_schedule", length = 255)
    private String administrationSchedule;
}
