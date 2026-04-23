package uz.uzinfocom.isemid.features.act.dtos.requests.act154;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Date;
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
@Schema(description = "Request object for Act154")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act154Request extends ActRequest {
    private String title;
    private Long actNumber;
    private Institution institution;

    // field persistence issue
    private String activityTypeCode;

    private LocalDateTime sampleTakenDateTime;
    private LocalDateTime deliveredDateTime;

    private Purpose purpose;
    private EmployeeInfo sampler;
    private EmployeeInfo participant;

    private ConditionInfo specialCondition;

    private ConditionInfo deliveryAndStorageCondition;
    private String manufacturingCompany;
    private Date manufactureDate;

    private Long lisOrganizationId;
    private String laboratoryAddress;

    private PackageTypeInfo packageTypeInfo;

    private String additionalInfo;
    private Set<Act154DetailRequest> act154Details;
}
