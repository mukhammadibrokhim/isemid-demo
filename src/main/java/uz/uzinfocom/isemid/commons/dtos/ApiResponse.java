package uz.uzinfocom.isemid.commons.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private Boolean success;
    private final String message;
    private final T data;
    private final Object errors;

    public static <T> ApiResponse<T> success(MessageUtil messageUtil, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(messageUtil.get("message.success"))
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> deleteSuccess(MessageUtil messageUtil) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(messageUtil.get("message.deleted"))
                .data(null)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, Object errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .build();
    }
}
