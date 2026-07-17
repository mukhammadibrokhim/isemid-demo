package uz.uzinfocom.app.modules.act.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;

import java.time.Instant;

@Schema(description = "Строка табличного (списочного) представления акта.")
public record ActTableResponse(
        @Schema(description = "Идентификатор акта.")
        Long id,

        @Schema(description = "Тип акта.")
        String actType,

        @Schema(description = "Текущий статус акта.")
        ActStatus status,

        @Schema(description = "Идентификатор супервайзера, назначившего акт.")
        Long assignedById,

        @Schema(description = "Идентификатор карты, к которой привязан акт — важно при просмотре списка "
                + "актов, охватывающего несколько карт (например \"Мои акты\").")
        Long cardId,

        @Schema(description = "Дата и время создания акта.")
        Instant createdAt
) {
}
