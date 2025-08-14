package uz.uzinfocom.isemid.features.patient.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import uz.uzinfocom.isemid.features.patient.models.enums.AffiliationType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AffiliationRequest {
    @NotNull(message = "${validation.affiliation_ype_required}")
    private AffiliationType type;
    private String organizationName;
    private String stateCode;
    private String cityCode;
    private Long organizationId;
    private String address;
}
