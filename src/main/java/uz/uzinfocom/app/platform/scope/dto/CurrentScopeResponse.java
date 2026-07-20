package uz.uzinfocom.app.platform.scope.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.platform.iam.domain.enums.OrganizationLevel;
import uz.uzinfocom.app.platform.scope.OrganizationScopeMode;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;

import java.util.UUID;

@Schema(description = "Текущая область доступа пользователя по выбранной организации.")
public record CurrentScopeResponse(

        @Schema(description = "Режим организационного доступа.")
        OrganizationScopeMode mode,

        @Schema(description = "Внутренний ID выбранной организации.")
        Long organizationId,

        @Schema(description = "UUID выбранной организации.")
        UUID organizationUuid,

        @Schema(description = "Медицинский тип выбранной организации.")
        MedicalType medicalType,

        @Schema(description = "Уровень выбранной организации.")
        OrganizationLevel levelType,

        @Schema(description = "Код региона, доступного пользователю.")
        String regionCode,

        @Schema(description = "Наименование региона, доступного пользователю (на языке текущего запроса).")
        String regionName,

        @Schema(description = "Код района или города, доступного пользователю.")
        String districtCode,

        @Schema(description = "Наименование района или города, доступного пользователю (на языке текущего запроса).")
        String districtName
) {

        public static CurrentScopeResponse from(ResolvedOrganizationScope scope, String regionName, String districtName) {
                return new CurrentScopeResponse(
                        scope.mode(),
                        scope.organizationId(),
                        scope.organizationUuid(),
                        scope.medicalType(),
                        scope.levelType(),
                        scope.regionCode(),
                        regionName,
                        scope.districtCode(),
                        districtName
                );
        }
}