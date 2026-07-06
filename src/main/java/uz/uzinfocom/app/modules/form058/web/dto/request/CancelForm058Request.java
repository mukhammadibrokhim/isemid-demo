package uz.uzinfocom.app.modules.form058.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelForm058Request(
        @NotBlank(message = "{validation.form058.cancel-reason.required}")
        @Size(max = 1000, message = "{validation.form058.cancel-reason.size}")
        String reason
) {
}
