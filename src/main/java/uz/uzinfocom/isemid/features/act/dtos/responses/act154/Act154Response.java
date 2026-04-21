package uz.uzinfocom.isemid.features.act.dtos.responses.act154;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.features.act.dtos.responses.ActResponse;
import uz.uzinfocom.isemid.features.act.models.common.ConditionInfo;
import uz.uzinfocom.isemid.features.act.models.common.EmployeeInfo;
import uz.uzinfocom.isemid.features.act.models.common.Institution;
import uz.uzinfocom.isemid.features.act.models.common.PackageTypeInfo;
import uz.uzinfocom.isemid.features.act.models.common.Purpose;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Schema(description = "Request object for Act154")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act154Response extends ActResponse {
    private Long id;
    private String title;
    private Long serialNumber;
    private Institution institution;
    private String activityTypeCode;

    private LocalDateTime sampleTakenDateTime;
    private LocalDateTime deliveredDateTime;

    private Purpose purpose;
    private EmployeeInfo sampler;
    private EmployeeInfo participant;

    private ConditionInfo specialCondition;
    private ConditionInfo deliveryCondition;
    private ConditionInfo storageCondition;

    private PackageTypeInfo packageTypeInfo;

    private String additionalInfo;
    private Set<Act154DetailResponse> act154Details;
}
