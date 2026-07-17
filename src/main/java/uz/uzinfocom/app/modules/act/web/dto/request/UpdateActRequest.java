package uz.uzinfocom.app.modules.act.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Fills in the act's single generic outcome field. Kept intentionally
 * minimal — the legacy per-subtype act fields (act153/154/155/156/223/224)
 * remain out of scope; see {@link uz.uzinfocom.app.modules.act.domain.model.Act}.
 */
@Schema(description = "Запрос на сохранение результата акта (шаг \"Сохранить\", переводит акт в статус В работе).")
public record UpdateActRequest(
        @Schema(description = "Результат/заключение по акту.")
        @Size(max = 10000) String resultComment
) {
}
