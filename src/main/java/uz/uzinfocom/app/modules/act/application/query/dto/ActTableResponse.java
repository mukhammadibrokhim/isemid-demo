package uz.uzinfocom.app.modules.act.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;

import java.time.Instant;

@Schema(description = "Строка табличного (списочного) представления акта.")
public record ActTableResponse(
        @Schema(description = "Идентификатор акта.")
        Long id,

        @Schema(description = "Тип акта.")
        ActType actType,

        @Schema(description = "Наименование типа акта на текущем языке интерфейса.")
        String actTypeName,

        @Schema(description = "Текущий статус акта.")
        ActStatus status,

        @Schema(description = "Дата и время создания акта.")
        Instant createdAt
) {
}
