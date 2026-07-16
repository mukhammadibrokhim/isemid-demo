package uz.uzinfocom.app.shared.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;

@Schema(description = "Стандартная обертка постраничного ответа API в формате data, meta и links.")
public record PagedResponse<T>(
        @Schema(description = "Признак успешного выполнения запроса.", example = "true")
        boolean success,

        @Schema(description = "Сообщение для клиента.", example = "Успешный запрос.")
        String message,

        @Schema(description = "Список записей текущей страницы.")
        List<T> data,

        @Schema(description = "Метаданные ответа, включая параметры пагинации.")
        PageMeta meta,

        @Schema(description = "Публичные ссылки навигации, сформированные на основе app.base-uri.")
        PaginationLinks links
) {

    public PagedResponse {
        data = data == null ? List.of() : List.copyOf(data);
        meta = Objects.requireNonNull(meta, "meta must not be null");
        links = Objects.requireNonNull(links, "links must not be null");
    }
}
