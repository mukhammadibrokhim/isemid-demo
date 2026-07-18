package uz.uzinfocom.app.modules.form0581.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.modules.form0581.domain.enums.Form0581Status;
import uz.uzinfocom.app.modules.form0581.domain.exception.InvalidForm0581StateException;
import uz.uzinfocom.app.modules.form0581.domain.model.embedded.*;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.platform.persistence.entity.AbsEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * "Form 058-1" — emergency notification of suspected rabies on an animal
 * bite/scratch/saliva-contact case. Sibling of {@link uz.uzinfocom.app.modules.form058.domain.model.Form058},
 * built to the same create/update/approve/notApprove/cancel/delete lifecycle,
 * but deliberately not sharing {@code FormStatus} (no CARD_LINKED concept
 * here) or the {@code Form058} entity itself — these are independent
 * sibling forms, not a subtype relationship.
 */
@Getter
@Setter
@Entity
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "form058_1",
        indexes = {
                @Index(name = "idx_form0581_status", columnList = "status"),
                @Index(name = "idx_form0581_patient_id", columnList = "patient_id"),
                @Index(name = "idx_form0581_sender_org_id", columnList = "sender_organization_id"),
                @Index(name = "idx_form0581_receiver_org_id", columnList = "receiver_organization_id"),
                @Index(name = "idx_form0581_created_at", columnList = "created_at"),
                @Index(name = "idx_form0581_mkb10_code", columnList = "mkb10_code"),
                @Index(name = "idx_form0581_deleted", columnList = "deleted"),
                @Index(name = "idx_form0581_deleted_sender_created", columnList = "deleted,sender_organization_id,created_at"),
                @Index(name = "idx_form0581_deleted_receiver_created", columnList = "deleted,receiver_organization_id,created_at"),
                // partial (WHERE deleted = false) in reality — see Liquibase changelog
                @Index(name = "idx_form0581_receiver_mkb10", columnList = "receiver_organization_id,mkb10_code"),
                @Index(name = "idx_form0581_sender_mkb10", columnList = "sender_organization_id,mkb10_code"),
                @Index(name = "idx_form0581_mkb10_not_deleted", columnList = "mkb10_code"),
                @Index(name = "idx_form0581_receiver_source", columnList = "receiver_organization_id,source"),
                @Index(name = "idx_form0581_sender_source", columnList = "sender_organization_id,source")
        }
)
public class Form0581 extends AbsEntity {

    @Embedded
    private Form0581DiagnosisInfo diagnosisInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private Form0581Status status;

    @Column(name = "source", nullable = false, length = 20)
    private String source;

    /**
     * Form0581 belongs to a patient (the bite victim).
     * Do not use CascadeType.ALL here because Form0581 must not control
     * Patient lifecycle — same rule as Form058.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "patient_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_form0581_patient")
    )
    private Patient patient;

    @Column(name = "sender_organization_id", nullable = false)
    private Long senderOrganizationId;

    /**
     * The receiving sanitary-epidemiological service (SES) organization.
     * Constrained to {@code Organization.medicalType == SANEPID_SERVICE} at
     * the validator level, not by a DB constraint.
     */
    @Column(name = "receiver_organization_id", nullable = false)
    private Long receiverOrganizationId;

    @Embedded
    private Form0581IncidentInfo incidentInfo;

    @Embedded
    private Form0581AnimalInfo animalInfo;

    @Embedded
    private Form0581AnimalOwnerInfo animalOwnerInfo;

    /**
     * Gates {@link #otherInjuredPeople} — whether anyone besides the primary
     * victim (identified via {@link #patient}) was also injured in the same
     * incident.
     */
    @Column(name = "other_people_injured")
    private Boolean otherPeopleInjured;

    @OneToMany(mappedBy = "form0581", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<Form0581OtherInjuredPerson> otherInjuredPeople = new ArrayList<>();

    @Embedded
    private Form0581HospitalizationInfo hospitalizationInfo;

    @Embedded
    private Form0581ReportInfo reportInfo;

    @Embedded
    private Form0581CancellationInfo cancellationInfo;

    @Embedded
    private Form0581ApprovalInfo approvalInfo;

    /**
     * Soft delete state. Columns remain in the form058_1 table: deleted,
     * deleted_at, deleted_by_id, delete_reason.
     */
    @Embedded
    @Builder.Default
    private Form0581DeleteInfo deleteInfo = new Form0581DeleteInfo();

    public void ensureEditable() {
        if (isCanceled() || isApproved() || isDeleted()) {
            throw new InvalidForm0581StateException(
                    "error.form0581.update-not-allowed",
                    this.status
            );
        }
    }

    public void cancel(String reason, Long canceledBy) {
        if (isCanceled()) {
            return;
        }

        ensureCancellationInfo();

        this.status = Form0581Status.CANCELED;
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

        this.status = Form0581Status.APPROVED;
        this.diagnosisInfo.setFinalMkb10Code(finalMkb10Code);
        this.diagnosisInfo.setFinalMkb10Name(finalMkb10Name);
        this.approvalInfo.setApprovedBy(approvedBy);
        this.approvalInfo.setApprovedOrganizationId(approvedOrganizationId);
        this.approvalInfo.setApprovedAt(Instant.now());
    }

    public void notApprove(String reason) {
        ensureCancellationInfo();

        this.status = Form0581Status.NOT_APPROVED;
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
        return Form0581Status.APPROVED.equals(this.status);
    }

    public boolean isCanceled() {
        return Form0581Status.CANCELED.equals(this.status);
    }

    public boolean isDeleted() {
        return this.deleteInfo != null && this.deleteInfo.isDeleted();
    }

    private void ensureDiagnosisInfo() {
        if (this.diagnosisInfo == null) {
            this.diagnosisInfo = new Form0581DiagnosisInfo();
        }
    }

    private void ensureCancellationInfo() {
        if (this.cancellationInfo == null) {
            this.cancellationInfo = new Form0581CancellationInfo();
        }
    }

    private void ensureApprovalInfo() {
        if (this.approvalInfo == null) {
            this.approvalInfo = new Form0581ApprovalInfo();
        }
    }

    private void ensureDeleteInfo() {
        if (this.deleteInfo == null) {
            this.deleteInfo = new Form0581DeleteInfo();
        }
    }
}
