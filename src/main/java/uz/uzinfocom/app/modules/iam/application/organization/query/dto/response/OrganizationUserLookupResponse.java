package uz.uzinfocom.app.modules.iam.application.organization.query.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Пользователь организации для справочного выбора.")
public record OrganizationUserLookupResponse(
        @Schema(description = "Уникальный идентификатор пользователя.", example = "1")
        Long id,
        @Schema(description = "Уникальный UUID пользователя.")
        UUID uuid,
        @Schema(description = "Имя пользователя.")
        String firstName,
        @Schema(description = "Фамилия пользователя.")
        String lastName,
        @Schema(description = "Отчество пользователя.")
        String middleName,
        @Schema(description = "Номер телефона пользователя.")
        String phoneNumber,
        @Schema(description = "Названия ролей пользователя.")
        List<String> roles
) {

    /**
     * Used by the JPQL constructor expression, which cannot project a collection;
     * roles are filled in afterwards via {@link #withRoles(List)}.
     */
    public OrganizationUserLookupResponse(
            Long id,
            UUID uuid,
            String firstName,
            String lastName,
            String middleName,
            String phoneNumber
    ) {
        this(id, uuid, firstName, lastName, middleName, phoneNumber, List.of());
    }

    public OrganizationUserLookupResponse withRoles(List<String> roles) {
        return new OrganizationUserLookupResponse(id, uuid, firstName, lastName, middleName, phoneNumber, roles);
    }
}
