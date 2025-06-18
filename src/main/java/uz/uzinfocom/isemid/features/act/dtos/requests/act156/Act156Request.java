package uz.uzinfocom.isemid.features.act.dtos.requests.act156;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uz.uzinfocom.isemid.features.act.dtos.requests.ActRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

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
    private LocalDate documentSendDate;
    private Integer tin;
    private String institutionName;
    private String institutionAddress;
    private LocalDateTime sampleTakenTime;
    private Long organizationId;
    private String laboratoryAddress;
    private LocalDateTime sampleDeliveryTime;
    private String fullNameofSampler;
    private String positionOfSampler;
    private String fullNameOfObjectRepresentative;
    private String positionOfObjectRepresentative;
    private Set<Act156KitchenUtensilRequest> act156KitchenUtensils;
    private Set<Act156GroupDetailRequest> act156GroupDetails;
}