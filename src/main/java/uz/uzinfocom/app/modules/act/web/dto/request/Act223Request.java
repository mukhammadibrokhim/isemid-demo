package uz.uzinfocom.app.modules.act.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.act.web.dto.request.act223.Act223SampleRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.ConditionInfoRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.EmployeeInfoRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.InstitutionRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.PackageTypeInfoRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.PurposeRequest;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Акт 223 — далолатнома по отбору проб.")
public record Act223Request(
        InstitutionRequest institution,

        Long actNumber,
        @Size(max = 500) String supportingDocumentsForSampling,
        @Size(max = 500) String goal,
        @Size(max = 255) String activityTypeCode,
        EmployeeInfoRequest sampler,
        EmployeeInfoRequest participant,
        PurposeRequest purpose,
        LocalDateTime sampleTakenDateTime,
        LocalDateTime deliveredDateTime,
        ConditionInfoRequest specialCondition,
        ConditionInfoRequest storageAndDeliveryCondition,
        Long lisOrganizationId,
        @Size(max = 500) String laboratoryAddress,
        PackageTypeInfoRequest packageTypeInfo,
        String additionalInfo,

        @Valid List<Act223SampleRequest> samples
) implements ActRequest {

    public Act223Request {
        samples = samples == null ? List.of() : List.copyOf(samples);
    }

    @Override
    public ActType type() {
        return ActType.ACT223;
    }
}
