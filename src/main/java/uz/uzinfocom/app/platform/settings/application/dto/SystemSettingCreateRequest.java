package uz.uzinfocom.app.platform.settings.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.settings.domain.SystemSettingValueType;

@Schema(description = "Данные для создания настройки системы.")
public record SystemSettingCreateRequest(
        @Schema(
                description = "Уникальный ключ настройки.",
                example = "form058.allowed-sources",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "{settings.setting-key.required}")
        @Size(max = 200, message = "{settings.setting-key.size}")
        String settingKey,

        @Schema(description = "Значение настройки (хранится как текст, интерпретируется согласно valueType).")
        @Size(max = 2000, message = "{settings.setting-value.size}")
        String settingValue,

        @Schema(
                description = "Тип значения настройки.",
                example = "STRING",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "{settings.value-type.required}")
        SystemSettingValueType valueType,

        @Schema(description = "Описание назначения настройки (для администратора).")
        @Size(max = 1000, message = "{settings.description.size}")
        String description,

        @Schema(description = "Признак активности настройки.", example = "true")
        Boolean active
) {
}
