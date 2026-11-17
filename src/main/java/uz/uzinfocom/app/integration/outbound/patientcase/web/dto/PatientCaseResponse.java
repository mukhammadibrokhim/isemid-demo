package uz.uzinfocom.app.integration.outbound.patientcase.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A patient's basic info plus their most recently submitted form058/form058-1
 * (each nullable independently - a patient may have one, both, or neither).
 * Deliberately its own, smaller shape rather than the frontend's
 * {@code PatientDetailResponse}/{@code Form058DetailResponse}: an external
 * integration client only needs basic patient info and the latest diagnosis,
 * not cancellation/approval/audit internals. The patient's identity documents
 * (PINFL, passport, ...) are not returned at all - the caller already supplied
 * the one it looked up by.
 */
@Schema(description = "Сводные сведения о пациенте и его последних формах №058/№058-1.")
public record PatientCaseResponse(
        @Schema(description = "Сведения о пациенте.")
        PatientSummary patient,

        @Schema(description = "Последняя поданная форма №058 (если есть).")
        Form058Summary latestForm058,

        @Schema(description = "Последняя поданная форма №058-1 (если есть).")
        Form0581Summary latestForm0581
) {

    @Schema(description = "Сведения о пациенте.")
    public record PatientSummary(
            @Schema(description = "UUID пациента.")
            UUID uuid,

            @Schema(description = "Имя пациента.")
            String firstName,

            @Schema(description = "Фамилия пациента.")
            String lastName,

            @Schema(description = "Отчество пациента.")
            String middleName,

            @Schema(description = "Дата рождения пациента.")
            LocalDate birthDate,

            @Schema(description = "Код пола пациента (по справочнику).")
            String genderCode
    ) {
    }

    @Schema(description = "Последняя форма №058.")
    public record Form058Summary(
            @Schema(description = "UUID формы.")
            UUID uuid,

            @Schema(description = "Статус формы.")
            String status,

            @Schema(description = "Источник поступления формы.")
            String source,

            @Schema(description = "Код диагноза по МКБ-10.")
            String mkb10Code,

            @Schema(description = "Наименование диагноза по МКБ-10.")
            String mkb10Name,

            @Schema(description = "Итоговый (подтверждённый) код диагноза по МКБ-10.")
            String finalMkb10Code,

            @Schema(description = "Итоговое (подтверждённое) наименование диагноза по МКБ-10.")
            String finalMkb10Name,

            @Schema(description = "Дата и время начала заболевания.")
            LocalDateTime diseaseDate,

            @Schema(description = "Дата и время первого обращения пациента за медицинской помощью.")
            LocalDateTime firstVisitDate,

            @Schema(description = "Дата и время осмотра пациента, по итогам которого заполнена форма.")
            LocalDateTime visitDate,

            @Schema(description = "Дата и время первичного сообщения о случае заболевания.")
            LocalDateTime initialReportDateTime
    ) {
    }

    @Schema(description = "Последняя форма №058-1.")
    public record Form0581Summary(
            @Schema(description = "UUID формы.")
            UUID uuid,

            @Schema(description = "Статус формы.")
            String status,

            @Schema(description = "Источник поступления формы.")
            String source,

            @Schema(description = "Код диагноза по МКБ-10.")
            String mkb10Code,

            @Schema(description = "Наименование диагноза по МКБ-10.")
            String mkb10Name,

            @Schema(description = "Итоговый (подтверждённый) код диагноза по МКБ-10.")
            String finalMkb10Code,

            @Schema(description = "Итоговое (подтверждённое) наименование диагноза по МКБ-10.")
            String finalMkb10Name,

            @Schema(description = "Дата и время получения укуса/травмы.")
            LocalDateTime injuryDateTime,

            @Schema(description = "Дата и время обращения в травматологический пункт (ДПУ).")
            LocalDateTime dpuVisitDateTime
    ) {
    }
}
