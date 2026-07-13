package uz.uzinfocom.isemid.features.form0581.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.UUID;

/**
 * Result of the final diagnosis approval/rejection - modeled as a single cohesive value object
 * (populated via the {@code approve-diagnosis} or {@code not-approve} action).
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class DiagnosisApproval {

    @Column(name = "final_mkb10code")
    private String finalMkb10Code;

    @Column(name = "final_mkb10name")
    private String finalMkb10Name;

    @Column(name = "approved_full_name")
    private String approvedFullName;

    @Column(name = "approved_org_uuid")
    private UUID approvedOrgUuid;

    @Column(name = "not_approve_comment", length = 1000)
    private String notApproveComment;
}
