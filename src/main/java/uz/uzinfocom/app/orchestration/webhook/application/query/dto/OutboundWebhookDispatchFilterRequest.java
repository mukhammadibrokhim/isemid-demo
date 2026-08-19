package uz.uzinfocom.app.orchestration.webhook.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.orchestration.webhook.domain.OutboundWebhookDispatchStatus;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

@Schema(description = "Параметры фильтрации и пагинации списка исходящих webhook-задач.")
public record OutboundWebhookDispatchFilterRequest(
        @Schema(description = "Номер страницы, начиная с 1.", example = "1")
        Integer page,

        @Schema(description = "Количество записей на странице.", example = "20")
        Integer size,

        @Schema(description = "Поле для сортировки.", example = "id")
        String sortBy,

        @Schema(description = "Направление сортировки.", example = "desc", allowableValues = {"asc", "desc"})
        String sortDir,

        @Schema(description = "Фильтр по идентификатору интеграционного клиента.")
        Long integrationClientId,

        @Schema(description = "Фильтр по типу сущности.")
        AuditEntityType entityType,

        @Schema(description = "Фильтр по статусу доставки.")
        OutboundWebhookDispatchStatus status
) implements PageableRequest {
}
