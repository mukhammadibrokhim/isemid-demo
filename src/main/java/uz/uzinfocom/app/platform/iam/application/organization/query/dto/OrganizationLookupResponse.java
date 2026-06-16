package uz.uzinfocom.app.platform.iam.application.organization.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.platform.iam.domain.enums.OrganizationLevel;

import java.util.UUID;

@Schema(description = "Организация для справочного выбора.")
public record OrganizationLookupResponse(
        @Schema(description = "Уникальный идентификатор записи.", example = "1")
        Long id,
        @Schema(description = "Уникальный UUID записи.")
        UUID uuid,
        @Schema(description = "Наименование организации.")
        String name,
        @Schema(description = "Признак активности записи.", example = "true")
        Boolean active,
        @Schema(description = "Уровень организации.")
        OrganizationLevel levelType,
        @Schema(description = "Тип медицинской организации.")
        MedicalType medicalType
) {
}
