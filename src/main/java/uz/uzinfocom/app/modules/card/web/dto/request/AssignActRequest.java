package uz.uzinfocom.app.modules.card.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssignActRequest(
        @NotBlank @Size(max = 50) String actType
) {
}
