package uz.uzinfocom.app.modules.form058.domain.model.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Form058ApprovalInfo {

    @Column(name = "approved_by_id")
    private Long approvedBy;

    @Column(name = "approved_organization_id")
    private Long approvedOrganizationId;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "approved_full_name")
    private String approvedFullName;

    @Column(name = "approved_org_uuid")
    private UUID approvedOrgUuid;
}