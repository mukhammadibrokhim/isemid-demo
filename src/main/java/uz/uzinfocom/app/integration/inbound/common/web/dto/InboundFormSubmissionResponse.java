package uz.uzinfocom.app.integration.inbound.common.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Shared create-result shape across every inbound-integration form endpoint
 * (form058, form0581, and any future form type following the same recipe) —
 * {@code status} is a plain string rather than a form-specific enum so this
 * one record works for all of them.
 */
@Schema(description = "Результат приёма формы через интеграционный API.")
public record InboundFormSubmissionResponse(
        @Schema(description = "Идентификатор созданной формы.")
        Long id,

        @Schema(description = "UUID созданной формы.")
        UUID uuid,

        @Schema(description = "Статус формы после создания.")
        String status
) {
}
