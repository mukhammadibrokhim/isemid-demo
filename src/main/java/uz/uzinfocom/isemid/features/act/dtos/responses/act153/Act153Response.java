package uz.uzinfocom.isemid.features.act.dtos.responses.act153;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.features.act.dtos.responses.ActResponse;
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
@SuperBuilder
@Schema(description = "Request object for ACT153 type Act")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act153Response extends ActResponse {
    private Long id;

    private Institution institution;
    private Long actNumber;
    private String activityTypeCode;

    private String samplingDocuments;
    private LocalDateTime sampleTakenDateTime;
    private LocalDateTime deliveredDateTime;

    private Purpose purpose;
    private EmployeeInfo sampler;
    private EmployeeInfo participant;

    private ConditionInfo specialCondition;
    private ConditionInfo storageAndDeliveryCondition;

    private Long lisOrganizationId;
    private String laboratoryAddress;

    private PackageTypeInfo packageTypeInfo;

    private ConservationTypeInfo conservationTypeInfo;

    private String additionalInfo;
    @Builder.Default
    private Set<Act153DetailResponse> act153Details = new HashSet<>();
}
