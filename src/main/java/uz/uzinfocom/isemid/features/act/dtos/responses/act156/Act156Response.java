package uz.uzinfocom.isemid.features.act.dtos.responses.act156;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.features.act.dtos.responses.ActResponse;
import uz.uzinfocom.isemid.features.act.models.act156.Act156GroupDetail;
import uz.uzinfocom.isemid.features.act.models.act156.Act156KitchenUtensil;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Schema(description = "Request object for Act156")
public class Act156Response extends ActResponse {
    private Long id;
    private String title;
    private Integer tin;
    private String institutionName;
    private String institutionAddress;
    private LocalDateTime sampleTakenTime;
    private Long lisOrganizationId;
    private String laboratoryAddress;
    private LocalDateTime sampleDeliveryTime;
    private String fullNameofSampler;
    private String positionOfSampler;
    private String fullNameOfObjectRepresentative;
    private String positionOfObjectRepresentative;
    private Set<Act156KitchenUtensil> act156KitchenUtensils;
    private Set<Act156GroupDetail> act156GroupDetails;
}