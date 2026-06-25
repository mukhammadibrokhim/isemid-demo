package uz.uzinfocom.app.modules.form058.application.query.dto.detail;

import java.time.Instant;
import java.util.UUID;

public record Form058ApprovalDetailResponse(
        Long approvedBy,
        Long approvedOrganizationId,
        Instant approvedAt,
        String approvedFullName,
        UUID approvedOrgUuid
) {
}