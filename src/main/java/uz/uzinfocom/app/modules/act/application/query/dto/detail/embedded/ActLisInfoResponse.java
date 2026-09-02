package uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * The act's LIS transmission state, surfaced on every {@code Act…DetailResponse}
 * so the frontend can explain a {@code SEND_FAILED}, {@code RETURNED_BY_LIS}
 * or {@code COMPLETED} act rather than just showing the status.
 *
 * <p>Always present (never {@code null}); before the first send attempt
 * {@code attempt} is {@code 0} and everything else is {@code null}. Mirrors
 * the {@code LisInfo} embeddable on the entity one-to-one.
 */
@Schema(description = "Состояние передачи акта в LIS: попытки, последняя ошибка/причина возврата, ответ LIS.")
public record ActLisInfoResponse(
        @Schema(description = "Сколько раз акт отправляли в LIS (0 — ни разу).")
        Integer attempt,

        @Schema(description = "Время последней отправки в LIS.")
        LocalDateTime sentDate,

        @Schema(description = "Идентификатор акта на стороне LIS (после принятия/ответа).")
        Long actId,

        @Schema(description = "Причина последней неудачи отправки или возврата акта из LIS. "
                + "Очищается при следующей попытке.")
        String lastError,

        @Schema(description = "Ответ LIS целиком, в исходном виде — доступен после COMPLETED или RETURNED_BY_LIS.")
        Map<String, Object> response
) {
}
