package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.form129.domain.enums.Form129Status;

@Schema(description = "Количество извещений формы №129 с этим статусом (SENT — отправлено, ACCEPTED — "
        + "принято, CANCELED — отклонено получателем).")
public record StatisticsForm129StatusCountResponse(
        @Schema(description = "Статус формы №129.")
        Form129Status status,

        @Schema(description = "Количество извещений с этим статусом.")
        long count
) {
}
