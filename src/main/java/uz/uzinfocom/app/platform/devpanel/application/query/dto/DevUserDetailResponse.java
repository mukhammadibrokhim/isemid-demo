package uz.uzinfocom.app.platform.devpanel.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "A developer-monitoring-panel login, in detail. Password is never returned after creation.")
public record DevUserDetailResponse(
        Long id,
        String username,
        boolean enabled,

        @Schema(description = "Whether this account may itself manage other dev-panel accounts.")
        boolean root,

        Instant createdAt,
        Instant updatedAt
) {
}
