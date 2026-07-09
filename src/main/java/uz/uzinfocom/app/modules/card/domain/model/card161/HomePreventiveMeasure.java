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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "card161_prevent_measure",
        indexes = @Index(name = "idx_card161_prevent_measure_card161_id", columnList = "card161_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class HomePreventiveMeasure extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card161_id", nullable = false, foreignKey = @ForeignKey(name = "fk_card161_prevent_measure_card161"))
    private Card161 card161;

    @Column(name = "notified_person", length = 500)
    private String notifiedPerson;

    /** DMED integration. */
    @Column(name = "next_disease_time")
    private LocalDateTime nextDiseaseTime;

    /** DMED integration. */
    @Column(name = "drug_name", length = 255)
    private String drugName;

    /** DMED integration. */
    @Column(name = "dose", length = 100)
    private String dose;

    /** DMED integration. */
    @Column(name = "series", length = 100)
    private String series;

    /** LIS integration. */
    @Column(name = "result_time")
    private LocalDateTime resultTime;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    /** LIS integration. */
    @Column(name = "lis_result", length = 500)
    private String lisResult;

    @Column(name = "observation_result", length = 500)
    private String observationResult;
}
