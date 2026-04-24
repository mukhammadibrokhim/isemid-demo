package uz.uzinfocom.isemid.features.act.dtos.responses.act223;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@Schema(description = "Request object for Act223")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act223Response extends ActResponse {
    private Long id;
    private Institution institution;
    private Long actNumber;
    @JsonProperty("documentConfirmSampling")
    private String supportingDocumentsForSampling;
    private String goal;
    private String activityTypeCode;

    private EmployeeInfo sampler;
    private EmployeeInfo participant;
    private Purpose purpose;
    private LocalDateTime sampleTakenDateTime;
    private LocalDateTime deliveredDateTime;

    private ConditionInfo specialCondition;
    private ConditionInfo storageAndDeliveryCondition;
    private Long lisOrganizationId;
    private String laboratoryAddress;

    private PackageTypeInfo packageTypeInfo;

    private String additionalInfo;
    private Set<Act223DetailResponse> act223Details;
}
