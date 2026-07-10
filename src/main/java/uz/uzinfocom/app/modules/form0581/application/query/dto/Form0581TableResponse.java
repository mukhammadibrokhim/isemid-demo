package uz.uzinfocom.app.modules.form0581.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Строка табличного (списочного) представления формы №058-1.")
public record Form0581TableResponse(
        @Schema(description = "Идентификатор формы.")
        Long id,

        @Schema(description = "UUID формы.")
        UUID uuid,

        @Schema(description = "Дата и время создания формы.")
        Instant createdAt,

        @Schema(description = "Статус формы.")
        Form0581TableStatus status,

        @Schema(description = "Код диагноза по МКБ-10.")
        String mkb10Code,

        @Schema(description = "Наименование диагноза по МКБ-10.")
        String mkb10Name,

        @Schema(description = "Источник поступления формы.")
        String source,

        @Schema(description = "Идентификатор организации-отправителя.")
        Long senderOrganizationId,

        @Schema(description = "Наименование организации-отправителя.")
        String senderOrganizationName,

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
            String middleName,

            @Schema(description = "Наименование региона постоянного проживания.")
            String permanentRegionName,

            @Schema(description = "Наименование района постоянного проживания.")
            String permanentDistrictName,

            @Schema(description = "Наименование массива/махалли постоянного проживания.")
            String permanentNeighborhoodName,

            @Schema(description = "Улица постоянного проживания.")
            String permanentStreetAddress,

            @Schema(description = "Номер дома постоянного проживания.")
            String permanentHouseNumber,

            @Schema(description = "Номер квартиры постоянного проживания.")
            String permanentApartmentNumber
    ) {
    }
}
