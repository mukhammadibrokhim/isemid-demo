package uz.uzinfocom.app.integration.lis.reference.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Dropdown-shaped profession row — LIS's per-language names collapsed into
 * one {@code name} for the request locale ({@code nameUz} by default), see
 * {@code LocalizedTextResolver}.
 */
@Schema(description = "Профессия из справочника LIS, название по локали запроса.")
public record LisProfessionLookupResponse(
        @Schema(description = "Идентификатор профессии в LIS.", example = "3643")
        Long id,

        @Schema(description = "Код профессии в LIS.", example = "3643")
        String code,

        @Schema(description = "Название по локали запроса (по умолчанию nameUz).", example = "Sanitariya vrachi")
        String name
) {
}
