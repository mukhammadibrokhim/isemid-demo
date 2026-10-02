package uz.uzinfocom.app.modules.act.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.act.web.dto.request.act223.Act223SampleRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.ConditionInfoRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.EmployeeInfoRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.ActSubjectRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.PackageTypeInfoRequest;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Акт 223 — далолатнома по отбору проб.")
public record Act223Request(
        @Schema(description = "Субъект акта (блок «Tashkilot turi»).")
        @Valid ActSubjectRequest subject,

        @Schema(description = "Основание для отбора проб («Namuna olish uchun asos») — код из ref_catalog (type=SAMPLING_BASIS).")
        @Size(max = 50) String samplingBasisCode,
        @Schema(description = "Вид деятельности («Faoliyat turi») — код из ref_catalog (type=ACTIVITY_TYPE).")
        @Size(max = 255) String activityTypeCode,
        EmployeeInfoRequest sampler,
        EmployeeInfoRequest participant,
        @Schema(description = "Цель проверки/отбора — код из ref_catalog (type=PURPOSE).")
        @Size(max = 50) String purposeCode,
        LocalDateTime sampleTakenDateTime,
        LocalDateTime deliveredDateTime,
        ConditionInfoRequest specialCondition,
        ConditionInfoRequest storageAndDeliveryCondition,
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
