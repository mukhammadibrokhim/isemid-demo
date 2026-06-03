package uz.uzinfocom.app.shared.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "Стандартная обертка постраничного ответа API.")
public record PagedResponse<T>(
        @Schema(description = "Признак успешного выполнения запроса.", example = "true")
        boolean success,

        @Schema(description = "Сообщение для клиента.", example = "Успешный запрос.")
        String message,

        @Schema(description = "Общее количество записей.", example = "125")
        long total,

        @Schema(description = "Номер текущей страницы.", example = "1")
        int page,

        @Schema(description = "Количество записей на странице.", example = "20")
        int size,

        @Schema(description = "Общее количество страниц.", example = "7")
        int totalPages,

        @Schema(description = "Признак первой страницы.", example = "true")
        boolean first,

        @Schema(description = "Признак последней страницы.", example = "false")
        boolean last,

        @Schema(description = "Список записей текущей страницы.")
        List<T> data
) {

    public PagedResponse {
        data = data == null ? List.of() : List.copyOf(data);
    }

    public static <T> PagedResponse<T> of(
            String message,
            long total,
            int page,
            int size,
            int totalPages,
            boolean first,
            boolean last,
            List<T> data
    ) {
        return new PagedResponse<>(
                true,
                message,
                total,
                page,
                size,
                totalPages,
                first,
                last,
                data
        );
    }

    public static <T> PagedResponse<T> fromPage(
            Page<T> pageData,
            String message
    ) {
        return new PagedResponse<>(
                true,
                message,
                pageData.getTotalElements(),
                pageData.getNumber() + 1,
                pageData.getSize(),
                pageData.getTotalPages(),
                pageData.isFirst(),
                pageData.isLast(),
                pageData.getContent()
        );
    }
}
