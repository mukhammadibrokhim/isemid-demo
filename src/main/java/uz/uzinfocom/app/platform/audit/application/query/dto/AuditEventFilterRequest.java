package uz.uzinfocom.app.platform.audit.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.audit.domain.AuditEventType;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

import java.time.Instant;

@Schema(description = "Filter and pagination parameters for the audit event log.")
public record AuditEventFilterRequest(
        @Min(value = 1, message = "{pagination.page.min}")
        Integer page,

        @Min(value = 1, message = "{pagination.size.min}")
        @Max(value = 200, message = "{pagination.size.max}")
        Integer size,

        @Schema(allowableValues = {"id", "occurredAt"})
        String sortBy,

        @Schema(allowableValues = {"asc", "desc"})
        String sortDir,

        @Schema(description = "Filter by audited aggregate type.")
        AuditEntityType entityType,

        @Schema(description = "Filter by the audited aggregate's id (Form058/Form0581/Act id).")
        Long entityId,

        @Schema(description = "Filter by event kind.")
        AuditEventType eventType,

        @Schema(description = "Filter by the organization the entity was assigned to after the event.")
        Long organizationId,

        @Schema(description = "Only include events at or after this instant.")
        Instant from,

        @Schema(description = "Only include events at or before this instant.")
        Instant to
) implements PageableRequest {
}
