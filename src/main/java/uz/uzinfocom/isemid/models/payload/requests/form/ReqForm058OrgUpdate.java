package uz.uzinfocom.isemid.models.payload.requests.form;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqForm058OrgUpdate {
    @NotNull
    private Long receivedOrgId;
    private String workPlaceStateCode;
    private String workPlaceCityCode;

    //address
    private String addressStateCode;
    private String addressCityCode;
    private String addressNeighborhoodCode;
    private String streetAddress;
    private String houseNumber;
    private String apartmentNumber;
}
