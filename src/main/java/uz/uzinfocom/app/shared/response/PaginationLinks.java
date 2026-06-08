package uz.uzinfocom.app.shared.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "Публичные ссылки навигации для постраничного ответа.")
public record PaginationLinks(
        @Schema(
                description = "Ссылка на текущую страницу, сформированная на основе app.base-uri.",
                example = "https://test-ykem.sanepid.uz/api/v1/users?page=1&size=10"
        )
        String self,

        @Schema(
                description = "Ссылка на первую страницу, сформированная на основе app.base-uri.",
                example = "https://test-ykem.sanepid.uz/api/v1/users?page=1&size=10"
        )
        String first,

        @Schema(
                description = "Ссылка на предыдущую страницу. Значение null для первой страницы.",
                nullable = true,
                example = "https://test-ykem.sanepid.uz/api/v1/users?page=1&size=10"
        )
        String prev,

        @Schema(
                description = "Ссылка на следующую страницу. Значение null для последней страницы.",
                nullable = true,
                example = "https://test-ykem.sanepid.uz/api/v1/users?page=2&size=10"
        )
        String next,

        @Schema(
                description = "Ссылка на последнюю страницу, сформированная на основе app.base-uri.",
                example = "https://test-ykem.sanepid.uz/api/v1/users?page=101&size=10"
        )
        String last
) {

    public PaginationLinks {
        self = Objects.requireNonNull(self, "self must not be null");
        first = Objects.requireNonNull(first, "first must not be null");
        last = Objects.requireNonNull(last, "last must not be null");
    }
}
