package uz.uzinfocom.app.modules.patient.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(description = "Полные детальные сведения о пациенте.")
public record PatientDetailResponse(
        @Schema(description = "Идентификатор пациента.")
        Long id,

        @Schema(description = "UUID пациента.")
        UUID uuid,

        @Schema(description = "Имя пациента.")
        String firstName,

        @Schema(description = "Фамилия пациента.")
        String lastName,

        @Schema(description = "Отчество пациента.")
        String middleName,

        @Schema(description = "Полное имя пациента (ФИО одной строкой).")
        String fullName,

        @Schema(description = "Дата рождения пациента.")
        LocalDate birthDate,

        @Schema(description = "Возраст пациента в полных годах.")
        Integer ageYears,

        @Schema(description = "Возраст пациента в месяцах (для детей до одного года).")
        Integer ageMonths,

        @Schema(description = "Код пола пациента (по справочнику).")
        String genderCode,

        @Schema(description = "Контактный номер телефона пациента.")
        String phoneNumber,

        @Schema(description = "Код степени родства с контактным лицом (если сведения предоставлены родственником).")
        String kinshipDegree,

        @Schema(description = "ФИО родственника/контактного лица.")
        String kinshipFullName,

        @Schema(description = "Код статуса проживания пациента (по справочнику).")
        String residentialStatusCode,

        @Schema(description = "Код семейного положения пациента (по справочнику).")
        String maritalStatusCode,

        @Schema(description = "Код типа населения (городское/сельское, по справочнику).")
        String populationTypeCode,

        @Schema(description = "Код категории пациента (по справочнику).")
        String categoryCode,

        @Schema(description = "Код профессии пациента (по справочнику).")
        String professionCode,

        @Schema(description = "Список документов, удостоверяющих личность пациента.")
        List<PatientIdentifierDetailResponse> identifiers,

        @Schema(description = "Список адресов пациента.")
        List<PatientAddressDetailResponse> addresses,

        @Schema(description = "Список принадлежностей пациента (место работы/учёбы).")
        List<PatientAffiliationDetailResponse> affiliations
) {
}
