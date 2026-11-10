package uz.uzinfocom.app.platform.devpanel.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of an admin-initiated password reset. password is shown only once and "
        + "cannot be retrieved again; the account must change it on next login.")
public record DevUserResetPasswordResponse(
        Long id,
        String username,

        @Schema(description = "Plaintext password - shown once, never stored or retrievable again.")
        String password
) {
}
