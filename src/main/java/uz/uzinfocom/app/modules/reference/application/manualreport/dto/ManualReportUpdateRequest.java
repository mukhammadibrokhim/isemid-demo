package uz.uzinfocom.app.modules.reference.application.manualreport.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import uz.uzinfocom.app.modules.reference.domain.ManualReportAgeUnit;
import uz.uzinfocom.app.modules.reference.domain.ManualReportGender;
import uz.uzinfocom.app.modules.reference.domain.ManualReportCaseSource;
import uz.uzinfocom.app.modules.reference.domain.ManualReportPopulationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Schema(description = "Данные для обновления записи ручного отчёта.")
public record ManualReportUpdateRequest(
        @Schema(
                description = "Уникальный код ручного отчёта.",
                example = "TUBERCULOSIS",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "{reference.code.required}")
        @Size(max = 50, message = "{reference.code.max_length}")
        String code,

        @Schema(description = "Краткое отображаемое наименование отчёта.", example = "TB")
        @Size(max = 100, message = "{reference.manual_report.short_name.size}")
        String shortName,

        @Schema(description = "Наименование отчёта на узбекском языке (латиница).", example = "Sil kasalligi bo‘yicha hisobot")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUz,

        @Schema(description = "Наименование отчёта на узбекском языке (кириллица).", example = "Сил касаллиги бўйича ҳисобот")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUzCyril,

        @Schema(description = "Наименование отчёта на русском языке.", example = "Отчёт по туберкулёзу")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameRu,

        @Schema(description = "Наименование отчёта на каракалпакском языке.", example = "Túberkulez esabatı")
        @Size(max = 255, message = "{reference.name.max_length}")
        String nameKaa,

        @Schema(description = "Учитываются ли соответствующие диагнозы в общих итогах.", example = "true")
        Boolean includeInTotal,

        @Schema(description = "Теги типа отчёта: FORM_12, FORM_13, FORM_28_1, FORM_28_2. Новые неизвестные значения отклоняются.")
        Set<String> reportTypes,

        @Schema(
                description = "Коды диагнозов МКБ-10, агрегируемые данным отчётом. Требуется хотя бы один код.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotEmpty(message = "{reference.manual_report.icd10_codes.required}")
        Set<String> icd10Codes,

        @Schema(description = "Считать только пациентов этого пола. null - без ограничения.")
        ManualReportGender gender,

        @Schema(description = "Нижняя граница возраста на дату регистрации извещения, включительно. Задаётся вместе с ageFromUnit.", example = "0")
        @Min(value = 0, message = "{reference.manual_report.age.range}")
        @Max(value = 50000, message = "{reference.manual_report.age.range}")
        Integer ageFrom,

        @Schema(description = "Единица нижней границы возраста.")
        ManualReportAgeUnit ageFromUnit,

        @Schema(description = "Верхняя граница возраста, НЕ включительно. Задаётся вместе с ageToUnit.", example = "28")
        @Min(value = 1, message = "{reference.manual_report.age.range}")
        @Max(value = 50000, message = "{reference.manual_report.age.range}")
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
        Set<String> diseasePlaceCodes
) {
}
