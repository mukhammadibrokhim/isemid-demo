package uz.uzinfocom.app.modules.form129.domain.model;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.modules.form129.domain.enums.Form129Status;
import uz.uzinfocom.app.modules.form129.domain.exception.InvalidForm129StateException;
import uz.uzinfocom.app.modules.form129.domain.model.embedded.Form129CancellationInfo;
import uz.uzinfocom.app.modules.form129.domain.model.embedded.Form129LabResults;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.platform.audit.domain.AuditableFields;
import uz.uzinfocom.app.platform.persistence.entity.AbsEntity;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * "Form 129" — a lab-to-sanitary-epidemiological-committee notification of
 * infectious disease serology results (syphilis, hepatitis B, brucellosis)
 * submitted by AKP/hospital/blood-transfusion-center laboratories. Sibling
 * of {@link uz.uzinfocom.app.modules.form0581.domain.model.Form0581} (same
 * sender/receiver-SES shape), but a deliberately smaller lifecycle: create →
 * receiver accept/reject, with no card-linking, approval, update or delete —
 * a pure registry, never carrying attachments.
 */
@Getter
@Setter
@Entity
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "form_129",
        indexes = {
                @Index(name = "idx_form129_status", columnList = "status"),
                @Index(name = "idx_form129_patient_id", columnList = "patient_id"),
                @Index(name = "idx_form129_sender_org_id", columnList = "sender_organization_id"),
                @Index(name = "idx_form129_receiver_org_id", columnList = "receiver_organization_id"),
                @Index(name = "idx_form129_created_at", columnList = "created_at"),
                @Index(name = "idx_form129_sender_created", columnList = "sender_organization_id,created_at"),
                @Index(name = "idx_form129_receiver_created", columnList = "receiver_organization_id,created_at")
        }
)
public class Form129 extends AbsEntity implements AuditableFields {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private Form129Status status;

    @Column(name = "source", nullable = false, length = 20)
    private String source;

    @Column(name = "reporting_institution_name", length = 500)
    private String reportingInstitutionName;

    @Column(name = "medical_id", length = 128)
    private String medicalId;

    /**
     * Form129 belongs to a patient (the person tested). Do not use
     * CascadeType.ALL here — Form129 must not control Patient lifecycle,
     * same rule as Form058/Form0581.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "patient_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_form129_patient")
    )
    private Patient patient;

    @Column(name = "sender_organization_id", nullable = false)
    private Long senderOrganizationId;

    /**
     * The receiving sanitary-epidemiological service (SES) organization.
     * Constrained to {@code Organization.medicalType == SANEPID_SERVICE} at
     * the validator level, not by a DB constraint — see
     * {@code Form129CreateValidator}.
     */
    @Column(name = "receiver_organization_id", nullable = false)
    private Long receiverOrganizationId;

    /**
     * The submitting {@code IntegrationClient}'s numeric id, when this form
     * was created through the inbound-integration API by a registered
     * client — null for SSO/DHP-submitted forms. Drives the outbound
     * status-change webhook: only the client that submitted a form is ever
     * notified back about it.
     */
    @Column(name = "source_integration_client_id")
    private Long sourceIntegrationClientId;

    @Embedded
    private Form129LabResults labResults;

    /**
     * "Ф.И.О. заявителя" — auto-filled from the submitting user at create
     * time, never entered by hand.
     */
    @Column(name = "notifier_full_name", length = 255)
    private String notifierFullName;

    /**
     * "Кто получил сообщение Ф.И.О." — auto-filled from the accepting
     * receiver-organization user at {@link #accept()} time.
     */
    @Column(name = "receiver_full_name", length = 255)
    private String receiverFullName;

    @Embedded
    @Builder.Default
    private Form129CancellationInfo cancellationInfo = new Form129CancellationInfo();

    public void accept(String receiverFullName) {
        ensureDecisionPending();
        this.status = Form129Status.ACCEPTED;
        this.receiverFullName = receiverFullName;
    }

    /**
     * The receiver's rejection of an incoming ({@code SENT}) form — the only
     * way to CANCELED. Unlike Form0581, the sender may not withdraw a
     * Form129: only the receiving SES organization decides accept/reject.
     */
    public void reject(String reason, Long rejectedBy) {
        ensureDecisionPending();
        ensureCancellationInfo();

        this.status = Form129Status.CANCELED;
        this.cancellationInfo.setCancelReason(reason);
        this.cancellationInfo.setCanceledBy(rejectedBy);
        this.cancellationInfo.setCanceledAt(Instant.now());
    }

    private void ensureDecisionPending() {
        if (!status.isDecisionPending()) {
            throw new InvalidForm129StateException("error.form129.decision-not-allowed", this.status);
        }
    }

    private void ensureCancellationInfo() {
        if (this.cancellationInfo == null) {
            this.cancellationInfo = new Form129CancellationInfo();
        }
    }

    /**
     * Flattened, scalar-only snapshot for {@code AuditFieldDiff} — never a
     * reference to {@link #labResults} or {@link #patient} themselves
     * (mutable), only their leaf values.
     */
    @Override
    public Map<String, Object> auditFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("status", status);
        fields.put("source", source);
        fields.put("senderOrganizationId", senderOrganizationId);
        fields.put("receiverOrganizationId", receiverOrganizationId);
        fields.put("reportingInstitutionName", reportingInstitutionName);
        fields.put("medicalId", medicalId);
        if (patient != null) {
            fields.put("patientId", patient.getId());
            fields.put("patientFirstName", patient.getFirstName());
            fields.put("patientLastName", patient.getLastName());
            fields.put("patientMiddleName", patient.getMiddleName());
        }
        return fields;
    }
}
