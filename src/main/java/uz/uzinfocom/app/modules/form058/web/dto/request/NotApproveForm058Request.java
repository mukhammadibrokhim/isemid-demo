package uz.uzinfocom.app.modules.form058.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NotApproveForm058Request(
        @NotBlank(message = "{validation.form058.not-approve-reason.required}")
        @Size(max = 1000, message = "{validation.form058.not-approve-reason.size}")
        String reason
) {
}
