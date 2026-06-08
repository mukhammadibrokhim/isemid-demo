package uz.uzinfocom.app.shared.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "Метаданные постраничного ответа.")
public record PageMeta(
        @Schema(description = "Метаданные пагинации.")
        PaginationMetadata pagination
) {

    public PageMeta {
        pagination = Objects.requireNonNull(pagination, "pagination must not be null");
    }
}
