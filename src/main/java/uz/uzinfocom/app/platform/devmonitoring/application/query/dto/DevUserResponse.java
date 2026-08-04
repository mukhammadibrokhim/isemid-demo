package uz.uzinfocom.app.platform.devmonitoring.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "A developer-monitoring-panel login. Password is never returned after creation.")
public record DevUserResponse(
        Long id,
        String username,
        boolean enabled,

        @Schema(description = "Whether this account may itself manage other dev-panel accounts.")
        boolean root,

        Instant createdAt
) {
}
