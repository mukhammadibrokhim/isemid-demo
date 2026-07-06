package uz.uzinfocom.app.modules.form058.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeleteForm058Request(

        @NotBlank(message = "{validation.form058.delete-reason.required}")
        @Size(
                max = 2_000,
                message = "{validation.form058.delete-reason.max-length}"
        )
        String reason

) {

    public DeleteForm058Request {
        reason = reason == null ? null : reason.trim();
    }
}