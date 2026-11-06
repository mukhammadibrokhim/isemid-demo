package uz.uzinfocom.app.platform.devpanel.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.devpanel.domain.DevUserRole;

import java.time.Instant;

@Schema(description = "A developer-monitoring-panel login, in detail. Password is never returned after creation.")
public record DevUserDetailResponse(
        Long id,
        String username,
        boolean enabled,

        @Schema(description = "Access tier: SUPER_ADMIN (full CRUD, manages other dev-panel accounts), "
                + "ADMIN (CRUD except delete) or USER (read-only).")
        DevUserRole role,

        @Schema(description = "When true, every /v1/dev/** request from this account is blocked "
                + "(403 dev-user.password.change-required) except GET .../me and "
                + "PATCH .../me/password - see DevPasswordChangeGuardFilter.")
        boolean mustChangePassword,

        String email,
        String fullName,
        String phone,

        @Schema(description = "Id of the assigned position/department (GET /v1/dev/ref/positions), if any.")
        Long positionId,

        @Schema(description = "Name of the assigned position/department, if any.")
        String positionName,

        Instant createdAt,
        Instant updatedAt
) {
}
