package uz.uzinfocom.app.modules.form058.web.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssignCardRequest(
        @NotNull(message = "{validation.form058.id.required}")
        @Positive(message = "{validation.must_be_positive}")
        Long formId,

        @NotNull(message = "{validation.form058.card-id.required}")
        @Positive(message = "{validation.must_be_positive}")
        Long cardId
) {
}
