package uz.uzinfocom.app.modules.card.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на прикрепление акта (лабораторного/процедурного назначения) к карте.")
public record AssignActRequest(
        @Schema(description = "Тип акта.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 50) String actType
) {
}
