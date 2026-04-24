package uz.uzinfocom.isemid.features.act.dtos.requests.act223;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;
import uz.uzinfocom.isemid.features.act.models.common.ConditionInfo;
import uz.uzinfocom.isemid.features.act.models.common.EmployeeInfo;
import uz.uzinfocom.isemid.features.act.models.common.Institution;
import uz.uzinfocom.isemid.features.act.models.common.PackageTypeInfo;
import uz.uzinfocom.isemid.features.act.models.common.Purpose;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request object for Act223")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act223Request extends ActRequest {
    private Long actNumber;
    private Institution institution;
    private String activityTypeCode;

    @JsonProperty("documentConfirmSampling")
    private String supportingDocumentsForSampling;
    private String goal;

    private Purpose purpose;
    private EmployeeInfo sampler;
    private EmployeeInfo participant;

    private ConditionInfo specialCondition;
    private ConditionInfo storageAndDeliveryCondition;

    private LocalDateTime sampleTakenDateTime;
    private LocalDateTime deliveredDateTime;
    private PackageTypeInfo packageTypeInfo;
    private String additionalInfo;
    private Set<Act223DetailRequest> act223Details;
}
