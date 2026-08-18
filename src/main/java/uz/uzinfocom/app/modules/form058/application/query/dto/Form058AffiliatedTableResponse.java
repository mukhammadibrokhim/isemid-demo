package uz.uzinfocom.app.modules.form058.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.patient.domain.enums.AffiliationType;

/**
 * Row shape for {@code GET /v1/form-058/affiliated} - wraps the same {@link
 * Form058TableResponse} the direction-scoped listing returns, plus the one
 * extra fact that only makes sense in this mode: which kind of affiliation
 * (workplace/place of study) is the reason this organization can see the
 * form at all. Kept as a separate wrapper rather than a field bolted onto
 * {@link Form058TableResponse} itself so the base response doesn't grow a
 * field that's meaningless (always null) for every other listing.
 */
@Schema(description = "Строка табличного представления формы №058 в списке, доступном через affiliation "
        + "пациента - дополняет обычную строку типом принадлежности, из-за которой форма видна текущей "
        + "организации.")
public record Form058AffiliatedTableResponse(
        Form058TableResponse form,

        @Schema(description = "Тип принадлежности пациента к текущей организации: место работы или учёбы.")
        AffiliationType affiliationType
) {
}
