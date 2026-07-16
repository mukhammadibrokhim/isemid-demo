package uz.uzinfocom.app.shared.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Стандартная обертка успешного ответа API.")
public record ApiResponse<T>(
        @Schema(description = "Признак успешного выполнения запроса.", example = "true")
        boolean success,

        @Schema(description = "Сообщение для клиента.", example = "Успешный запрос.")
        String message,

        @Schema(description = "Полезная нагрузка ответа.")
        T data
) {
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, message, null);
    }
}
