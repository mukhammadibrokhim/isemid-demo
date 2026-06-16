package uz.uzinfocom.app.platform.iam.application.user.query.dto;

import java.util.UUID;

public record UserMiniResponse(
        Long id,
        UUID uuid,
        String username,
        String firstName,
        String lastName,
        String middleName,
        String fullName
) {
}
