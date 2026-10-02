package uz.uzinfocom.app.platform.devpanel.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "A dev-panel position/department lookup entry.")
public record DevPositionResponse(
        Long id,
        String name,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
}
