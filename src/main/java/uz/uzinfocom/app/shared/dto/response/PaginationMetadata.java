package uz.uzinfocom.app.shared.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Метаданные текущей страницы результата.")
public record PaginationMetadata(
        @Schema(description = "Номер текущей страницы, начиная с 1.", example = "1")
        int page,

        @Schema(description = "Запрошенный размер страницы.", example = "10")
        int size,

        @Schema(description = "Реальное количество элементов на текущей странице.", example = "10")
        int numberOfElements,

        @Schema(description = "Общее количество элементов во всем результате.", example = "1001")
        long totalElements,

        @Schema(description = "Общее количество страниц.", example = "101")
        int totalPages,

        @Schema(description = "Признак первой страницы.", example = "true")
        boolean first,

        @Schema(description = "Признак последней страницы.", example = "false")
        boolean last
) {
}
