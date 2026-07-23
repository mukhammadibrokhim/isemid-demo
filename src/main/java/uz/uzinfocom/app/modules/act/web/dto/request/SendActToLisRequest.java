package uz.uzinfocom.app.modules.act.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import uz.uzinfocom.app.integration.lis.client.dto.LisPriority;

@Schema(description = "Данные, которые сотрудник указывает при отправке акта в LIS.")
public record SendActToLisRequest(

        @Schema(description = "Идентификатор лаборатории LIS, куда направляется акт.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Positive Long labId,

        @Schema(description = "Приоритет выполнения в LIS.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull LisPriority priority,

        @Schema(description = "Услуга платная или нет.")
        Boolean paid,

        @Schema(description = "Разрешить LIS принять акт даже если она уже видела акт с этим номером ранее "
                + "(повторная отправка после сбоя).")
        Boolean force

) {
    public SendActToLisRequest {
        paid = paid != null && paid;
        force = force != null && force;
    }
}
