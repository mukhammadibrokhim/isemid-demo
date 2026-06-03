package uz.uzinfocom.app.platform.iam.web.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.OrganizationShortResponse;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RoleShortResponse;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Detailed user information.")
public record UserDetailedResponse(
        @Schema(description = "User database identifier.", example = "1")
        Long id,
        @Schema(description = "User UUID.")
        UUID uuid,
        @Schema(description = "Username or login.")
        String username,
        @Schema(description = "Whether the user is active.", example = "true")
        Boolean active,
        @Schema(description = "First name.")
        String firstName,
        @Schema(description = "Last name.")
        String lastName,
        @Schema(description = "Middle name.")
        String middleName,
        @Schema(description = "NNUZB identifier.")
        String nnuzb,
        @Schema(description = "Birth date.", example = "1990-01-01")
        LocalDate birthDate,
        @Schema(description = "Phone number.")
        String phoneNumber,
        @Schema(description = "State code.")
        String stateCode,
        @Schema(description = "City or district code.")
        String cityCode,
        @Schema(description = "Address line.")
        String line,
        @Schema(description = "Gender code.")
        String genderCode,
        @Schema(description = "Organizations the user belongs to.")
        Set<OrganizationShortResponse> organizations,
        @Schema(description = "Global roles assigned to the user.")
        Set<RoleShortResponse> roles
) {
}
