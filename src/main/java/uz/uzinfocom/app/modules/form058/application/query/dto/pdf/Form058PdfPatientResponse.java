package uz.uzinfocom.app.modules.form058.application.query.dto.pdf;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Сведения о пациенте для печатной формы №058.")
public record Form058PdfPatientResponse(
        @Schema(description = "Идентификатор пациента.")
        Long id,

        @Schema(description = "ФИО пациента одной строкой.")
        String fullName,

        @Schema(description = "Наименование пола пациента.")
        String genderName,

        @Schema(description = "Серия и номер паспорта пациента.")
        String passport,

        @Schema(description = "НЗБ/ПИНФЛ (JSHSHIR) пациента.")
        String nnuzb,

        @Schema(description = "Возраст пациента в полных годах.")
        Integer ageYears,

        @Schema(description = "Возраст пациента в месяцах (для детей до одного года).")
        Integer ageMonths,

        @Schema(description = "Дата рождения пациента.")
        LocalDate birthDate,

        @Schema(description = "Наименование семейного положения пациента.")
        String maritalStatusName,

        @Schema(description = "Наименование профессии пациента.")
        String professionName,

        @Schema(description = "Контактный номер телефона пациента.")
        String phoneNumber
) {
}
