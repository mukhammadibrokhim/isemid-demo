package uz.uzinfocom.app.modules.form058.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;
import uz.uzinfocom.app.modules.form058.domain.exception.InvalidForm058StateException;
import uz.uzinfocom.app.modules.form058.domain.model.embedded.*;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.platform.persistence.entity.AbsEntity;

import java.time.Instant;

@Getter
@Setter
@Entity
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
/*
 * ddl-auto=validate — the DB schema is owned by Liquibase
 * (db.migration/changelog/form058/), not by these annotations. The
 * @Index list below is a best-effort documentation mirror: Hibernate's
 * columnList syntax can't express the partial (WHERE deleted = false)
 * or DESC-ordered indexes that actually exist on the table, so several
 * entries below are listed without those qualifiers. The Liquibase
 * changelog is the source of truth for the real index definitions.
 */
@Table(
        name = "form058",
        indexes = {
                @Index(name = "idx_form058_status", columnList = "status"),
                @Index(name = "idx_form058_patient_id", columnList = "patient_id"),
                @Index(name = "idx_form058_sender_org_id", columnList = "sender_organization_id"),
                @Index(name = "idx_form058_receiver_org_id", columnList = "receiver_organization_id"),
                @Index(name = "idx_form058_created_at", columnList = "created_at"),
                @Index(name = "idx_form058_mkb10_code", columnList = "mkb10_code"),
                @Index(name = "idx_form058_final_mkb10_code", columnList = "final_mkb10_code"),
                @Index(name = "idx_form058_deleted", columnList = "deleted"),
                @Index(name = "idx_form058_deleted_sender_created", columnList = "deleted,sender_organization_id,created_at"),
                @Index(name = "idx_form058_deleted_receiver_created", columnList = "deleted,receiver_organization_id,created_at"),
                // partial (WHERE deleted = false) in reality — see Liquibase changelog
                @Index(name = "idx_form058_outgoing_created_fast", columnList = "sender_organization_id,created_at,id"),
                @Index(name = "idx_form058_incoming_created_fast", columnList = "receiver_organization_id,created_at,id"),
                @Index(name = "idx_form058_outgoing_table", columnList = "sender_organization_id,status,created_at,id"),
                @Index(name = "idx_form058_incoming_table", columnList = "receiver_organization_id,status,created_at,id"),
                @Index(name = "idx_form058_active_patient_id_id", columnList = "patient_id,id"),
                @Index(name = "idx_form058_source_created_fast", columnList = "source,created_at,id"),
                @Index(name = "idx_form058_has_linked_cards_created_fast", columnList = "has_linked_cards,created_at,id"),
                // partial (WHERE deleted = false) in reality — see Liquibase changelog
                @Index(name = "idx_form058_receiver_mkb10", columnList = "receiver_organization_id,mkb10_code"),
                @Index(name = "idx_form058_sender_mkb10", columnList = "sender_organization_id,mkb10_code"),
                @Index(name = "idx_form058_mkb10_not_deleted", columnList = "mkb10_code"),
                @Index(name = "idx_form058_receiver_source", columnList = "receiver_organization_id,source"),
                @Index(name = "idx_form058_sender_source", columnList = "sender_organization_id,source")
        }
)
public class Form058 extends AbsEntity {

    @Embedded
    private Form058DiagnosisInfo diagnosisInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private FormStatus status;

    @Column(name = "source", nullable = false, length = 20)
    private String source;

    /**
     * Form058 belongs to a patient.
     * Do not use CascadeType.ALL here because Form058 must not control Patient lifecycle.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "patient_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_form058_patient")
    )
    private Patient patient;

    @Embedded
    private Form058ClinicalInfo clinicalInfo;

    @Embedded
    private Form058DateInfo dateInfo;

    @Column(name = "sender_organization_id", nullable = false)
    private Long senderOrganizationId;

    @Column(name = "receiver_organization_id", nullable = false)
    private Long receiverOrganizationId;

    /**
     * Location is owned by Form058.
     * Persist and merge are enough. Remove is intentionally not cascaded.
     */
    @OneToOne(
            fetch = FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            },
            optional = true
    )
    @JoinColumn(
            name = "location_id",
            foreignKey = @ForeignKey(name = "fk_form058_location")
    )
    private Form058Location location;

    @Embedded
    private Form058EpidemicInfo epidemicInfo;

    @Embedded
    private Form058ReportInfo reportInfo;

    /**
     * Denormalized flag for fast table filtering.
     */
    @Column(name = "has_linked_cards", nullable = false)
    @Builder.Default
    private boolean hasLinkedCards = false;

    /**
     * Legacy single-card link kept for existing API and database compatibility.
     */
    @Deprecated
    @Column(name = "assigned_card_id")
    private Long assignedCardId;

    @Embedded
    private Form058CancellationInfo cancellationInfo;

    @Embedded
    private Form058ApprovalInfo approvalInfo;

    /**
     * Soft delete state.
     * Columns remain in form058 table:
     * deleted, deleted_at, deleted_by_id, delete_reason.
     */
    @Embedded
    @Builder.Default
    private Form058DeleteInfo deleteInfo = new Form058DeleteInfo();

    public void attachLocation(Form058Location location) {
        this.location = location;
    }

    public void refreshCardLinkState() {
        this.hasLinkedCards = this.assignedCardId != null;
    }

    public void markCardsLinked() {
        this.hasLinkedCards = true;
    }

    public void markCardsUnlinked() {
        this.hasLinkedCards = false;
    }

    /**
     * Called once one or more cards exist on this form. Advances the
     * workflow into {@link FormStatus#CARD_LINKED} — but only forward: a
     * form already past that point (APPROVED_PENDING and beyond) must not
     * be pushed backwards just because another card was added to it.
     * {@link #ensureEditable()} already rules out CANCELED/APPROVED/deleted
     * forms before this is ever reached.
     */
    public void linkCards() {
        ensureEditable();
        markCardsLinked();

        if (status == FormStatus.NOT_APPROVED || status == FormStatus.SENT || status == FormStatus.RECEIVED) {
            status = FormStatus.CARD_LINKED;
        }
    }

    public void ensureEditable() {
        if (isCanceled() || isApproved() || isDeleted()) {
            throw new InvalidForm058StateException(
                    "error.form058.update-not-allowed",
                    this.status
            );
        }
    }

    public void cancel(String reason, Long canceledBy) {
        if (isCanceled()) {
            return;
        }

        ensureCancellationInfo();

        this.status = FormStatus.CANCELED;
        this.cancellationInfo.setCancelReason(reason);
        this.cancellationInfo.setCanceledBy(canceledBy);
        this.cancellationInfo.setCanceledAt(Instant.now());
    }

    public void approve(
            String finalMkb10Code,
            String finalMkb10Name,
            Long approvedBy,
            Long approvedOrganizationId
    ) {
        ensureDiagnosisInfo();
        ensureApprovalInfo();

        this.status = FormStatus.APPROVED;
        this.diagnosisInfo.setFinalMkb10Code(finalMkb10Code);
        this.diagnosisInfo.setFinalMkb10Name(finalMkb10Name);
        this.approvalInfo.setApprovedBy(approvedBy);
        this.approvalInfo.setApprovedOrganizationId(approvedOrganizationId);
        this.approvalInfo.setApprovedAt(Instant.now());
    }

    public void notApprove(String reason) {
        ensureCancellationInfo();

        this.status = FormStatus.NOT_APPROVED;
        this.cancellationInfo.setNotApprovedReason(reason);
    }

    public void updateFinalDiagnosis(String finalMkb10Code, String finalMkb10Name) {
        ensureDiagnosisInfo();
        this.diagnosisInfo.setFinalMkb10Code(finalMkb10Code);
        this.diagnosisInfo.setFinalMkb10Name(finalMkb10Name);
    }

    public void softDelete(Long deletedBy, String reason) {
        ensureDeleteInfo();
        this.deleteInfo.softDelete(deletedBy, reason);
    }

    public void restore() {
        ensureDeleteInfo();
        this.deleteInfo.restore();
    }

    public boolean isApproved() {
        return FormStatus.APPROVED.equals(this.status);
    }

    public boolean isCanceled() {
        return FormStatus.CANCELED.equals(this.status);
    }

    public boolean isDeleted() {
        return this.deleteInfo != null && this.deleteInfo.isDeleted();
    }

    private void ensureDiagnosisInfo() {
        if (this.diagnosisInfo == null) {
            this.diagnosisInfo = new Form058DiagnosisInfo();
        }
    }

    private void ensureCancellationInfo() {
        if (this.cancellationInfo == null) {
            this.cancellationInfo = new Form058CancellationInfo();
        }
    }

    private void ensureApprovalInfo() {
        if (this.approvalInfo == null) {
            this.approvalInfo = new Form058ApprovalInfo();
        }
    }

    private void ensureDeleteInfo() {
        if (this.deleteInfo == null) {
            this.deleteInfo = new Form058DeleteInfo();
        }
    }
}