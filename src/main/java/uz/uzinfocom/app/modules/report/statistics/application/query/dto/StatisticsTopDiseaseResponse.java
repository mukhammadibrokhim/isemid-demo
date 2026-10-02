package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Одна строка рейтинга «какая болезнь преобладает» для узла «Statistika» — код МКБ-10 и "
        + "число подтверждённых (status = APPROVED) случаев форм №058 + №058-1 за период, отнесённых к этому "
        + "коду по ЗАКЛЮЧИТЕЛЬНОМУ коду (final_icd10_code; без отката к первичному). Сортировка — по убыванию "
        + "числа случаев.")
public record StatisticsTopDiseaseResponse(
        @Schema(description = "Код МКБ-10 (заключительный диагноз).")
        String diagnosisCode,

        @Schema(description = "Локализованное наименование по справочнику МКБ-10; при отсутствии записи — сам код.")
        String diagnosisName,

        @Schema(description = "Число подтверждённых случаев форм №058 + №058-1 с этим заключительным кодом за период.")
        long confirmedCount
) {
}
