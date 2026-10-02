package uz.uzinfocom.app.modules.act.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.ActSubjectResponse;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

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

        @Schema(description = "Субъект акта (блок «Tashkilot turi»); subject.label — готовая подпись для строки. "
                + "null пока не заполнено.")
        ActSubjectResponse subject,

        @Schema(description = "Номер акта. Генерируется сервером (равен id акта); у мигрированных актов — номер из бумажного бланка.")
        Long actNumber,

        @Schema(description = "Идентификатор карты, к которой привязан акт.")
        Long cardId,

        @Schema(description = "Тип карты, к которой привязан акт.")
        CardType cardType,

        @Schema(description = "Идентификатор пользователя, назначившего акт (супервайзер).")
        Long assignedById,

        @Schema(description = "Дата и время создания акта.")
        Instant createdAt
) {
}
