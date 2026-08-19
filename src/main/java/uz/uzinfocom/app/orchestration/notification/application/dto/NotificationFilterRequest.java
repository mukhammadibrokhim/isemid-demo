package uz.uzinfocom.app.orchestration.notification.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "Фильтр списка уведомлений текущего пользователя.")
public record NotificationFilterRequest(

        @Schema(description = "Номер страницы. Нумерация начинается с 1.", example = "1")
        @Min(value = 1, message = "{pagination.page.min}")
        Integer page,

        @Schema(description = "Количество записей на странице. Максимальное значение — 200.", example = "20")
        @Min(value = 1, message = "{pagination.size.min}")
        @Max(value = 200, message = "{pagination.size.max}")
        Integer size,

        @Schema(description = "Если true — только непрочитанные уведомления.")
        Boolean unreadOnly
) {
}
