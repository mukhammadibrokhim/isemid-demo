package uz.uzinfocom.app.modules.act.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.act.web.dto.request.act154.Act154SampleRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.ConditionInfoRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.EmployeeInfoRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.ActSubjectRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.PackageTypeInfoRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Акт 154 — далолатнома по отбору проб.")
public record Act154Request(
        @Schema(description = "Субъект акта (блок «Tashkilot turi»).")
        @Valid ActSubjectRequest subject,

        @Schema(description = "Вид деятельности («Faoliyat turi») — код из ref_catalog (type=ACTIVITY_TYPE).")
        @Size(max = 255) String activityTypeCode,
        LocalDateTime sampleTakenDateTime,
        LocalDateTime deliveredDateTime,
        @Schema(description = "Основание для отбора проб («Namuna olish uchun asos») — код из ref_catalog (type=SAMPLING_BASIS).")
        @Size(max = 50) String samplingBasisCode,
        @Schema(description = "Уточнение основания (текстовое поле рядом с селектом, например номер ГОСТ).")
        @Size(max = 500) String samplingBasisText,
        @Schema(description = "Цель проверки/отбора — код из ref_catalog (type=PURPOSE).")
        @Size(max = 50) String purposeCode,
        EmployeeInfoRequest sampler,
        EmployeeInfoRequest participant,
        @Size(max = 255) String manufacturingCompany,
        LocalDate manufactureDate,
        @Size(max = 255) String docNumberOfTakenObject,
        ConditionInfoRequest specialCondition,
        ConditionInfoRequest storageAndDeliveryCondition,
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
