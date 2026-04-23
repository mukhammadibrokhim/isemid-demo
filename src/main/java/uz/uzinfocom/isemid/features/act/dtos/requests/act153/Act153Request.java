package uz.uzinfocom.isemid.features.act.dtos.requests.act153;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import uz.uzinfocom.isemid.features.act.models.common.ConservationTypeInfo;
import uz.uzinfocom.isemid.features.act.models.common.EmployeeInfo;
import uz.uzinfocom.isemid.features.act.models.common.Institution;
import uz.uzinfocom.isemid.features.act.models.common.PackageTypeInfo;
import uz.uzinfocom.isemid.features.act.models.common.Purpose;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request object for ACT153 type Act")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act153Request extends ActRequest {
    private Long actNumber;
    private Institution institution;
    private String activityTypeCode;
    private String samplingDocuments;
    private LocalDateTime sampleTakenDateTime;
    private LocalDateTime deliveredDateTime;

    private Purpose purpose;
    private EmployeeInfo sampler;
    private EmployeeInfo participant;

    private ConditionInfo specialCondition;
    private ConditionInfo deliveryAndStorageCondition;

    private PackageTypeInfo packageTypeInfo;
    private ConservationTypeInfo conservationTypeInfo;

    private Long lisOrganizationId;
    private String laboratoryAddress;

    private String additionalInfo;
    private Set<Act153DetailRequest> act153Details;
}
