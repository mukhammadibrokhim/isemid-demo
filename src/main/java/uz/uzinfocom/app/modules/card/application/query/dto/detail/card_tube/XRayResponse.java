package uz.uzinfocom.app.modules.card.application.query.dto.detail.card_tube;

import java.time.LocalDate;

public record XRayResponse(
        Long id,
        LocalDate xrayDate,
        String xrayPlace,
        String result
) {
}
