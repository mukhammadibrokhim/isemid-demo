package uz.uzinfocom.isemid.commons.dtos;


import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiErrorResponse {
    private boolean success;
    private int statusCode;
    private String message;
    private List<ErrorDetail> errors = new ArrayList<>();

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ErrorDetail {
        private String field;
        private String errorMessage;
    }
}
