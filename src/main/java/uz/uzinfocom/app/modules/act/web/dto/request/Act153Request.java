package uz.uzinfocom.app.modules.act.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.act.web.dto.request.act153.Act153SampleRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.ConditionInfoRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.ConservationTypeInfoRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.EmployeeInfoRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.InstitutionRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.PackageTypeInfoRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.PurposeRequest;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Акт 153 — далолатнома по отбору проб.")
public record Act153Request(
        InstitutionRequest institution,

        Long actNumber,
        @Size(max = 255) String activityTypeCode,
        String samplingDocuments,
        @Size(max = 500) String goal,
        LocalDateTime sampleTakenDateTime,
        LocalDateTime deliveredDateTime,
        PurposeRequest purpose,
        EmployeeInfoRequest sampler,
        EmployeeInfoRequest participant,
        ConditionInfoRequest specialCondition,
        ConditionInfoRequest storageAndDeliveryCondition,
        Long lisOrganizationId,
        @Size(max = 500) String laboratoryAddress,
        PackageTypeInfoRequest packageTypeInfo,
        ConservationTypeInfoRequest conservationTypeInfo,
        String additionalInfo,

        @Valid List<Act153SampleRequest> samples
) implements ActRequest {

    public Act153Request {
        samples = samples == null ? List.of() : List.copyOf(samples);
    }

    @Override
    public ActType type() {
        return ActType.ACT153;
    }
}
