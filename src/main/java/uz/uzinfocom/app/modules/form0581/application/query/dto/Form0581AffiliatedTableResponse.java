package uz.uzinfocom.app.modules.form0581.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.patient.domain.enums.AffiliationType;

/**
 * Row shape for {@code GET /v1/form-058-1/affiliated} - wraps the same
 * {@link Form0581TableResponse} the direction-scoped listing returns, plus
 * the one extra fact that only makes sense in this mode: which kind of
 * affiliation (workplace/place of study) is the reason this organization can
 * see the form at all. Mirrors {@code Form058AffiliatedTableResponse}.
 */
@Schema(description = "Строка табличного представления формы №058-1 в списке, доступном через affiliation "
        + "пациента - дополняет обычную строку типом принадлежности, из-за которой форма видна текущей "
        + "организации.")
public record Form0581AffiliatedTableResponse(
        Form0581TableResponse form,

        @Schema(description = "Тип принадлежности пациента к текущей организации: место работы или учёбы.")
        AffiliationType affiliationType
) {
}
