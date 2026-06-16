package uz.uzinfocom.app.modules.form058.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApproveForm058Request(
        @NotBlank(message = "{validation.form058.mkb10-code.required}")
        @Size(max = 20, message = "{validation.form058.mkb10-code.size}")
        String finalMkb10Code,

        @NotBlank(message = "{validation.form058.mkb10-name.required}")
        @Size(max = 512, message = "{validation.form058.mkb10-name.size}")
        String finalMkb10Name
) {
}
