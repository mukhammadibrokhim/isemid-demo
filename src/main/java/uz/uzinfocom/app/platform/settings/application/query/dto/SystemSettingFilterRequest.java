package uz.uzinfocom.app.platform.settings.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.settings.domain.SystemSettingValueType;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

@Schema(description = "Параметры фильтрации и пагинации списка настроек системы.")
public record SystemSettingFilterRequest(
        @Schema(description = "Номер страницы, начиная с 1.", example = "1")
        @Min(value = 1, message = "{pagination.page.min}")
        Integer page,

        @Schema(description = "Количество записей на странице. Максимальное значение — 200.", example = "20")
        @Min(value = 1, message = "{pagination.size.min}")
        @Max(value = 200, message = "{pagination.size.max}")
        Integer size,

        @Schema(
                description = "Поле сортировки.",
                example = "settingKey",
                allowableValues = {"id", "settingKey", "valueType", "active"}
        )
        String sortBy,

        @Schema(description = "Направление сортировки.", example = "asc", allowableValues = {"asc", "desc"})
        String sortDir,

        @Schema(description = "Текст поиска по ключу настройки.")
        @Size(max = 200, message = "{settings.setting-key.size}")
        String search,

        @Schema(description = "Текст поиска по значению настройки.")
        @Size(max = 2000, message = "{settings.setting-value.size}")
        String settingValue,

        @Schema(description = "Фильтр по типу значения настройки.", example = "STRING")
        SystemSettingValueType valueType,

        @Schema(description = "Фильтр по признаку активности.", example = "true")
        Boolean active,

        @Schema(description = "Фильтр по признаку удаления (мягкое удаление). По умолчанию (не указано) "
                + "возвращаются только неудалённые записи — как и раньше. Укажите true, чтобы найти "
                + "удалённую запись (например, чтобы восстановить её через PATCH .../{id}/restore, если id "
                + "уже неизвестен вызывающей стороне), или false, чтобы явно исключить удалённые (то же, "
                + "что и поведение по умолчанию).", example = "true")
        Boolean deleted
) implements PageableRequest {
}
