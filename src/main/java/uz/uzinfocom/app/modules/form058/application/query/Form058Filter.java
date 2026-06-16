package uz.uzinfocom.app.modules.form058.application.query;

import org.springframework.format.annotation.DateTimeFormat;
import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

import java.time.LocalDate;

public record Form058Filter(
        String search,
        FormStatus status,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateTo,
        Integer page,
        Integer size,
        String sortBy,
        String sortDir
) implements PageableRequest {
}
