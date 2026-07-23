package uz.uzinfocom.app.modules.act.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.act.web.dto.request.act154.Act154SampleRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.ConditionInfoRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.EmployeeInfoRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.InstitutionRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.PackageTypeInfoRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.PurposeRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Акт 154 — далолатнома по отбору проб.")
public record Act154Request(
        InstitutionRequest institution,

        @Size(max = 255) String title,
        Long actNumber,
        @Size(max = 255) String activityTypeCode,
        LocalDateTime sampleTakenDateTime,
        LocalDateTime deliveredDateTime,
        @Size(max = 500) String documentConfirmSampling,
        @Size(max = 500) String goal,
        PurposeRequest purpose,
        EmployeeInfoRequest sampler,
        EmployeeInfoRequest participant,
        @Size(max = 255) String manufacturingCompany,
        LocalDate manufactureDate,
        @Size(max = 255) String docNumberOfTakenObject,
        ConditionInfoRequest specialCondition,
        ConditionInfoRequest storageAndDeliveryCondition,
        Long lisOrganizationId,
        @Size(max = 500) String laboratoryAddress,
        PackageTypeInfoRequest packageTypeInfo,
        String additionalInfo,

        @Valid List<Act154SampleRequest> samples
) implements ActRequest {

    public Act154Request {
        samples = samples == null ? List.of() : List.copyOf(samples);
    }

    @Override
    public ActType type() {
        return ActType.ACT154;
    }
}
