package uz.uzinfocom.isemid.features.act.dtos.requests.act156;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;

import java.time.LocalDateTime;
import java.util.Set;
import uz.uzinfocom.isemid.features.act.models.common.Institution;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request object for Act156")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act156Request extends ActRequest {
    private Long id;
    private String title;
    private Institution institution;
    private String activityTypeCode;

    private LocalDateTime sampleTakenTime;
    private Long lisOrganizationId;
    private String laboratoryAddress;
    private LocalDateTime sampleDeliveryTime;
    private String fullNameofSampler;
    private String positionOfSampler;
    private String fullNameOfObjectRepresentative;
    private String positionOfObjectRepresentative;
    private Set<Act156KitchenUtensilRequest> act156KitchenUtensils;
    private Set<Act156GroupDetailRequest> act156GroupDetails;
}