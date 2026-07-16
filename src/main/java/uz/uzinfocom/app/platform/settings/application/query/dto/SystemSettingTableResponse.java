package uz.uzinfocom.app.platform.settings.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.settings.domain.SystemSettingValueType;

@Schema(description = "Настройка системы для постраничного табличного ответа.")
public record SystemSettingTableResponse(
        @Schema(description = "Внутренний идентификатор настройки.", example = "1")
        Long id,

        @Schema(description = "Уникальный ключ настройки.", example = "form058.allowed-sources")
        String settingKey,

        @Schema(description = "Значение настройки.")
        String settingValue,

        @Schema(description = "Тип значения настройки.", example = "STRING")
        SystemSettingValueType valueType,

        @Schema(description = "Признак активности настройки.", example = "true")
        Boolean active
) {
}
