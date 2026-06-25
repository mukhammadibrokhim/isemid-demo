package uz.uzinfocom.app.modules.form058.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;
import uz.uzinfocom.app.modules.form058.domain.exception.InvalidForm058StateException;
import uz.uzinfocom.app.modules.form058.domain.model.embedded.Form058ApprovalInfo;
import uz.uzinfocom.app.modules.form058.domain.model.embedded.Form058CancellationInfo;
import uz.uzinfocom.app.modules.form058.domain.model.embedded.Form058ClinicalInfo;
import uz.uzinfocom.app.modules.form058.domain.model.embedded.Form058DateInfo;
import uz.uzinfocom.app.modules.form058.domain.model.embedded.Form058DiagnosisInfo;
import uz.uzinfocom.app.modules.form058.domain.model.embedded.Form058EpidemicInfo;
import uz.uzinfocom.app.modules.form058.domain.model.embedded.Form058ReportInfo;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.platform.persistence.entity.AbsEntity;

import java.time.Instant;

@Getter
@Setter
@Entity
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
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
                @Index(name = "idx_form058_deleted", columnList = "deleted")
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
     * SQL delete annotation is not used. Delete is handled explicitly from the service layer.
     */
    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by_id")
    private Long deletedBy;

    @Column(name = "delete_reason", length = 1000)
    private String deleteReason;

    public void attachLocation(Form058Location location) {
        this.location = location;
    }

    public void refreshCardLinkState() {
        this.hasLinkedCards = this.assignedCardId != null;
    }

    public void ensureEditable() {
        if (isCanceled() || isApproved() || isDeleted()) {
            throw new InvalidForm058StateException("error.form058.update-not-allowed", this.status);
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

    public void approve(String finalMkb10Code, String finalMkb10Name, Long approvedBy, Long approvedOrganizationId) {
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
        if (this.deleted) {
            return;
        }

        this.deleted = true;
        this.deletedAt = Instant.now();
        this.deletedBy = deletedBy;
        this.deleteReason = reason;
    }

    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
        this.deleteReason = null;
    }

    public boolean isApproved() {
        return FormStatus.APPROVED.equals(this.status);
    }

    public boolean isCanceled() {
        return FormStatus.CANCELED.equals(this.status);
    }

    public boolean isDeleted() {
        return this.deleted;
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
}
