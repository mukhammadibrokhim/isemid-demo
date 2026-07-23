package uz.uzinfocom.app.modules.act.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.act.web.dto.request.act155.Act155SampleRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.InstitutionRequest;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Акт 155 — далолатнома по отбору проб.")
public record Act155Request(
        InstitutionRequest institution,

        @Size(max = 255) String title,
        Integer tin,
        @Size(max = 255) String institutionName,
        @Size(max = 500) String institutionAddress,
        @Size(max = 255) String activityTypeCode,
        LocalDate selectedDate,
        @Size(max = 255) String samplerFullName,
        @Size(max = 255) String samplerPosition,
        @Size(max = 255) String objectRepresentativeFullName,
        @Size(max = 255) String objectRepresentativePosition,
        String additionalInfo,

        @Valid List<Act155SampleRequest> samples
) implements ActRequest {

    public Act155Request {
        samples = samples == null ? List.of() : List.copyOf(samples);
    }

    @Override
    public ActType type() {
        return ActType.ACT155;
    }
}
