package uz.uzinfocom.app.platform.settings.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;

@Schema(description = "Детальный ответ по политике доступа к маршруту.")
public record RouteAccessPolicyDetailResponse(
        @Schema(description = "Внутренний идентификатор записи.", example = "1")
        Long id,

        @Schema(description = "Шаблон маршрута (Ant-паттерн).", example = "/v1/admin/**")
        String pattern,

        @Schema(description = "Порядок сопоставления шаблонов (первое совпадение побеждает).", example = "10")
        Integer displayOrder,

        @Schema(description = "Признак публичного (неаутентифицированного) доступа.", example = "false")
        Boolean open,

        @Schema(description = "Признак обязательности заголовка организации.", example = "true")
        Boolean organizationHeaderRequired,

        @Schema(description = "Признак обязательности проверки роли.", example = "true")
        Boolean roleValidationRequired,

        @Schema(description = "Признак активности правила.", example = "true")
        Boolean enabled,

        @Schema(description = "Описание назначения правила.", nullable = true)
        String description,

        @Schema(description = "Сведения об аудите записи.")
        AuditResponse audit
) {
}
