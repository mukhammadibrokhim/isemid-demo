package uz.uzinfocom.app.modules.form058.application.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

import java.time.LocalDate;

/**
 * Filter for {@code GET /v1/form-058/affiliated} - forms visible to the
 * current organization because the patient's workplace or place of study
 * (their {@code PatientAffiliation}) is that organization, regardless of
 * who sent/received the form. Deliberately has no {@code direction} or
 * {@code organizationId} field: neither has a meaning once access is scoped
 * by patient affiliation instead of sender/receiver - the earlier combined
 * filter ({@code Form058Filter.affiliation=true}) let a caller pass {@code
 * organizationId} that silently matched the sender/receiver columns instead
 * of the affiliation, which this dedicated filter makes impossible instead
 * of merely undocumented.
 */
@Schema(description = "Фильтр для получения списка форм 058, доступных организации через affiliation пациента "
        + "(место работы/учёбы).")
public record Form058AffiliatedFilter(

        @Schema(description = "Номер страницы. Нумерация начинается с 1.", example = "1")
        @Min(value = 1, message = "{pagination.page.min}")
        Integer page,

        @Schema(description = "Количество записей на странице. Максимальное значение — 200.", example = "20")
        @Min(value = 1, message = "{pagination.size.min}")
        @Max(value = 200, message = "{pagination.size.max}")
        Integer size,

        @Schema(description = "Поле, по которому выполняется сортировка.", example = "createdAt")
        String sortBy,

        @Schema(
                description = "Направление сортировки. Допустимые значения: ASC, DESC.",
                example = "DESC",
                allowableValues = {"ASC", "DESC"}
        )
        String sortDir,

        @Schema(description = "Статус формы 058.", example = "SENT")
        FormStatus status,

        @Schema(
                description = "Дата начала периода фильтрации по дате создания формы. Не может быть позже текущей даты.",
                example = "2026-05-01",
                type = "string",
                format = "date"
        )
        @PastOrPresent(message = "{form058.filter.date_from.past_or_present}")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateFrom,

        @Schema(
                description = "Дата окончания периода фильтрации по дате создания формы. Не может быть позже текущей даты.",
                example = "2026-05-26",
                type = "string",
                format = "date"
        )
        @PastOrPresent(message = "{form058.filter.date_to.past_or_present}")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateTo,

        @Schema(description = "Внутренний идентификатор формы 058.", example = "12345")
        Long id,

        @Schema(
                description = "Значение документа пациента. Например, ПИНФЛ, паспорт или другой идентификатор.",
                example = "52712046520013"
        )
        String documentValue,

        @Schema(description = "Код диагноза по МКБ-10.", example = "A09")
        String icd10Code,

        @Schema(description = "Источник создания формы 058.", example = "MANUAL")
        String source,

        @Schema(description = "Признак наличия связанных карт у формы 058.", example = "true")
        Boolean hasLinkedCards

) implements PageableRequest, Form058FilterFields {

        /**
         * Mirrors {@link Form058Filter#hasNoAdditionalFilters()} - used to decide
         * whether the pagination total can use a fast planner estimate instead of
         * an exact {@code COUNT(*)}.
         */
        public boolean hasNoAdditionalFilters() {
                return status == null
                        && dateFrom == null
                        && dateTo == null
                        && id == null
                        && !StringUtils.hasText(documentValue)
                        && !StringUtils.hasText(icd10Code)
                        && !StringUtils.hasText(source)
                        && hasLinkedCards == null;
        }

        @Schema(hidden = true)
        @AssertTrue(message = "{form058.filter.date_range.invalid}")
        public boolean isDateRangeValid() {
                if (dateFrom == null || dateTo == null) {
                        return true;
                }

                return !dateFrom.isAfter(dateTo);
        }
}
