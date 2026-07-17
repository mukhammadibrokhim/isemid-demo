package uz.uzinfocom.app.modules.act.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;

import java.time.LocalDate;

@Schema(description = "Детальные сведения по акту.")
public record ActDetailResponse(
        @Schema(description = "Идентификатор акта.")
        Long id,

        @Schema(description = "Тип акта.")
        String actType,

        @Schema(description = "Текущий статус акта в его жизненном цикле.")
        ActStatus status,

        @Schema(description = "Идентификатор карты, к которой привязан акт.")
        Long cardId,

        @Schema(description = "Идентификатор супервайзера, назначившего акт.")
        Long assignedById,

        @Schema(description = "Комментарий супервайзера (например, причина отклонения при проверке).")
        String supervisorComment,

        @Schema(description = "Комментарий прикреплённого сотрудника (например, причина отказа от акта).")
        String attachedUserComment,

        @Schema(description = "Дата завершения заполнения акта.")
        LocalDate completedDate,

        @Schema(description = "Результат/заключение по акту.")
        String resultComment
) {
}
