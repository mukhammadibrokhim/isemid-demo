package uz.uzinfocom.app.integration.inbound.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.platform.persistence.entity.BaseEntity;

import java.time.Instant;

/**
 * Who submitted a form through the inbound integration API: the calling
 * system (token-proven), the organization it submitted for, and the individual
 * the system named in the body (as stated by the system, not verified).
 */
@Getter
@Setter
@Entity
@Table(name = "inbound_submission_audit")
@SuperBuilder
@NoArgsConstructor
public class InboundSubmissionAudit extends BaseEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "form_type", nullable = false, length = 32)
    private InboundFormType formType;

    @Column(name = "form_id", nullable = false)
    private Long formId;

    @Column(name = "integration_client_id")
    private Long integrationClientId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "submitter_pinfl", length = 32)
    private String submitterPinfl;

    @Column(name = "submitter_full_name", length = 255)
    private String submitterFullName;

    @Column(name = "submitter_position", length = 255)
    private String submitterPosition;

    @Column(name = "submitter_phone", length = 64)
    private String submitterPhone;

    @Column(name = "remote_ip", length = 64)
    private String remoteIp;
}
