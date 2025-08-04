package uz.uzinfocom.isemid.features.patient.dtos.responses;

import lombok.*;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogMinResponse;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrgMiniResponse;
import uz.uzinfocom.isemid.features.patient.models.enums.AffiliationType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AffiliationResponse {
    private AffiliationType type;
    private CatalogMinResponse state;
    private CatalogMinResponse city;
    private OrgMiniResponse organization;
    private String address;
}
