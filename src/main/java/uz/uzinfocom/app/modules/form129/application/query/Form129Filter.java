package uz.uzinfocom.app.modules.form129.application.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.springframework.format.annotation.DateTimeFormat;
import uz.uzinfocom.app.modules.form129.domain.enums.Form129Status;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

import java.time.LocalDate;

@Schema(description = "Фильтр для получения списка форм №129.")
public record Form129Filter(

        @Schema(description = "Номер страницы. Нумерация начинается с 1.", example = "1")
        @Min(value = 1, message = "{pagination.page.min}")
        Integer page,

        @Schema(description = "Количество записей на странице. Максимальное значение — 200.", example = "20")
        @Min(value = 1, message = "{pagination.size.min}")
        @Max(value = 200, message = "{pagination.size.max}")
        Integer size,

        @Schema(description = "Поле, по которому выполняется сортировка.", example = "createdAt")
        String sortBy,

        @Schema(description = "Направление сортировки. Допустимые значения: ASC, DESC.",
                example = "DESC", allowableValues = {"ASC", "DESC"})
        String sortDir,

        @Schema(description = "Статус формы №129.", example = "SENT")
        Form129Status status,

        @Schema(description = """
                Направление списка форм №129.
                INCOMING — входящие формы, фильтрация по receiverOrganizationId.
                OUTGOING — исходящие формы, фильтрация по senderOrganizationId.
                ALL — все доступные формы в рамках текущего scope.
                """,
                example = "INCOMING", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{form129.filter.direction.required}")
        Form129Direction direction,

        @Schema(description = "Дата начала периода фильтрации по дате создания формы. Не может быть позже текущей даты.",
                example = "2026-05-01", type = "string", format = "date")
        @PastOrPresent(message = "{form129.filter.date_from.past_or_present}")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateFrom,

        @Schema(description = "Дата окончания периода фильтрации по дате создания формы. Не может быть позже текущей даты.",
                example = "2026-05-26", type = "string", format = "date")
        @PastOrPresent(message = "{form129.filter.date_to.past_or_present}")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateTo,

        @Schema(description = "Внутренний идентификатор формы №129.", example = "12345")
        Long id,

        @Schema(description = "Значение документа пациента. Например, ПИНФЛ, паспорт или другой идентификатор.",
                example = "52712046520013")
        String documentValue,

        @Schema(description = """
                Идентификатор организации для фильтрации.
                Сопоставляется с sender- или receiver-организацией формы в зависимости от direction:
                OUTGOING — сравнивается с senderOrganizationId, INCOMING — с receiverOrganizationId,
                ALL — совпадение по любой из сторон.
                """, example = "336")
        Long organizationId,

        @Schema(description = "Источник создания формы №129.", example = "MANUAL")
        String source

) implements PageableRequest {

    @Schema(hidden = true)
    @AssertTrue(message = "{form129.filter.date_range.invalid}")
    public boolean isDateRangeValid() {
        if (dateFrom == null || dateTo == null) {
            return true;
        }
        return !dateFrom.isAfter(dateTo);
    }
}
