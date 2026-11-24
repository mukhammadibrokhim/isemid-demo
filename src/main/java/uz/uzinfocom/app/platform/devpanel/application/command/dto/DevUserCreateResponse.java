package uz.uzinfocom.app.platform.devpanel.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.devpanel.domain.DevUserRole;

import java.time.Instant;

@Schema(description = "A newly-provisioned dev-panel account. password is shown only once and cannot be retrieved again.")
public record DevUserCreateResponse(
        Long id,
        String username,
        DevUserRole role,
        String email,
        String fullName,
        String phone,
        Long positionId,
        String positionName,

        @Schema(description = "Plaintext password - shown once, never stored or retrievable again.")
        String password,

        Instant createdAt
) {
}
