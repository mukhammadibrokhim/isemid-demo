package uz.uzinfocom.app.modules.card.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.platform.iam.application.user.query.dto.UserMiniResponse;

import java.time.Instant;

@Schema(description = "Строка табличного (списочного) представления карты — только базовые поля, "
        + "без подтипо-специфичных данных, чтобы список формировался без лишних JOIN-ов по подтипам карт.")
public record CardTableResponse(
        @Schema(description = "Идентификатор карты.")
        Long id,

        @Schema(description = "Дата и время назначения (создания) карты.")
        Instant createdAt,

        @Schema(description = "Тип карты.")
        CardType cardType,

        @Schema(description = "Наименование типа карты на текущем языке интерфейса.")
        String cardTypeName,

        @Schema(description = "Текущий статус карты.")
        CardStatus status,

        @Schema(description = "Наименование статуса карты на текущем языке интерфейса.")
        String statusName,

        @Schema(description = "Супервайзер, назначивший карту (ответственный сотрудник).")
        UserMiniResponse assignedBy,

        @Schema(description = "Идентификатор ответственной организации (принявшей случай по форме №058).")
        Long organizationId,

        @Schema(description = "Наименование ответственной организации на текущем языке интерфейса.")
        String organizationName,

        @Schema(description = "Идентификатор формы №058, к которой привязана карта — важно при просмотре "
                + "списка карт, охватывающего несколько форм (например \"Мои карты\").")
        Long formId,

        @Schema(description = "Краткие сведения о пациенте.")
        PatientShortResponse patient
) {
    @Schema(description = "Краткие сведения о пациенте для табличного представления карты.")
    public record PatientShortResponse(
            @Schema(description = "Идентификатор пациента.")
            Long id,

            @Schema(description = "Имя пациента.")
            String firstName,

            @Schema(description = "Фамилия пациента.")
            String lastName
    ) {
    }
}
