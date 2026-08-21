package uz.uzinfocom.app.modules.form129.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.form129.domain.enums.Form129Status;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Строка табличного (списочного) представления формы №129.")
public record Form129TableResponse(
        @Schema(description = "Идентификатор формы.")
        Long id,

        @Schema(description = "UUID формы.")
        UUID uuid,

        @Schema(description = "Дата и время создания формы.")
        Instant createdAt,

        @Schema(description = "Статус формы.")
        Form129Status status,

        @Schema(description = "Источник поступления формы.")
        String source,

        @Schema(description = "Идентификатор организации-отправителя.")
        Long senderOrganizationId,

        @Schema(description = "Идентификатор организации-получателя.")
        Long receiverOrganizationId,

        @Schema(description = "Краткие сведения о пациенте.")
        PatientShortResponse patient
) {
    @Schema(description = "Краткие сведения о пациенте для табличного представления формы.")
    public record PatientShortResponse(
            @Schema(description = "Идентификатор пациента.")
            Long id,

            @Schema(description = "Имя пациента.")
            String firstName,

            @Schema(description = "Фамилия пациента.")
            String lastName,

            @Schema(description = "Отчество пациента.")
            String middleName
    ) {
    }
}
