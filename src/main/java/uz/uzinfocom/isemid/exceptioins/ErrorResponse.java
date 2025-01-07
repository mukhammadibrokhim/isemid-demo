package uz.uzinfocom.isemid.exceptioins;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private boolean success;
    private int statusCode;
    private String message;
    private List<ErrorDetail> errors;}
