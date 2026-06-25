package uz.uzinfocom.app.modules.form058.application.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;
import uz.uzinfocom.app.modules.form058.web.request.enums.Form058Direction;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

import java.time.LocalDate;

@Schema(description = "Form058 table filter.")
public record Form058Filter(
        @Min(1)
        Integer page,

        @Min(1)
        @Max(200)
        Integer size,

        String sortBy,

        String sortDir,

        FormStatus status,

        @NotNull(message = "direction is required")
        Form058Direction direction,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateFrom,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateTo,

        String search,

        Boolean hasLinkedCards
) implements PageableRequest {
}
