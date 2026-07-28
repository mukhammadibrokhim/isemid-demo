package uz.uzinfocom.app.platform.devmonitoring.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "A newly-provisioned dev-panel account. password is shown only once and cannot be retrieved again.")
public record DevUserCreateResponse(
        Long id,
        String username,

        @Schema(description = "Plaintext password - shown once, never stored or retrievable again.")
        String password,

        Instant createdAt
) {
}
