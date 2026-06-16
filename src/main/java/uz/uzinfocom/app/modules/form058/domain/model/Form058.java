package uz.uzinfocom.app.modules.form058.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.features.form058.domain.exception.InvalidForm058StateException;
import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;
import uz.uzinfocom.app.platform.persistence.entity.AbsEntity;
import uz.uzinfocom.app.platform.persistence.entity.UuidAuditableEntity;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "form058",
        indexes = {
                @Index(name = "idx_form058_sender_status", columnList = "sender_organization_id,status"),
                @Index(name = "idx_form058_receiver_status", columnList = "receiver_organization_id,status"),
                @Index(name = "idx_form058_patient_nnuzb", columnList = "patient_nnuzb"),
                @Index(name = "idx_form058_assigned_card_id", columnList = "assigned_card_id")
        }
)
public class Form058 extends AbsEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private FormStatus status;

    @Column(name = "source", nullable = false, length = 64)
    private String source;

    @Column(name = "sender_organization_id", nullable = false)
    private Long senderOrganizationId;

    @Column(name = "receiver_organization_id", nullable = false)
    private Long receiverOrganizationId;

    @Column(name = "hospital_place_id")
    private Long hospitalPlaceId;

    @Column(name = "mkb10_code", nullable = false, length = 20)
    private String mkb10Code;

    @Column(name = "mkb10_name", nullable = false, length = 512)
    private String mkb10Name;

    @Column(name = "final_mkb10_code", length = 20)
    private String finalMkb10Code;

    @Column(name = "final_mkb10_name", length = 512)
    private String finalMkb10Name;

    @Column(name = "disease_date", nullable = false)
    private LocalDate diseaseDate;

    @Column(name = "first_visit_date", nullable = false)
    private LocalDate firstVisitDate;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "initial_report_date_time", nullable = false)
    private Instant initialReportDateTime;

    @Column(name = "disease_place", nullable = false, length = 512)
    private String diseasePlace;

    @Column(name = "notifier_full_name", nullable = false, length = 255)
    private String notifierFullName;

    @Column(name = "journal_form_code", nullable = false, length = 64)
    private String journalFormCode;

    @Column(name = "form_comment", length = 2000)
    private String comment;

    @OneToOne(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(name = "has_linked_cards", nullable = false)
    private boolean hasLinkedCards;

    @Column(name = "assigned_card_id")
    private Long assignedCardId;

    @Column(name = "cancel_reason", length = 1000)
    private String cancelReason;

    @Column(name = "canceled_by_id")
    private Long canceledBy;

    @Column(name = "canceled_at")
    private Instant canceledAt;

    @Column(name = "approved_by_id")
    private Long approvedBy;

    @Column(name = "approved_organization_id")
    private Long approvedOrganizationId;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "not_approved_reason", length = 1000)
    private String notApprovedReason;

    @PrePersist
    protected void prePersistForm058() {
        if (status == null) {
            status = FormStatus.SENT;
        }
        if (source == null || source.isBlank()) {
            source = "ISEMID";
        }
        if (location == null) {
            location = new Location();
        }
    }

    public void ensureEditable() {
        if (status == null || !status.editable()) {
            throw new InvalidForm058StateException("error.form058.update-not-allowed", status);
        }
    }

    public void ensureCancellable() {
        if (status == null || !status.cancellable()) {
            throw new InvalidForm058StateException("error.form058.invalid-status", status);
        }
    }

    public void ensureApprovable() {
        if (status == null || !status.approvable()) {
            throw new InvalidForm058StateException("error.form058.approval-not-allowed", status);
        }
    }

    public void cancel(String reason, Long userId) {
        ensureCancellable();
        status = FormStatus.CANCELED;
        cancelReason = reason;
        canceledBy = userId;
        canceledAt = Instant.now();
    }

    public void approve(String diagnosisCode, String diagnosisName, Long userId, Long organizationId) {
        ensureApprovable();
        status = FormStatus.APPROVED;
        finalMkb10Code = diagnosisCode;
        finalMkb10Name = diagnosisName;
        approvedBy = userId;
        approvedOrganizationId = organizationId;
        approvedAt = Instant.now();
    }

    public void notApprove(String reason) {
        ensureApprovable();
        status = FormStatus.NOT_APPROVED;
        notApprovedReason = reason;
    }

    public void assignCard(Long cardId) {
        assignedCardId = cardId;
        hasLinkedCards = cardId != null;
    }
}
