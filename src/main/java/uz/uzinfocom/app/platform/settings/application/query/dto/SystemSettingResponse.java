package uz.uzinfocom.app.platform.settings.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;
import uz.uzinfocom.app.platform.settings.domain.SystemSettingValueType;

@Schema(description = "Детальный ответ по настройке системы.")
public record SystemSettingResponse(
        @Schema(description = "Внутренний идентификатор настройки.", example = "1")
        Long id,

        @Schema(description = "Уникальный ключ настройки.", example = "form058.allowed-sources")
        String settingKey,

        @Schema(description = "Значение настройки.")
        String settingValue,

        @Schema(description = "Тип значения настройки.", example = "STRING")
        SystemSettingValueType valueType,

        @Schema(description = "Описание назначения настройки.")
        String description,

        @Schema(description = "Признак активности настройки.", example = "true")
        Boolean active,

        @Schema(description = "Признак мягкого удаления.", example = "false")
        Boolean deleted,

        @Schema(description = "Сведения об аудите записи.")
        AuditResponse audit
) {
}
