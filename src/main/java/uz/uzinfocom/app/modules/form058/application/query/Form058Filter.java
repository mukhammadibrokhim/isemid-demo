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

        @Min(value = 1, message = "{pagination.page.min}")
        Integer page,

        @Min(value = 1, message = "{pagination.size.min}")
        @Max(value = 200, message = "{pagination.size.max}")
        Integer size,

        String sortBy,

        String sortDir,

        FormStatus status,

        @NotNull(message = "{form058.filter.direction.required}")
        Form058Direction direction,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateFrom,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateTo,

        Long id,

        String documentValue,

        String mkb10Code,

        Long senderOrganizationId,

        String regionCode,

        String districtCode,

        String source,

        Boolean affiliation,

        Boolean hasLinkedCards

) implements PageableRequest {
        public boolean isAffiliationFilterEnabled() {
                return Boolean.TRUE.equals(affiliation);
        }
}