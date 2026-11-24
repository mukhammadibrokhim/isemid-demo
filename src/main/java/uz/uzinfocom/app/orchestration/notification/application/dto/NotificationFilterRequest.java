package uz.uzinfocom.app.orchestration.notification.application.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.orchestration.notification.domain.NotificationType;

import java.time.LocalDate;
import java.util.List;

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
        Boolean unreadOnly,

        @ArraySchema(
                arraySchema = @Schema(description = "Типы уведомлений. Уведомление должно иметь один из указанных типов."),
                schema = @Schema(description = "Тип уведомления.", implementation = NotificationType.class)
        )
        List<NotificationType> types,

        @ArraySchema(
                arraySchema = @Schema(description = "Типы связанных сущностей. "
                        + "Уведомление должно быть связано с одним из указанных типов сущностей."),
                schema = @Schema(description = "Тип связанной сущности.", implementation = AuditEntityType.class)
        )
        List<AuditEntityType> entityTypes,

        @Schema(description = "Начало периода (включительно) по дате события уведомления.", example = "2026-09-01")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,

        @Schema(description = "Конец периода (включительно) по дате события уведомления.", example = "2026-09-16")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to
) {
}
