package uz.uzinfocom.app.platform.dashboard.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.scope.OrganizationScopeMode;

@Schema(description = "Организационная область видимости, применённая к дашборду текущего пользователя.")
public record DashboardScopeResponse(
        @Schema(description = "Режим области видимости: ALL — вся республика, REGION — область/регион, "
                + "DISTRICT — район/город, ORGANIZATION — только своя организация.")
        OrganizationScopeMode mode,

        @Schema(description = "Код региона текущей организации (если применимо).")
        String regionCode,

        @Schema(description = "Наименование региона текущей организации (если применимо).")
        String regionName,

        @Schema(description = "Код района текущей организации (если применимо).")
        String districtCode,

        @Schema(description = "Наименование района текущей организации (если применимо).")
        String districtName
) {
}
