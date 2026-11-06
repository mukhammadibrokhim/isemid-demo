package uz.uzinfocom.app.platform.devpanel.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update a dev-panel account's profile detail. Username, password, role "
        + "and enabled/revoked state are not editable here - see the dedicated create/revoke endpoints.")
public record DevUserUpdateRequest(
        @Schema(description = "Contact email for this account.", example = "dev-oncall@example.com")
        @NotBlank(message = "{dev-user.email.required}")
        @Size(max = 150, message = "{dev-user.email.size}")
        @Email(message = "{dev-user.email.invalid}")
        String email,

        @Schema(description = "Full name, for display in the account list. Optional.")
        @Size(max = 200, message = "{dev-user.full-name.size}")
        String fullName,

        @Schema(description = "Contact phone number, for display in the account list. Optional.")
        @Size(max = 32, message = "{dev-user.phone.size}")
        String phone,

        @Schema(description = "Id of a position/department from GET /v1/dev/ref/positions. Null clears it.")
        @Positive(message = "{dev-user.position-id.positive}")
        Long positionId
) {
}
