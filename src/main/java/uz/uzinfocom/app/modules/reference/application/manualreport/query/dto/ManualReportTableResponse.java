package uz.uzinfocom.app.modules.reference.application.manualreport.query.dto;

import uz.uzinfocom.app.modules.reference.domain.ManualReportAgeUnit;
import uz.uzinfocom.app.modules.reference.domain.ManualReportGender;
import uz.uzinfocom.app.modules.reference.domain.ManualReportCaseSource;
import uz.uzinfocom.app.modules.reference.domain.ManualReportPopulationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "Строка ручного отчёта для постраничного табличного ответа.")
public record ManualReportTableResponse(
        @Schema(description = "Внутренний идентификатор ручного отчёта.", example = "1")
        Long id,
        @Schema(description = "Уникальный код ручного отчёта.", example = "TUBERCULOSIS")
        String code,
        @Schema(description = "Краткое отображаемое наименование отчёта.", example = "TB")
        String shortName,
        @Schema(description = "Наименование отчёта на узбекском языке (латиница).", example = "Sil kasalligi bo‘yicha hisobot")
        String nameUz,
        @Schema(description = "Наименование отчёта на узбекском языке (кириллица).", example = "Сил касаллиги бўйича ҳисобот")
        String nameUzCyril,
        @Schema(description = "Наименование отчёта на русском языке.", example = "Отчёт по туберкулёзу")
        String nameRu,
        @Schema(description = "Наименование отчёта на каракалпакском языке.", example = "Túberkulez esabatı")
        String nameKaa,
        @Schema(description = "Учитываются ли соответствующие диагнозы в общих итогах.", example = "true")
        Boolean includeInTotal,
        @Schema(description = "Теги типа отчёта.", example = "[\"FORM_12\", \"FORM_13\"]")
        Set<String> reportTypes,
        @Schema(description = "Ограничение по полу пациента; null - без ограничения.")
        ManualReportGender gender,

        @Schema(description = "Нижняя граница возраста, включительно; null - без ограничения.")
        Integer ageFrom,

        @Schema(description = "Единица нижней границы возраста.")
        ManualReportAgeUnit ageFromUnit,

        @Schema(description = "Верхняя граница возраста, не включительно; null - без ограничения.")
        Integer ageTo,

        @Schema(description = "Единица верхней границы возраста.")
        ManualReportAgeUnit ageToUnit,

        @Schema(description = "Считать случаи только из этой формы. null - из обеих.")
        ManualReportCaseSource caseSource,

        @Schema(description = "Лабораторное подтверждение по форме №058: true - только подтверждённые, false - только с отметкой «не подтверждён», null - без ограничения. При заданном значении случаи формы №058-1 не считаются.")
        Boolean labConfirmation,

        @Schema(description = "Тип населения пациента (город / село). null - без ограничения.")
        ManualReportPopulationType populationType,

        @Schema(description = "true - только госпитализированные, false - только не госпитализированные, null - все.")
        Boolean hospitalized,

        @Schema(description = "Места возникновения заболевания (коды справочника DISEASE_PLACE) по форме №058. Пусто - без ограничения. При заданном значении случаи формы №058-1 не считаются.")
        Set<String> diseasePlaceCodes,

        @Schema(description = "Признак мягкого удаления.", example = "false")
        Boolean deleted
) {
}
